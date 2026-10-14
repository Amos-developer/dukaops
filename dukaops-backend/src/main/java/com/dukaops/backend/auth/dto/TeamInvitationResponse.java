package com.dukaops.backend.auth.dto;

import com.dukaops.backend.auth.UserRole;
import java.time.Instant;
import java.util.UUID;

public record TeamInvitationResponse(
        UUID invitationId,
        String email,
        UserRole role,
        Instant expiresAt,
        String invitationToken
) {}
