package com.dukaops.backend.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.dukaops.backend.tenant.Tenant;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "unit_price", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "category_id")
    private UUID categoryId;

    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public Product(
            Tenant tenant,
            String sku,
            String name,
            String description,
            BigDecimal unitPrice,
            BigDecimal costPrice) {

        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.status = ProductStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void updateDetails(
            String sku,
            String name,
            String description,
            BigDecimal unitPrice,
            BigDecimal costPrice) {

        this.sku = sku;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
