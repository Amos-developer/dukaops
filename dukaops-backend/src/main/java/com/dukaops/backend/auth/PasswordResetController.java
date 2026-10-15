package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService service;
    public PasswordResetController(PasswordResetService service) { this.service = service; }
    public record RecoveryResponse(String message) {}
    @PostMapping("/forgot-password")
    public RecoveryResponse request(@Valid @RequestBody ForgotPasswordRequest request) {
        service.request(request);
        return new RecoveryResponse("If an active account matches that email, password reset instructions will be sent. Please check your inbox.");
    }
    @PostMapping("/reset-password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@Valid @RequestBody ResetPasswordRequest request) { service.reset(request); }
}
