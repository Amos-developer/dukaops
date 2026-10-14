package com.dukaops.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {
    Optional<TeamInvitation> findByTokenHash(String tokenHash);
    boolean existsByTenant_IdAndEmailIgnoreCaseAndAcceptedAtIsNull(UUID tenantId, String email);
}
