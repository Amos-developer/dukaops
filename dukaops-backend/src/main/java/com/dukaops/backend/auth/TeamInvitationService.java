package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.AcceptTeamInvitationRequest;
import com.dukaops.backend.auth.dto.CreateTeamInvitationRequest;
import com.dukaops.backend.auth.dto.TeamInvitationResponse;
import com.dukaops.backend.auth.dto.TeamUserResponse;
import com.dukaops.backend.auth.dto.TeamBranchResponse;
import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.branch.BranchRepository;
import com.dukaops.backend.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TeamInvitationService {
    private final TeamInvitationRepository invitationRepository;
    private final TenantRepository tenantRepository;
    private final TenantUserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public TeamInvitationService(TeamInvitationRepository invitationRepository, TenantRepository tenantRepository,
                                 TenantUserRepository userRepository, BranchRepository branchRepository,
                                 PasswordEncoder passwordEncoder) {
        this.invitationRepository = invitationRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TeamInvitationResponse create(TenantUserPrincipal actor, CreateTeamInvitationRequest request) {
        requireAdmin(actor);
        requireAssignableRole(actor, request.role());
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)
                || invitationRepository.existsByTenant_IdAndEmailIgnoreCaseAndAcceptedAtIsNull(actor.getTenantId(), email)) {
            throw error(HttpStatus.CONFLICT, "Email address already has an account or pending invitation.");
        }
        var tenant = tenantRepository.findById(actor.getTenantId()).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Business not found."));
        var inviter = userRepository.findTenantScopedUserById(actor.getUserId(), actor.getTenantId()).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Inviting account was not found."));
        Set<UUID> branches = validateBranches(actor.getTenantId(), request.role(), request.branchIds());
        String token = randomToken();
        TeamInvitation invitation = new TeamInvitation(tenant, inviter, request.fullName().trim(), email, request.role(), hash(token), Instant.now().plus(48, ChronoUnit.HOURS), branches);
        invitationRepository.save(invitation);
        return new TeamInvitationResponse(invitation.getId(), email, request.role(), invitation.getExpiresAt(), token);
    }

    @Transactional
    public TeamUserResponse accept(String rawToken, AcceptTeamInvitationRequest request) {
        TeamInvitation invitation = invitationRepository.findByTokenHash(hash(rawToken)).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Invitation is invalid or has expired."));
        if (invitation.getAcceptedAt() != null || invitation.getExpiresAt().isBefore(Instant.now())) throw error(HttpStatus.GONE, "Invitation is invalid or has expired.");
        if (userRepository.existsByEmailIgnoreCase(invitation.getEmail())) throw error(HttpStatus.CONFLICT, "Email address is already registered.");
        var tenant = invitation.getTenant();
        var user = new TenantUser(tenant, invitation.getFullName(), invitation.getEmail(), passwordEncoder.encode(request.password()), invitation.getRole());
        user.replaceBranchAssignments(buildAssignments(tenant, user, invitation.getRole(), invitation.getBranchIds()));
        var saved = userRepository.save(user);
        invitation.accept();
        invitationRepository.save(invitation);
        return new TeamUserResponse(saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRole(), saved.getStatus(),
                saved.getBranchAssignments().stream().map(TenantUserBranchAssignment::getBranch).map(b -> new TeamBranchResponse(b.getId(), b.getName(), b.getCode())).toList(), saved.getCreatedAt(), saved.getUpdatedAt(), null);
    }

    private Set<UUID> validateBranches(UUID tenantId, UserRole role, Set<UUID> ids) {
        if (role == UserRole.OWNER || ids == null || ids.isEmpty()) return Set.of();
        Set<UUID> unique = new LinkedHashSet<>(ids);
        for (UUID id : unique) branchRepository.findTenantScopedBranchById(id, tenantId).orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "One or more branch assignments are invalid."));
        return unique;
    }
    private Set<TenantUserBranchAssignment> buildAssignments(com.dukaops.backend.tenant.Tenant tenant, TenantUser user, UserRole role, Set<UUID> ids) {
        Set<TenantUserBranchAssignment> result = new LinkedHashSet<>();
        for (UUID id : validateBranches(tenant.getId(), role, ids)) {
            Branch branch = branchRepository.findTenantScopedBranchById(id, tenant.getId()).orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "One or more branch assignments are invalid."));
            result.add(new TenantUserBranchAssignment(tenant, user, branch));
        }
        return result;
    }
    private void requireAdmin(TenantUserPrincipal actor) { if (actor.getRole() != UserRole.OWNER && actor.getRole() != UserRole.ADMIN) throw error(HttpStatus.FORBIDDEN, "You do not have permission to invite team users."); }
    private void requireAssignableRole(TenantUserPrincipal actor, UserRole role) { if (role == UserRole.OWNER || (actor.getRole() == UserRole.ADMIN && role == UserRole.ADMIN)) throw error(HttpStatus.FORBIDDEN, "This role cannot be assigned by the current account."); }
    private String randomToken() { byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes); return HexFormat.of().formatHex(bytes); }
    private String hash(String value) { try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception e) { throw new IllegalStateException("Could not create invitation token.", e); } }
    private ResponseStatusException error(HttpStatus status, String message) { return new ResponseStatusException(status, message); }
}
