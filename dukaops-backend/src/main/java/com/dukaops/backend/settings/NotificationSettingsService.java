package com.dukaops.backend.settings;

import com.dukaops.backend.auth.*;
import com.dukaops.backend.settings.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationSettingsService {
    private final NotificationPreferencesRepository repository;
    public NotificationSettingsService(NotificationPreferencesRepository repository) {
        this.repository = repository;
    }
    @Transactional(readOnly = true)
    public NotificationSettingsResponse get(TenantUserPrincipal principal) {
        return response(repository.findById(principal.getTenantId())
                .orElseGet(() -> new NotificationPreferences(principal.getTenantId())));
    }
    @Transactional
    public NotificationSettingsResponse update(TenantUserPrincipal principal, NotificationSettingsRequest request) {
        if (principal.getRole() != UserRole.OWNER && principal.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only owners and administrators can update notification settings.");
        }
        var preferences = repository.findById(principal.getTenantId())
                .orElseGet(() -> new NotificationPreferences(principal.getTenantId()));
        preferences.update(request.lowStockAlerts(), request.inAppNotifications(), request.emailNotifications());
        return response(repository.save(preferences));
    }
    private NotificationSettingsResponse response(NotificationPreferences preferences) {
        return new NotificationSettingsResponse(preferences.isLowStockAlerts(),
                preferences.isInAppNotifications(), preferences.isEmailNotifications(), false);
    }
}
