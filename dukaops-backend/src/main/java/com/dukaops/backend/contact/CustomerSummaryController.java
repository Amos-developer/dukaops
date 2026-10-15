package com.dukaops.backend.contact;

import com.dukaops.backend.auth.TenantUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerSummaryController {
    private final CustomerSummaryService service;
    public CustomerSummaryController(CustomerSummaryService service) { this.service = service; }
    @GetMapping("/api/contacts/customers/summary")
    public CustomerSummaryService.Summary summary(@AuthenticationPrincipal TenantUserPrincipal actor) {
        return service.summary(actor);
    }
}
