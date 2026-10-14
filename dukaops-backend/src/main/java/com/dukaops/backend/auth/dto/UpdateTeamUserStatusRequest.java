package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamUserStatusRequest(
        @NotNull
        UserStatus status
) {
}
