package com.dukaops.backend.auth;

import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.tenant.Tenant;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "tenant_user_branch_assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_branch_assignment",
                columnNames = {"user_id", "branch_id"}
        )
)
public class TenantUserBranchAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private TenantUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false, updatable = false)
    private Branch branch;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TenantUserBranchAssignment() {
    }

    public TenantUserBranchAssignment(
            Tenant tenant,
            TenantUser user,
            Branch branch
    ) {
        this.tenant = tenant;
        this.user = user;
        this.branch = branch;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public TenantUser getUser() {
        return user;
    }

    public Branch getBranch() {
        return branch;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
