
package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserRole;

import java.util.UUID;

public record RegisterResponse(
        UUID tenantId,
        String businessName,
        String businessSlug,
        UUID userId,
        String fullName,
        String email,
        UserRole role
) {
}
