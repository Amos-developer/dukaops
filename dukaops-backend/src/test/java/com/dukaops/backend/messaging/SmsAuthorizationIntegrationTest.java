package com.dukaops.backend.messaging;

import java.util.UUID;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.tenant.*;
import com.dukaops.backend.contact.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class SmsAuthorizationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CustomerSmsService service;
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;
    @Autowired CustomerRepository customers;
    @MockitoBean TwilioGateway gateway;
    TenantUserPrincipal principal(UserRole role) {
        return new TenantUserPrincipal(UUID.randomUUID(), UUID.randomUUID(), "sms@example.com", "unused", role, true);
    }
    @Test void anonymousCannotReadSmsConfiguration() throws Exception {
        mvc.perform(get("/api/messaging/sms/configuration")).andExpect(status().isUnauthorized());
    }
    @Test void operationalRolesCannotAccessCustomerMessages() throws Exception {
        for (var role : new UserRole[] { UserRole.STAFF, UserRole.MANAGER }) {
            mvc.perform(get("/api/messaging/sms").with(user(principal(role)))).andExpect(status().isForbidden());
        }
    }
    @Test void adminCanInspectConfigurationWithoutSecrets() throws Exception {
        mvc.perform(get("/api/messaging/sms/configuration").with(user(principal(UserRole.ADMIN))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.provider").value("Twilio"))
                .andExpect(jsonPath("$.authToken").doesNotExist());
    }

    @Test @Transactional void duplicateSubmissionSendsOnlyOnceAndHistoryIsIsolated() {
        var tenant = tenants.saveAndFlush(new Tenant("SMS tests", "sms-" + UUID.randomUUID()));
        var owner = users.saveAndFlush(new TenantUser(tenant, "SMS owner", UUID.randomUUID() + "@example.com", "unused-test-hash", UserRole.OWNER));
        var customer = customers.saveAndFlush(new Customer(tenant, "Customer", "0712345678", null, null, null));
        var actor = new TenantUserPrincipal(owner.getId(), tenant.getId(), owner.getEmail(), owner.getPasswordHash(), owner.getRole(), true);
        when(gateway.configured()).thenReturn(true);
        when(gateway.send("+255712345678", "Order ready")).thenReturn(new TwilioGateway.Result("SM" + "1".repeat(32), "queued"));
        UUID key = UUID.randomUUID();
        var first = service.send(actor, customer.getId(), key, "Order ready");
        var second = service.send(actor, customer.getId(), key, "Order ready");
        assertEquals(first.get("id"), second.get("id"));
        assertEquals("queued", first.get("status"));
        verify(gateway, times(1)).send(anyString(), anyString());
        assertEquals(1, service.history(actor, 0).size());
        var stranger = principal(UserRole.OWNER);
        assertTrue(service.history(stranger, 0).isEmpty());
        assertEquals(404, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.refresh(stranger, (UUID) first.get("id"))).getStatusCode().value());
        assertEquals(409, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.send(actor, customer.getId(), key, "Different message")).getStatusCode().value());
    }

    @Test @Transactional void crossTenantCustomerNeverReachesTwilio() {
        var tenant = tenants.saveAndFlush(new Tenant("SMS tests", "sms-" + UUID.randomUUID()));
        var owner = users.saveAndFlush(new TenantUser(tenant, "SMS owner", UUID.randomUUID() + "@example.com", "unused-test-hash", UserRole.OWNER));
        var other = tenants.saveAndFlush(new Tenant("Other", "sms-other-" + UUID.randomUUID()));
        var foreign = customers.saveAndFlush(new Customer(other, "Foreign", "0712345678", null, null, null));
        var actor = new TenantUserPrincipal(owner.getId(), tenant.getId(), owner.getEmail(), owner.getPasswordHash(), owner.getRole(), true);
        when(gateway.configured()).thenReturn(true);
        assertEquals(404, assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.send(actor, foreign.getId(), UUID.randomUUID(), "Order ready")).getStatusCode().value());
        verify(gateway, never()).send(anyString(), anyString());
    }
}
