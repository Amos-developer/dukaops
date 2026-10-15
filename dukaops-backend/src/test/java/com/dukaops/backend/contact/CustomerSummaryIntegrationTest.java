package com.dukaops.backend.contact;

import java.util.UUID;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.tenant.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerSummaryIntegrationTest {
    @Autowired CustomerSummaryService service;
    @Autowired CustomerRepository customers;
    @Autowired TenantRepository tenants;
    @Autowired MockMvc mvc;
    Tenant tenant, other;
    TenantUserPrincipal actor(Tenant business, UserRole role) {
        return new TenantUserPrincipal(UUID.randomUUID(), business.getId(), "count@example.com", "unused", role, true);
    }
    @BeforeEach void setup() {
        tenant = tenants.saveAndFlush(new Tenant("Customers", "customers-count-" + UUID.randomUUID()));
        other = tenants.saveAndFlush(new Tenant("Other", "customers-count-other-" + UUID.randomUUID()));
    }
    @Test void countIncludesActiveAndInactiveCustomersButNeverOtherTenants() throws Exception {
        customers.saveAndFlush(new Customer(tenant, "Active", null, null, null, null));
        var inactive = new Customer(tenant, "Inactive", null, null, null, null);
        inactive.setStatus(ContactStatus.INACTIVE); customers.saveAndFlush(inactive);
        customers.saveAndFlush(new Customer(other, "Foreign", null, null, null, null));
        assertEquals(2, service.summary(actor(tenant, UserRole.OWNER)).totalCustomers());
        assertEquals(2, service.summary(actor(tenant, UserRole.ADMIN)).totalCustomers());
        mvc.perform(get("/api/contacts/customers/summary").with(user(actor(tenant, UserRole.OWNER))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalCustomers").value(2));
    }
    @Test void emptyBusinessReturnsZero() throws Exception {
        mvc.perform(get("/api/contacts/customers/summary").with(user(actor(tenant, UserRole.ADMIN))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalCustomers").value(0));
    }
    @Test void anonymousAndOperationalRolesCannotReadCustomerCounts() throws Exception {
        mvc.perform(get("/api/contacts/customers/summary")).andExpect(status().isUnauthorized());
        for (var role : new UserRole[] { UserRole.MANAGER, UserRole.STAFF }) {
            mvc.perform(get("/api/contacts/customers/summary").with(user(actor(tenant, role))))
                    .andExpect(status().isForbidden());
            assertEquals(403, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                    () -> service.summary(actor(tenant, role))).getStatusCode().value());
        }
    }
}
