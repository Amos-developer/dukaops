package com.dukaops.backend.branch;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

import com.dukaops.backend.tenant.Tenant;

@Entity 
@Table(name = "branches")
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

     @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(name = "is_main", nullable = false)
    private boolean mainBranch = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BranchStatus status = BranchStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Branch() {
    }

    public Branch(
            Tenant tenant,
            String name,
            String code,
            boolean mainBranch) {
        this.tenant = tenant;
        this.name = name;
        this.code = code;
        this.mainBranch = mainBranch;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;

        if (status == null) {
            status = BranchStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void updateDetails(String name, String code) { this.name = name; this.code = code; }
    public void updateStatus(BranchStatus status) { this.status = status; }

    public Tenant getTenant() {
        return tenant;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isMain() {
        return mainBranch;
    }

    public BranchStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
