ALTER TABLE tenants ADD COLUMN business_email VARCHAR(254);
ALTER TABLE tenants ADD COLUMN business_phone VARCHAR(40);
ALTER TABLE tenants ADD COLUMN business_address TEXT;
ALTER TABLE tenants ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'TZS';
ALTER TABLE tenants ADD COLUMN timezone VARCHAR(80) NOT NULL DEFAULT 'Africa/Dar_es_Salaam';
ALTER TABLE tenants ADD COLUMN date_format VARCHAR(30) NOT NULL DEFAULT 'dd/MM/yyyy';
ALTER TABLE tenants ADD COLUMN default_branch_id UUID;
ALTER TABLE tenants ADD CONSTRAINT fk_tenants_default_branch FOREIGN KEY (default_branch_id) REFERENCES branches(id) ON DELETE SET NULL;
