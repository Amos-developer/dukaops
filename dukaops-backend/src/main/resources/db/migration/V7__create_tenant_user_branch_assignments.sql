CREATE TABLE tenant_user_branch_assignments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL
        REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL
        REFERENCES tenant_users(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL
        REFERENCES branches(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_branch_assignment UNIQUE (user_id, branch_id)
);

CREATE INDEX idx_tenant_user_branch_assignments_tenant_id
    ON tenant_user_branch_assignments (tenant_id);

CREATE INDEX idx_tenant_user_branch_assignments_user_id
    ON tenant_user_branch_assignments (user_id);

CREATE INDEX idx_tenant_user_branch_assignments_branch_id
    ON tenant_user_branch_assignments (branch_id);
