package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserRole;
import com.dukaops.backend.auth.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TeamUserResponse(
        UUID id,
        String fullName,
        String email,
        UserRole role,
        UserStatus status,
        List<TeamBranchResponse> assignedBranches,
        Instant createdAt,
        Instant updatedAt,
        Instant lastLoginAt
) {
}
