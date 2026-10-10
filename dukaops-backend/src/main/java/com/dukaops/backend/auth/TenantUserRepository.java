
package com.dukaops.backend.auth;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantUserRepository
        extends JpaRepository<TenantUser, UUID> {

    @EntityGraph(attributePaths = "tenant")
    Optional<TenantUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
