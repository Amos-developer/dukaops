-- Enforce that a branch assignment cannot connect records from different tenants.
-- V7 already validates this in the service; these composite FKs make it a
-- database invariant as well.
ALTER TABLE tenant_user_branch_assignments
    ADD CONSTRAINT fk_team_assignment_user_tenant
    FOREIGN KEY (tenant_id, user_id)
    REFERENCES tenant_users (tenant_id, id)
    ON DELETE CASCADE;

ALTER TABLE tenant_user_branch_assignments
    ADD CONSTRAINT fk_team_assignment_branch_tenant
    FOREIGN KEY (tenant_id, branch_id)
    REFERENCES branches (tenant_id, id)
    ON DELETE CASCADE;
