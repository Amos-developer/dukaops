package com.dukaops.backend.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.dukaops.backend.auth.TenantUser;
import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.product.Product;
import com.dukaops.backend.tenant.Tenant;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_movements")
public class StockMovement {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType;

    @Column(name = "quantity_change", nullable = false,
            precision = 14, scale = 3)
    private BigDecimal quantityChange;

    @Column(name = "quantity_after", nullable = false,
            precision = 14, scale = 3)
    private BigDecimal quantityAfter;

    @Column(length = 100)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private TenantUser performedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    public StockMovement(
            Tenant tenant,
            Branch branch,
            Product product,
            StockMovementType movementType,
            BigDecimal quantityChange,
            BigDecimal quantityAfter,
            String reference,
            String notes,
            TenantUser performedBy) {

        this.id = UUID.randomUUID();
        this.tenant = tenant;
        this.branch = branch;
        this.product = product;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.reference = reference;
        this.notes = notes;
        this.performedBy = performedBy;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }

        if (createdAt == null) {
            createdAt = Instant.now();
        }
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

    public StockMovementType getMovementType() {
        return movementType;
    }

    public BigDecimal getQuantityChange() {
        return quantityChange;
    }

    public BigDecimal getQuantityAfter() {
        return quantityAfter;
    }

    public String getReference() {
        return reference;
    }

    public String getNotes() {
        return notes;
    }

    public TenantUser getPerformedBy() {
        return performedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}