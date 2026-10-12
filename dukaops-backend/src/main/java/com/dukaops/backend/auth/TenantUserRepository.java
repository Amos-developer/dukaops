
package com.dukaops.backend.auth;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TenantUserRepository
        extends JpaRepository<TenantUser, UUID> {

    @EntityGraph(attributePaths = "tenant")
    Optional<TenantUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
    SELECT u FROM TenantUser u
    WHERE u.id = :userId
      AND u.tenant.id = :tenantId
    """)
    Optional<TenantUser> findTenantScopedUserById(
            @Param("userId") UUID userId,
            @Param("tenantId") UUID tenantId
    );
}
