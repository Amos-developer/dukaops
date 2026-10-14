
package com.dukaops.backend.auth;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TenantUserRepository
        extends JpaRepository<TenantUser, UUID> {

    @EntityGraph(attributePaths = "tenant")
    Optional<TenantUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            UUID userId
    );

    long countByTenant_IdAndRoleAndStatus(
            UUID tenantId,
            UserRole role,
            UserStatus status
    );

    @Query("""
    SELECT u FROM TenantUser u
    LEFT JOIN FETCH u.branchAssignments uba
    LEFT JOIN FETCH uba.branch
    WHERE u.id = :userId
      AND u.tenant.id = :tenantId
    """)
    Optional<TenantUser> findTenantScopedUserById(
            @Param("userId") UUID userId,
            @Param("tenantId") UUID tenantId
    );

    @Query(
            value = """
            SELECT u FROM TenantUser u
            WHERE u.tenant.id = :tenantId
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
              AND (
                  :search IS NULL
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """,
            countQuery = """
            SELECT COUNT(u) FROM TenantUser u
            WHERE u.tenant.id = :tenantId
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
              AND (
                  :search IS NULL
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """
    )
    Page<TenantUser> findTenantUsers(
            @Param("tenantId") UUID tenantId,
            @Param("search") String search,
            @Param("role") UserRole role,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
