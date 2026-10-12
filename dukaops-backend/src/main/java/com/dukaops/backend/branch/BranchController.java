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

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BranchResponse create(
            @PathVariable UUID tenantId,
            @Valid @RequestBody CreateBranchRequest request) {
        return branchService.create(tenantId, request);
    }

    @GetMapping
    public List<BranchResponse> findAll(
            @PathVariable UUID tenantId) {
        return branchService.findAll(tenantId);
    }

    @GetMapping("/{branchId}")
    public BranchResponse findById(
            @PathVariable UUID tenantId,
            @PathVariable UUID branchId) {
        return branchService.findById(tenantId, branchId);
    }
}