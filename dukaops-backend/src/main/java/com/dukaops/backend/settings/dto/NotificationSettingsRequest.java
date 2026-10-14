package com.dukaops.backend.settings.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationSettingsRequest(@NotNull Boolean lowStockAlerts,
        @NotNull Boolean inAppNotifications, @NotNull Boolean emailNotifications) {}
