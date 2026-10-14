package com.dukaops.backend.settings.dto;

import java.util.UUID;

public record PreferencesResponse(String currency, String timezone,
        String dateFormat, UUID defaultBranchId, String defaultBranchName) {}
