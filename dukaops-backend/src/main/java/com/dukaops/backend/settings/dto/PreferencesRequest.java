package com.dukaops.backend.settings.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PreferencesRequest(
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank @Size(max = 80) String timezone,
        @NotBlank @Size(max = 30) String dateFormat,
        UUID defaultBranchId) {}
