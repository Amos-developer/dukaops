package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateTeamUserRequest(
        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 12, max = 64)
        String password,

        @NotNull
        UserRole role,

        Set<UUID> branchIds
) {
}
