package com.dukaops.backend.auth.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import com.dukaops.backend.auth.UserRole;

public record TeamInvitationListResponse(UUID id, String fullName, String email,
        UserRole role, Set<UUID> branchIds, Instant expiresAt, String status) {}
