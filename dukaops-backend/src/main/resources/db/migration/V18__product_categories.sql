CREATE TABLE product_categories (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL CHECK (length(trim(name)) > 0),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_product_categories_tenant_id UNIQUE (tenant_id, id)
);
CREATE UNIQUE INDEX uq_product_categories_tenant_name ON product_categories(tenant_id, lower(name));
ALTER TABLE products ADD COLUMN category_id UUID;
ALTER TABLE products ADD CONSTRAINT fk_products_tenant_category
    FOREIGN KEY (tenant_id, category_id) REFERENCES product_categories(tenant_id, id);
CREATE INDEX idx_products_tenant_category ON products(tenant_id, category_id);
