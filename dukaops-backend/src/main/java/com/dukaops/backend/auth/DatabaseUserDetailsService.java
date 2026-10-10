
package com.dukaops.backend.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final TenantUserRepository tenantUserRepository;

    public DatabaseUserDetailsService(
            TenantUserRepository tenantUserRepository
    ) {
        this.tenantUserRepository = tenantUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        TenantUser account = tenantUserRepository
                .findByEmailIgnoreCase(email.trim())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid email or password"
                        )
                );

        return new TenantUserPrincipal(
                account.getId(),
                account.getTenant().getId(),
                account.getEmail(),
                account.getPasswordHash(),
                account.getRole(),
                account.getStatus() == UserStatus.ACTIVE
        );
    }
}
