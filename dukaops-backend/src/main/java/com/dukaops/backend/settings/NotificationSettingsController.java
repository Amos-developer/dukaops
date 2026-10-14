package com.dukaops.backend.settings;

import com.dukaops.backend.auth.TenantUserPrincipal;
import com.dukaops.backend.settings.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings/notifications")
public class NotificationSettingsController {
    private final NotificationSettingsService service;
    public NotificationSettingsController(NotificationSettingsService service) { this.service = service; }
    @GetMapping public NotificationSettingsResponse get(@AuthenticationPrincipal TenantUserPrincipal principal) {
        return service.get(principal);
    }
    @PutMapping public NotificationSettingsResponse update(@AuthenticationPrincipal TenantUserPrincipal principal,
            @Valid @RequestBody NotificationSettingsRequest request) {
        return service.update(principal, request);
    }
}
