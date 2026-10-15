package com.dukaops.backend.audit;

import java.util.UUID;
import com.dukaops.backend.auth.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SecurityAuditService {
    @PersistenceContext private EntityManager entityManager;
    public TenantUserPrincipal requireBusinessAdmin(UUID tenantId) {
        var actor = actor(tenantId);
        if (actor.getRole() != UserRole.OWNER && actor.getRole() != UserRole.ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners and administrators can change product details and costs.");
        return actor;
    }
    private TenantUserPrincipal actor(UUID tenantId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof TenantUserPrincipal actor))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        if (!actor.getTenantId().equals(tenantId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found.");
        return actor;
    }
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID tenantId, UUID branchId, String action, UUID resourceId,
            String before, String after, String reason) {
        var actor = actor(tenantId);
        entityManager.persist(new SecurityAuditEvent(tenantId, actor.getUserId(), branchId,
                action, resourceId, before, after, reason));
    }
}
