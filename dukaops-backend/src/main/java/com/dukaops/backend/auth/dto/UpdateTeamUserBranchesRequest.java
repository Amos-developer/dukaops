package com.dukaops.backend.auth.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record UpdateTeamUserBranchesRequest(@NotNull Set<UUID> branchIds) {}
