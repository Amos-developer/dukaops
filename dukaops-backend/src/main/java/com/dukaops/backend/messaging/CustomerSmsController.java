package com.dukaops.backend.messaging;

import java.util.*;
import com.dukaops.backend.auth.TenantUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messaging/sms")
public class CustomerSmsController {
    private final CustomerSmsService service;
    public CustomerSmsController(CustomerSmsService service) { this.service = service; }
    public record SendRequest(@NotNull UUID customerId, @NotNull UUID requestKey,
            @NotBlank @Size(max=480) String message, @AssertTrue boolean authorizedContact) {}
    @GetMapping("/configuration") public Map<String,Object> configuration(@AuthenticationPrincipal TenantUserPrincipal actor) { return service.configuration(actor); }
    @GetMapping public List<Map<String,Object>> history(@AuthenticationPrincipal TenantUserPrincipal actor, @RequestParam(defaultValue="0") int page) { return service.history(actor, page); }
    @PostMapping public Map<String,Object> send(@AuthenticationPrincipal TenantUserPrincipal actor, @Valid @RequestBody SendRequest request) {
        return service.send(actor, request.customerId(), request.requestKey(), request.message());
    }
    @PostMapping("/{id}/refresh") public Map<String,Object> refresh(@AuthenticationPrincipal TenantUserPrincipal actor, @PathVariable UUID id) { return service.refresh(actor, id); }
}
