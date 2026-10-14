package com.dukaops.backend.settings.dto;

public record NotificationSettingsResponse(boolean lowStockAlerts,
        boolean inAppNotifications, boolean emailNotifications,
        boolean emailDeliveryAvailable) {}
