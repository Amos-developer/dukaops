
package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.RegisterRequest;
import com.dukaops.backend.auth.dto.RegisterResponse;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            TenantRepository tenantRepository,
            TenantUserRepository tenantUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String slug = request.businessSlug()
                .trim()
                .toLowerCase(Locale.ROOT);

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (tenantRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Business slug is already in use."
            );
        }

        if (tenantUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email address is already registered."
            );
        }

        Tenant tenant = tenantRepository.save(
                new Tenant(request.businessName().trim(), slug)
        );

        tenant.updateSettings(null, request.phoneNumber(), null,
                tenant.getCurrency(), tenant.getTimezone(), tenant.getDateFormat(), null);

        TenantUser owner = tenantUserRepository.save(
                new TenantUser(
                        tenant,
                        request.fullName().trim(),
                        email,
                        passwordEncoder.encode(request.password()),
                        UserRole.OWNER
                )
        );

        return new RegisterResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                owner.getId(),
                owner.getFullName(),
                owner.getEmail(),
                owner.getRole()
        );
    }
}
