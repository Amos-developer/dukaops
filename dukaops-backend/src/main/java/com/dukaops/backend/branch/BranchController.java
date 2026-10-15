package com.dukaops.backend.branch;

import com.dukaops.backend.branch.dto.BranchResponse;
import com.dukaops.backend.branch.dto.CreateBranchRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants/{tenantId}/branches")
public class BranchController {

    private final BranchService branchService;
    @org.springframework.beans.factory.annotation.Autowired private com.dukaops.backend.auth.AccessPolicy accessPolicy;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateBranchRequest request) {
        checkTenant(tenantId);
        if (principal().getRole() != com.dukaops.backend.auth.UserRole.OWNER && principal().getRole() != com.dukaops.backend.auth.UserRole.ADMIN)
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Only the business owner can create branches.");
        return branchService.create(tenantId, request);
    }

    @GetMapping
    public List<BranchResponse> findAll(
            @PathVariable UUID tenantId) {
        checkTenant(tenantId);
        return branchService.accessibleBranches(principal());
    }

    @GetMapping("/{branchId}")
    public BranchResponse findById(
            @PathVariable UUID tenantId,
            @PathVariable UUID branchId) {
        checkTenant(tenantId);
        accessPolicy.requireBranch(principal(), branchId);
        return branchService.findById(tenantId, branchId);
    }
    @PutMapping("/{branchId}")
    public BranchResponse update(@PathVariable UUID tenantId, @PathVariable UUID branchId,
            @Valid @RequestBody com.dukaops.backend.branch.dto.UpdateBranchRequest request) {
        checkTenant(tenantId);
        return branchService.update(principal(), branchId, request);
    }
    @PatchMapping("/{branchId}/status")
    public BranchResponse status(@PathVariable UUID tenantId, @PathVariable UUID branchId,
            @Valid @RequestBody com.dukaops.backend.branch.dto.BranchStatusRequest request) {
        checkTenant(tenantId);
        return branchService.status(principal(), branchId, request.status());
    }
    private com.dukaops.backend.auth.TenantUserPrincipal principal() {
        return (com.dukaops.backend.auth.TenantUserPrincipal) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
    private void checkTenant(UUID tenantId) {
        if (!principal().getTenantId().equals(tenantId))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found.");
    }
}
