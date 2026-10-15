CREATE TABLE customer_sms (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    customer_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    request_key UUID NOT NULL,
    recipient VARCHAR(16) NOT NULL,
    body VARCHAR(480) NOT NULL,
    provider_sid VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, request_key),
    FOREIGN KEY (tenant_id, actor_id) REFERENCES tenant_users(tenant_id, id),
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);
CREATE INDEX idx_customer_sms_tenant_created ON customer_sms(tenant_id, created_at DESC);
