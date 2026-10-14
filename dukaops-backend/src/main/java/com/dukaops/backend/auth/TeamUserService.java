package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.CreateTeamUserRequest;
import com.dukaops.backend.auth.dto.TeamBranchResponse;
import com.dukaops.backend.auth.dto.TeamUserPageResponse;
import com.dukaops.backend.auth.dto.TeamUserResponse;
import com.dukaops.backend.auth.dto.UpdateTeamUserRequest;
import com.dukaops.backend.auth.dto.UpdateTeamUserStatusRequest;
import com.dukaops.backend.auth.dto.UpdateTeamUserRoleRequest;
import com.dukaops.backend.auth.dto.UpdateTeamUserBranchesRequest;
import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.branch.BranchRepository;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TeamUserService {

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public TeamUserService(
            TenantRepository tenantRepository,
            TenantUserRepository tenantUserRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public TeamUserPageResponse listUsers(
            TenantUserPrincipal actor,
            String search,
            UserRole role,
            UserStatus status,
            int page,
            int size
    ) {
        requireUserAdmin(actor);

        var pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.ASC, "fullName")
        );

        String cleanSearch =
                search == null || search.isBlank() ? null : search.trim();

        var users = tenantUserRepository.findTenantUsers(
                actor.getTenantId(),
                cleanSearch,
                role,
                status,
                pageable
        );

        return new TeamUserPageResponse(
                users.getContent().stream().map(this::toResponse).toList(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public TeamUserResponse getUser(
            TenantUserPrincipal actor,
            UUID userId
    ) {
        requireUserAdmin(actor);
        return toResponse(findTenantUser(actor.getTenantId(), userId));
    }

    @Transactional
    public TeamUserResponse createUser(
            TenantUserPrincipal actor,
            CreateTeamUserRequest request
    ) {
        requireUserAdmin(actor);
        requireAssignableRole(actor, request.role());

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (tenantUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email address is already registered."
            );
        }

        Tenant tenant = tenantRepository.findById(actor.getTenantId())
                .orElseThrow(() -> notFound("Business not found."));

        TenantUser user = new TenantUser(
                tenant,
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.role()
        );

        user.replaceBranchAssignments(buildAssignments(
                tenant,
                user,
                request.role(),
                request.branchIds()
        ));

        return toResponse(tenantUserRepository.save(user));
    }

    @Transactional
    public TeamUserResponse updateUser(
            TenantUserPrincipal actor,
            UUID userId,
            UpdateTeamUserRequest request
    ) {
        requireUserAdmin(actor);

        TenantUser user = findTenantUser(actor.getTenantId(), userId);
        preventOwnerMutation(user);
        preventSelfPrivilegeChange(actor, user, request.role());
        requireAssignableRole(actor, request.role());

        user.updateProfile(request.fullName().trim(), request.role());
        user.replaceBranchAssignments(buildAssignments(
                user.getTenant(),
                user,
                request.role(),
                request.branchIds()
        ));

        return toResponse(tenantUserRepository.save(user));
    }

    @Transactional
    public TeamUserResponse updateStatus(
            TenantUserPrincipal actor,
            UUID userId,
            UpdateTeamUserStatusRequest request
    ) {
        requireUserAdmin(actor);

        TenantUser user = findTenantUser(actor.getTenantId(), userId);
        preventOwnerMutation(user);

        if (actor.getUserId().equals(user.getId())
                && request.status() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot deactivate your own account."
            );
        }

        user.updateStatus(request.status());
        return toResponse(tenantUserRepository.save(user));
    }

    @Transactional
    public TeamUserResponse updateRole(TenantUserPrincipal actor, UUID userId, UpdateTeamUserRoleRequest request) {
        requireUserAdmin(actor);
        TenantUser user = findTenantUser(actor.getTenantId(), userId);
        preventOwnerMutation(user);
        preventSelfPrivilegeChange(actor, user, request.role());
        requireAssignableRole(actor, request.role());
        user.updateProfile(user.getFullName(), request.role());
        user.replaceBranchAssignments(buildAssignments(user.getTenant(), user, request.role(), user.getBranchAssignments().stream().map(a -> a.getBranch().getId()).collect(java.util.stream.Collectors.toSet())));
        return toResponse(tenantUserRepository.save(user));
    }

    @Transactional
    public TeamUserResponse updateBranches(TenantUserPrincipal actor, UUID userId, UpdateTeamUserBranchesRequest request) {
        requireUserAdmin(actor);
        TenantUser user = findTenantUser(actor.getTenantId(), userId);
        preventOwnerMutation(user);
        user.replaceBranchAssignments(buildAssignments(user.getTenant(), user, user.getRole(), request.branchIds()));
        return toResponse(tenantUserRepository.save(user));
    }

    private Set<TenantUserBranchAssignment> buildAssignments(
            Tenant tenant,
            TenantUser user,
            UserRole role,
            Set<UUID> branchIds
    ) {
        if (role == UserRole.OWNER || branchIds == null || branchIds.isEmpty()) {
            return Set.of();
        }

        Set<UUID> uniqueBranchIds = new LinkedHashSet<>(branchIds);
        Set<TenantUserBranchAssignment> assignments = new LinkedHashSet<>();

        for (UUID branchId : uniqueBranchIds) {
            Branch branch = branchRepository
                    .findTenantScopedBranchById(branchId, tenant.getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "One or more branch assignments are invalid."
                    ));

            assignments.add(new TenantUserBranchAssignment(
                    tenant,
                    user,
                    branch
            ));
        }

        return assignments;
    }

    private TenantUser findTenantUser(UUID tenantId, UUID userId) {
        return tenantUserRepository.findTenantScopedUserById(userId, tenantId)
                .orElseThrow(() -> notFound("User not found for this business."));
    }

    private void requireUserAdmin(TenantUserPrincipal actor) {
        if (actor.getRole() != UserRole.OWNER
                && actor.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to manage team users."
            );
        }
    }

    private void requireAssignableRole(
            TenantUserPrincipal actor,
            UserRole requestedRole
    ) {
        if (requestedRole == UserRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Owner accounts cannot be managed here."
            );
        }

        if (actor.getRole() == UserRole.ADMIN
                && (requestedRole == UserRole.ADMIN
                || requestedRole == UserRole.OWNER)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admins cannot assign privileged roles."
            );
        }
    }

    private void preventOwnerMutation(TenantUser user) {
        if (user.getRole() == UserRole.OWNER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Owner accounts cannot be changed by this endpoint."
            );
        }
    }

    private void preventSelfPrivilegeChange(
            TenantUserPrincipal actor,
            TenantUser user,
            UserRole requestedRole
    ) {
        if (actor.getUserId().equals(user.getId())
                && user.getRole() != requestedRole) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot change your own role."
            );
        }
    }

    private TeamUserResponse toResponse(TenantUser user) {
        List<TeamBranchResponse> branches = user.getBranchAssignments()
                .stream()
                .map(TenantUserBranchAssignment::getBranch)
                .map(branch -> new TeamBranchResponse(
                        branch.getId(),
                        branch.getName(),
                        branch.getCode()
                ))
                .toList();

        return new TeamUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                branches,
                user.getCreatedAt(),
                user.getUpdatedAt(),
                null
        );
    }

    private ResponseStatusException notFound(String message) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
}
