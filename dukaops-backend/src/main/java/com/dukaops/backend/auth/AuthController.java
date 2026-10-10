
package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.RegisterRequest;
import com.dukaops.backend.auth.dto.RegisterResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse currentUser(
            @AuthenticationPrincipal TenantUserPrincipal principal
    ) {
        return new CurrentUserResponse(
                principal.getUserId(),
                principal.getTenantId(),
                principal.getUsername(),
                principal.getRole()
        );
    }

    public record CurrentUserResponse(
            UUID userId,
            UUID tenantId,
            String email,
            UserRole role
    ) {
    }
}
