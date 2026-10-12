CREATE TABLE products (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL
        REFERENCES tenants(id) ON DELETE CASCADE,

    sku VARCHAR(64) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,

    unit_price NUMERIC(12, 2) NOT NULL
        CHECK (unit_price >= 0),

    cost_price NUMERIC(12, 2)
        CHECK (cost_price IS NULL OR cost_price >= 0),

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_products_tenant_sku
    ON products (tenant_id, LOWER(sku));

CREATE INDEX idx_products_tenant_id
    ON products (tenant_id);

CREATE INDEX idx_products_tenant_status
    ON products (tenant_id, status);