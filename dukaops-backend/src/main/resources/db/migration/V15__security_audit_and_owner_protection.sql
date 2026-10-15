CREATE TABLE security_audit_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    actor_id UUID NOT NULL,
    branch_id UUID,
    action VARCHAR(60) NOT NULL,
    resource_id UUID NOT NULL,
    before_value TEXT,
    after_value TEXT,
    reason VARCHAR(2000),
    created_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (tenant_id, actor_id) REFERENCES tenant_users(tenant_id, id),
    FOREIGN KEY (tenant_id, branch_id) REFERENCES branches(tenant_id, id)
);
CREATE INDEX idx_security_audit_tenant_date ON security_audit_events(tenant_id, created_at DESC);

CREATE FUNCTION protect_last_active_owner() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.role = 'OWNER' AND OLD.status = 'ACTIVE' THEN
        IF TG_OP = 'DELETE' THEN
            PERFORM id FROM tenants WHERE id = OLD.tenant_id FOR UPDATE;
            IF NOT EXISTS (SELECT 1 FROM tenant_users WHERE tenant_id = OLD.tenant_id
                AND id <> OLD.id AND role = 'OWNER' AND status = 'ACTIVE') THEN
                RAISE EXCEPTION 'The last active owner cannot be removed' USING ERRCODE = '23514';
            END IF;
        ELSIF NEW.role <> 'OWNER' OR NEW.status <> 'ACTIVE' OR NEW.tenant_id <> OLD.tenant_id THEN
            PERFORM id FROM tenants WHERE id = OLD.tenant_id FOR UPDATE;
            IF NOT EXISTS (SELECT 1 FROM tenant_users WHERE tenant_id = OLD.tenant_id
                AND id <> OLD.id AND role = 'OWNER' AND status = 'ACTIVE') THEN
                RAISE EXCEPTION 'The last active owner cannot be demoted or suspended' USING ERRCODE = '23514';
            END IF;
        END IF;
    END IF;
    IF TG_OP = 'DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER guard_last_active_owner BEFORE UPDATE OR DELETE ON tenant_users
    FOR EACH ROW EXECUTE FUNCTION protect_last_active_owner();
