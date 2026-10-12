package com.dukaops.backend.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import com.dukaops.backend.auth.TenantUser;

import java.util.UUID;

public interface TenantRepository
        extends JpaRepository<Tenant, UUID> {

    boolean existsBySlug(String slug);

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
