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