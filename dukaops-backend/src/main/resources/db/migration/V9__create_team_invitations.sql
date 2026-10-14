CREATE TABLE team_invitations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    invited_by UUID NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(254) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'STAFF')),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_invitation_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_invitation_inviter FOREIGN KEY (tenant_id, invited_by) REFERENCES tenant_users(tenant_id, id) ON DELETE CASCADE
);

CREATE TABLE team_invitation_branches (
    invitation_id UUID NOT NULL REFERENCES team_invitations(id) ON DELETE CASCADE,
    branch_id UUID NOT NULL,
    PRIMARY KEY (invitation_id, branch_id),
    CONSTRAINT fk_team_invitation_branch FOREIGN KEY (branch_id) REFERENCES branches(id) ON DELETE CASCADE
);

CREATE INDEX idx_team_invitations_tenant_email ON team_invitations (tenant_id, LOWER(email));
CREATE INDEX idx_team_invitations_expires_at ON team_invitations (expires_at);
