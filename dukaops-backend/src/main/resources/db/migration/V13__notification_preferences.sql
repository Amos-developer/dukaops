CREATE TABLE tenant_notification_preferences (
    tenant_id UUID PRIMARY KEY REFERENCES tenants(id),
    low_stock_alerts BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    email_notifications BOOLEAN NOT NULL DEFAULT FALSE
);
