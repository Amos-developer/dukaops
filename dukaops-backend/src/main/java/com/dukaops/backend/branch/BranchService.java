package com.dukaops.backend.branch;

import com.dukaops.backend.branch.dto.BranchResponse;
import com.dukaops.backend.branch.dto.CreateBranchRequest;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BranchService {

    private final BranchRepository branchRepository;
    private final TenantRepository tenantRepository;

    public BranchService(
            BranchRepository branchRepository,
            TenantRepository tenantRepository) {
        this.branchRepository = branchRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public BranchResponse create(
            UUID tenantId,
            CreateBranchRequest request) {

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Business not found"
            ));

        String code = request.code()
            .trim()
            .toUpperCase(Locale.ROOT);

        if (branchRepository.existsByTenant_IdAndCode(
                tenantId, code)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This branch code already exists for the business"
            );
        }

        boolean hasMainBranch =
            branchRepository.existsByTenant_IdAndMainBranchTrue(
                tenantId);

        boolean requestedMain =
            Boolean.TRUE.equals(request.isMain());

        if (requestedMain && hasMainBranch) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "This business already has a main branch"
            );
        }

        // Automatically make the first branch the main branch.
        boolean mainBranch = requestedMain || !hasMainBranch;

        Branch branch = new Branch(
            tenant,
            request.name().trim(),
            code,
            mainBranch
        );

        return toResponse(branchRepository.save(branch));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> findAll(UUID tenantId) {

        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Business not found"
            );
        }

        return branchRepository
            .findAllByTenant_IdOrderByCreatedAtDesc(tenantId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse findById(
            UUID tenantId,
            UUID branchId) {

        Branch branch = branchRepository
            .findByIdAndTenant_Id(branchId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Branch not found for this business"
            ));

        return toResponse(branch);
    }

    @Transactional
    public BranchResponse update(com.dukaops.backend.auth.TenantUserPrincipal actor, UUID branchId,
            com.dukaops.backend.branch.dto.UpdateBranchRequest request) {
        requireOwner(actor);
        var branch = branchRepository.findTenantScopedBranchByIdForUpdate(branchId, actor.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found."));
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (branchRepository.existsByTenant_IdAndCodeAndIdNot(actor.getTenantId(), code, branchId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Branch code is already in use.");
        branch.updateDetails(request.name().trim(), code);
        return toResponse(branch);
    }

    @Transactional
    public BranchResponse status(com.dukaops.backend.auth.TenantUserPrincipal actor, UUID branchId, BranchStatus status) {
        requireOwner(actor);
        var branch = branchRepository.findTenantScopedBranchByIdForUpdate(branchId, actor.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found."));
        var tenant = tenantRepository.findById(actor.getTenantId()).orElseThrow();
        if (status == BranchStatus.SUSPENDED && (branch.isMain()
                || (tenant.getDefaultBranch() != null && tenant.getDefaultBranch().getId().equals(branchId))))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The main or default branch cannot be deactivated.");
        branch.updateStatus(status);
        return toResponse(branch);
    }

    private void requireOwner(com.dukaops.backend.auth.TenantUserPrincipal actor) {
        if (actor.getRole() != com.dukaops.backend.auth.UserRole.OWNER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the business owner can manage branches.");
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
            branch.getId(),
            branch.getTenant().getId(),
            branch.getName(),
            branch.getCode(),
            branch.isMain(),
            branch.getStatus(),
            branch.getCreatedAt(),
            branch.getUpdatedAt()
        );
    }
}
