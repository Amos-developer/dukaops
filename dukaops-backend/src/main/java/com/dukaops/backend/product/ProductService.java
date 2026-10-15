package com.dukaops.backend.product;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.dukaops.backend.product.dto.ProductRequest;
import com.dukaops.backend.product.dto.ProductResponse;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProductService {
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;

    public void delete(com.dukaops.backend.auth.TenantUserPrincipal actor, UUID productId) {
        if (actor.getRole() != com.dukaops.backend.auth.UserRole.OWNER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the business owner can delete products.");
        Product product = findProduct(actor.getTenantId(), productId);
        entityManager.lock(product, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        for (String entity : List.of("StockLevel", "StockMovement", "SalesOrderItem")) {
            long references = entityManager.createQuery("select count(record) from " + entity
                    + " record where record.tenant.id = :tenantId and record.product.id = :productId", Long.class)
                    .setParameter("tenantId", actor.getTenantId()).setParameter("productId", productId).getSingleResult();
            if (references > 0) throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This product has stock or transaction records. Archive it instead.");
        }
        productRepository.delete(product);
        productRepository.flush();
    }

    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;

    public ProductService(
            ProductRepository productRepository,
            TenantRepository tenantRepository) {
        this.productRepository = productRepository;
        this.tenantRepository = tenantRepository;
    }

    public ProductResponse create(
            UUID tenantId,
            ProductRequest request) {

        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Business not found"));

        String sku = normalizeSku(request.sku());

        if (productRepository
                .existsByTenant_IdAndSkuIgnoreCase(tenantId, sku)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A product with this SKU already exists");
        }

        Product product = new Product(
            tenant,
            sku,
            request.name().trim(),
            normalizeDescription(request.description()),
            request.unitPrice(),
            request.costPrice()
        );

        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Business not found");
        }

        return productRepository
            .findAllByTenant_IdOrderByNameAsc(tenantId)
            .stream()
            .map(ProductResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID tenantId, UUID productId) {
        return ProductResponse.from(findProduct(tenantId, productId));
    }

    public ProductResponse update(
            UUID tenantId,
            UUID productId,
            ProductRequest request) {

        Product product = findProduct(tenantId, productId);
        String sku = normalizeSku(request.sku());

        if (productRepository
                .existsByTenant_IdAndSkuIgnoreCaseAndIdNot(
                    tenantId, sku, productId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A product with this SKU already exists");
        }

        product.updateDetails(
            sku,
            request.name().trim(),
            normalizeDescription(request.description()),
            request.unitPrice(),
            request.costPrice()
        );

        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse changeStatus(
            UUID tenantId,
            UUID productId,
            ProductStatus status) {

        Product product = findProduct(tenantId, productId);

        if (status == ProductStatus.ACTIVE) {
            product.activate();
        } else {
            product.deactivate();
        }

        return ProductResponse.from(productRepository.save(product));
    }

    private Product findProduct(UUID tenantId, UUID productId) {
        return productRepository
            .findByIdAndTenant_Id(productId, tenantId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Product not found"));
    }

    private String normalizeSku(String sku) {
        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
