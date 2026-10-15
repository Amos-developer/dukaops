package com.dukaops.backend.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "security_audit_events")
public class SecurityAuditEvent {
    @Id private UUID id;
    @Column(name = "tenant_id", nullable = false, updatable = false) private UUID tenantId;
    @Column(name = "actor_id", nullable = false, updatable = false) private UUID actorId;
    @Column(name = "branch_id", updatable = false) private UUID branchId;
    @Column(nullable = false, length = 60, updatable = false) private String action;
    @Column(name = "resource_id", nullable = false, updatable = false) private UUID resourceId;
    @Column(name = "before_value", columnDefinition = "TEXT", updatable = false) private String beforeValue;
    @Column(name = "after_value", columnDefinition = "TEXT", updatable = false) private String afterValue;
    @Column(length = 2000, updatable = false) private String reason;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected SecurityAuditEvent() {}
    public SecurityAuditEvent(UUID tenantId, UUID actorId, UUID branchId, String action,
            UUID resourceId, String beforeValue, String afterValue, String reason) {
        id = UUID.randomUUID(); this.tenantId = tenantId; this.actorId = actorId;
        this.branchId = branchId; this.action = action; this.resourceId = resourceId;
        this.beforeValue = beforeValue; this.afterValue = afterValue;
        this.reason = reason; createdAt = Instant.now();
    }
}
