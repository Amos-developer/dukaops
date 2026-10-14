package com.dukaops.backend.auth;

import java.util.UUID;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TeamUserSearchIntegrationTest {
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;

    @Test void nullSearchListsStaffAndPreservesTenantIsolation() {
        var tenant = tenants.save(new Tenant("Search test", "search-" + UUID.randomUUID()));
        var other = tenants.save(new Tenant("Other business", "other-" + UUID.randomUUID()));
        var staff = users.saveAndFlush(new TenantUser(tenant, "Search Staff", UUID.randomUUID() + "@example.com", "unused-test-hash", UserRole.STAFF));
        users.saveAndFlush(new TenantUser(other, "Other Staff", UUID.randomUUID() + "@example.com", "unused-test-hash", UserRole.STAFF));
        var page = users.findTenantUsers(tenant.getId(), null, null, null, PageRequest.of(0, 1));
        assertEquals(1, page.getTotalElements());
        assertEquals(staff.getId(), page.getContent().getFirst().getId());
        assertEquals(1, users.findTenantUsers(tenant.getId(), "search", UserRole.STAFF, UserStatus.ACTIVE, PageRequest.of(0, 1)).getTotalElements());
        assertEquals(0, users.findTenantUsers(tenant.getId(), "missing", null, null, PageRequest.of(0, 1)).getTotalElements());
    }
}
