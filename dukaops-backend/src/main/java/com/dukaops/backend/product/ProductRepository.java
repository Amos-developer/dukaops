
package com.dukaops.backend.product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, UUID> {

    boolean existsByTenant_IdAndSkuIgnoreCase(
            UUID tenantId,
            String sku);

    List<Product> findAllByTenant_IdOrderByNameAsc(
            UUID tenantId);

    Optional<Product> findByIdAndTenant_Id(
            UUID productId,
            UUID tenantId);

    boolean existsByTenant_IdAndSkuIgnoreCaseAndIdNot(
            UUID tenantId,
            String sku,
            UUID productId
    );
}
