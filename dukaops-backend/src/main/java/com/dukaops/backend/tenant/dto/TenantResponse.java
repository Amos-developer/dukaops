package com.dukaops.backend.tenant.dto;

import com.dukaops.backend.tenant.TenantStatus;
import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
    UUID id,
    String name,
    String slug,
    TenantStatus status,
    Instant createdAt,
    Instant updatedAt
) {
}