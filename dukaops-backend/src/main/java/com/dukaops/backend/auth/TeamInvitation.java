package com.dukaops.backend.auth;

import com.dukaops.backend.tenant.Tenant;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "team_invitations")
public class TeamInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private TenantUser invitedBy;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 254)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @ElementCollection
    @CollectionTable(name = "team_invitation_branches", joinColumns = @JoinColumn(name = "invitation_id"))
    @Column(name = "branch_id", nullable = false)
    private Set<UUID> branchIds = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected TeamInvitation() {}

    public TeamInvitation(Tenant tenant, TenantUser invitedBy, String fullName, String email,
                          UserRole role, String tokenHash, Instant expiresAt, Set<UUID> branchIds) {
        this.tenant = tenant;
        this.invitedBy = invitedBy;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        if (branchIds != null) this.branchIds.addAll(branchIds);
    }

    @PrePersist protected void onCreate() { if (createdAt == null) createdAt = Instant.now(); }
    public UUID getId() { return id; }
    public Tenant getTenant() { return tenant; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public String getTokenHash() { return tokenHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getAcceptedAt() { return acceptedAt; }
    public Set<UUID> getBranchIds() { return branchIds; }
    public void accept() { acceptedAt = Instant.now(); }
}
