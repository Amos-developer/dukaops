package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateTeamUserRoleRequest(@NotNull UserRole role) {}
