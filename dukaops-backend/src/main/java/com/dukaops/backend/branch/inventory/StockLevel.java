package com.dukaops.backend.branch.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.product.Product;
import com.dukaops.backend.tenant.Tenant;

import jakarta.persistence.*;

@Entity
@Table(
    name = "stock_levels",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_stock_levels_branch_product",
        columnNames = {"tenant_id", "branch_id", "product_id"}
    )
)
public class StockLevel {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false,
            precision = 14, scale = 3)
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected StockLevel() {
    }

    public StockLevel(
            Tenant tenant,
            Branch branch,
            Product product,
            BigDecimal reorderLevel) {

        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.branch = branch;
        this.product = product;
        this.quantity = BigDecimal.ZERO;
        this.reorderLevel = reorderLevel  == null
            ? BigDecimal.ZERO
            : reorderLevel;
    }

    public void applyQuantityChange(BigDecimal change) {
        BigDecimal newQuantity = quantity.add(change);

        if (newQuantity.signum() < 0) {
            throw new IllegalArgumentException(
                "Stock quantity cannot be negative");
        }

        this.quantity = newQuantity;
    }

    public void updateReorderLevel(BigDecimal reorderLevel) {
        if (reorderLevel == null || reorderLevel.signum() < 0) {
            throw new IllegalArgumentException(
                "Reorder level cannot be negative");
        }

        this.reorderLevel = reorderLevel;
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

    public UUID getId() {
        return id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public Branch getBranch() {
        return branch;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getReorderLevel() {
        return reorderLevel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}