package com.dukaops.backend.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false) private TenantUser user;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(name = "password_fingerprint", nullable = false, length = 64) private String passwordFingerprint;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "used_at") private Instant usedAt;
    protected PasswordResetToken() {}
    public PasswordResetToken(TenantUser user, String hash, String fingerprint, Instant now) {
        id = UUID.randomUUID(); this.user = user; tokenHash = hash;
        passwordFingerprint = fingerprint; createdAt = now; expiresAt = now.plusSeconds(1800);
    }
    public TenantUser getUser() { return user; }
    public String getPasswordFingerprint() { return passwordFingerprint; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public void consume(Instant now) { usedAt = now; }
}
