package com.dukaops.backend.settings;

import com.dukaops.backend.settings.dto.SubscriptionSettingsResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionSettingsController {
    @GetMapping("/api/settings/subscription")
    @PreAuthorize("hasRole('OWNER')")
    public SubscriptionSettingsResponse get() {
        return new SubscriptionSettingsResponse(false, "Subscription information is not available yet.");
    }
}
