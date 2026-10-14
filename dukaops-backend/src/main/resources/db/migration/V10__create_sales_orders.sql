CREATE TABLE sales_orders (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    created_by UUID NOT NULL,
    customer_name VARCHAR(150),
    customer_phone VARCHAR(40),
    status VARCHAR(20) NOT NULL,
    total_amount NUMERIC(14,2) NOT NULL CHECK (total_amount >= 0),
    idempotency_key VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_sales_order_idempotency UNIQUE (tenant_id, idempotency_key),
    CONSTRAINT fk_sales_orders_branch FOREIGN KEY (tenant_id, branch_id) REFERENCES branches (tenant_id, id),
    CONSTRAINT fk_sales_orders_user FOREIGN KEY (tenant_id, created_by) REFERENCES tenant_users (tenant_id, id)
);
CREATE INDEX idx_sales_orders_tenant_date ON sales_orders (tenant_id, created_at DESC);
CREATE TABLE sales_order_items (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(14,3) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(14,2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(14,2) NOT NULL CHECK (line_total >= 0),
    CONSTRAINT fk_sales_items_order FOREIGN KEY (tenant_id, order_id) REFERENCES sales_orders (tenant_id, id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_items_product FOREIGN KEY (tenant_id, product_id) REFERENCES products (tenant_id, id)
);
CREATE INDEX idx_sales_items_order ON sales_order_items (tenant_id, order_id);
CREATE TABLE sales_payments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    order_id UUID NOT NULL,
    method VARCHAR(20) NOT NULL,
    amount NUMERIC(14,2) NOT NULL CHECK (amount > 0),
    reference VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sales_payments_order FOREIGN KEY (tenant_id, order_id) REFERENCES sales_orders (tenant_id, id) ON DELETE CASCADE
);
