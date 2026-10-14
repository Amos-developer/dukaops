package com.dukaops.backend.auth;
import com.dukaops.backend.tenant.Tenant;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity 
@Table (name = "tenant_users")
public class TenantUser {
    
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @BatchSize(size = 50)
    private Set<TenantUserBranchAssignment> branchAssignments =
            new LinkedHashSet<>();

    protected TenantUser() {
    }

    public TenantUser(
            Tenant tenant,
            String fullName,
            String email,
            String passwordHash,
            UserRole role
    ) {
        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void updateProfile(String fullName, UserRole role) {
        this.fullName = fullName;
        this.role = role;
    }

    public void updateFullName(String fullName) { this.fullName = fullName; }
    public void changePassword(String passwordHash) { this.passwordHash = passwordHash; }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }

    public Set<TenantUserBranchAssignment> getBranchAssignments() {
        return branchAssignments;
    }

    public void replaceBranchAssignments(
            Set<TenantUserBranchAssignment> branchAssignments) {
        this.branchAssignments.clear();
        this.branchAssignments.addAll(branchAssignments);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
