package com.dukaops.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, UUID> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "branchIds")
    java.util.List<TeamInvitation> findByTenant_IdAndAcceptedAtIsNull(UUID tenantId);
    Optional<TeamInvitation> findByTokenHash(String tokenHash);
    boolean existsByTenant_IdAndEmailIgnoreCaseAndAcceptedAtIsNull(UUID tenantId, String email);
}
