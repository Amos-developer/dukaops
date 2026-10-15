-- Keep V16 unchanged once applied. Strengthen tenant integrity additively.
ALTER TABLE customers
    ADD CONSTRAINT uq_customers_tenant_id UNIQUE (tenant_id, id);

ALTER TABLE customer_sms
    ADD CONSTRAINT fk_customer_sms_tenant_customer
    FOREIGN KEY (tenant_id, customer_id) REFERENCES customers(tenant_id, id);

-- The composite foreign key above also protects customer existence.
ALTER TABLE customer_sms DROP CONSTRAINT customer_sms_customer_id_fkey;
