
package com.dukaops.backend.auth;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("tenantAccess")
public class TenantAccess {

    public boolean canAccess(
            UUID tenantId,
            Authentication authentication) {

        if (tenantId == null
                || authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof TenantUserPrincipal user)) {
            return false;
        }

        return tenantId.equals(user.getTenantId());
    }
}
