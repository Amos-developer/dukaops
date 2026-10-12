
-- Composite unique constraints ensure inventory records
-- cannot accidentally link a branch or product from another tenant.
ALTER TABLE branches
    ADD CONSTRAINT uq_branches_tenant_id_id
    UNIQUE (tenant_id, id);

ALTER TABLE products
    ADD CONSTRAINT uq_products_tenant_id_id
    UNIQUE (tenant_id, id);

ALTER TABLE tenant_users
    ADD CONSTRAINT uq_tenant_users_tenant_id_id
    UNIQUE (tenant_id, id);


-- Current stock for each product at each branch.
CREATE TABLE stock_levels (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    product_id UUID NOT NULL,

    quantity NUMERIC(14, 3) NOT NULL DEFAULT 0
        CHECK (quantity >= 0),

    reorder_level NUMERIC(14, 3) NOT NULL DEFAULT 0
        CHECK (reorder_level >= 0),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_stock_levels_branch_product
        UNIQUE (tenant_id, branch_id, product_id),

    CONSTRAINT fk_stock_levels_branch
        FOREIGN KEY (tenant_id, branch_id)
        REFERENCES branches (tenant_id, id)
        ON DELETE CASCADE,

    CONSTRAINT fk_stock_levels_product
        FOREIGN KEY (tenant_id, product_id)
        REFERENCES products (tenant_id, id)
        ON DELETE CASCADE
);

CREATE INDEX idx_stock_levels_tenant_branch
    ON stock_levels (tenant_id, branch_id);


-- An audit trail of stock receipts, sales, adjustments and transfers.
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    product_id UUID NOT NULL,

    movement_type VARCHAR(20) NOT NULL
        CHECK (movement_type IN (
            'RECEIPT',
            'SALE',
            'ADJUSTMENT',
            'TRANSFER_IN',
            'TRANSFER_OUT'
        )),

    quantity_change NUMERIC(14, 3) NOT NULL
        CHECK (quantity_change <> 0),

    quantity_after NUMERIC(14, 3) NOT NULL
        CHECK (quantity_after >= 0),

    reference VARCHAR(100),
    notes TEXT,
    performed_by UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_stock_movement_direction CHECK (
        (movement_type IN ('RECEIPT', 'TRANSFER_IN')
            AND quantity_change > 0)
        OR
        (movement_type IN ('SALE', 'TRANSFER_OUT')
            AND quantity_change < 0)
        OR
        movement_type = 'ADJUSTMENT'
    ),

    CONSTRAINT fk_stock_movements_branch
        FOREIGN KEY (tenant_id, branch_id)
        REFERENCES branches (tenant_id, id)
        ON DELETE CASCADE,

    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (tenant_id, product_id)
        REFERENCES products (tenant_id, id)
        ON DELETE CASCADE,

    CONSTRAINT fk_stock_movements_user
        FOREIGN KEY (tenant_id, performed_by)
        REFERENCES tenant_users (tenant_id, id)
        ON DELETE SET NULL (performed_by)
);

CREATE INDEX idx_stock_movements_branch_product_date
    ON stock_movements (
        tenant_id, branch_id, product_id, created_at DESC
    );
