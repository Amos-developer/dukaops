package com.dukaops.backend.branch.dto;
import jakarta.validation.constraints.NotNull;
import com.dukaops.backend.branch.BranchStatus;
public record BranchStatusRequest(@NotNull BranchStatus status) {}
