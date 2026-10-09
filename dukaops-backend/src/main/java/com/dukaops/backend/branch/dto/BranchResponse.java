package com.dukaops.backend.branch.dto;

import com.dukaops.backend.branch.BranchStatus;

import java.time.Instant;
import java.util.UUID;

public record BranchResponse(
    UUID id,
    UUID tenantId,
    String name,
    String code,
    boolean isMain,
    BranchStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}