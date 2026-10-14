package com.dukaops.backend.settings;

import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "tenant_notification_preferences")
public class NotificationPreferences {
    @Id @Column(name = "tenant_id") private UUID tenantId;
    @Column(name = "low_stock_alerts", nullable = false) private boolean lowStockAlerts = true;
    @Column(name = "in_app_notifications", nullable = false) private boolean inAppNotifications = true;
    @Column(name = "email_notifications", nullable = false) private boolean emailNotifications;
    protected NotificationPreferences() {}
    public NotificationPreferences(UUID tenantId) { this.tenantId = tenantId; }
    public boolean isLowStockAlerts() { return lowStockAlerts; }
    public boolean isInAppNotifications() { return inAppNotifications; }
    public boolean isEmailNotifications() { return emailNotifications; }
    public void update(boolean lowStockAlerts, boolean inAppNotifications, boolean emailNotifications) {
        this.lowStockAlerts = lowStockAlerts;
        this.inAppNotifications = inAppNotifications;
        this.emailNotifications = emailNotifications;
    }
}
