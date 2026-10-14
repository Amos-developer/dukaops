package com.dukaops.backend.tenant;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "business_email", length = 254) private String businessEmail;
    @Column(name = "business_phone", length = 40) private String businessPhone;
    @Column(name = "business_address", columnDefinition = "TEXT") private String businessAddress;
    @Column(nullable = false, length = 3) private String currency = "TZS";
    @Column(nullable = false, length = 80) private String timezone = "Africa/Dar_es_Salaam";
    @Column(name = "date_format", nullable = false, length = 30) private String dateFormat = "dd/MM/yyyy";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "default_branch_id") private com.dukaops.backend.branch.Branch defaultBranch;

    protected Tenant() {
    }

    public Tenant(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = TenantStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getBusinessEmail(){return businessEmail;} public String getBusinessPhone(){return businessPhone;} public String getBusinessAddress(){return businessAddress;} public String getCurrency(){return currency;} public String getTimezone(){return timezone;} public String getDateFormat(){return dateFormat;} public com.dukaops.backend.branch.Branch getDefaultBranch(){return defaultBranch;}
    public void updateSettings(String email,String phone,String address,String currency,String timezone,String dateFormat,com.dukaops.backend.branch.Branch branch){businessEmail=email;businessPhone=phone;businessAddress=address;this.currency=currency;this.timezone=timezone;this.dateFormat=dateFormat;defaultBranch=branch;}

}
