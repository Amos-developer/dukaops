package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateTeamUserRequest(
        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotNull
        UserRole role,

        Set<UUID> branchIds
) {
}
