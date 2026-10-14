package com.dukaops.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AcceptTeamInvitationRequest(
        @NotBlank @Size(min = 12, max = 64) String password
) {}
