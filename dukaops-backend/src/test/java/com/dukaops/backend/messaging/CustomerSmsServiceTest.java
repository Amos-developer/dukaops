package com.dukaops.backend.messaging;

import java.util.*;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.contact.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerSmsServiceTest {
    final CustomerRepository customers = mock(CustomerRepository.class);
    final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    final TransactionTemplate transactions = mock(TransactionTemplate.class);
    final TwilioGateway gateway = mock(TwilioGateway.class);
    final CustomerSmsService service = new CustomerSmsService(customers, jdbc, transactions, gateway);

    TenantUserPrincipal actor(UserRole role) {
        return new TenantUserPrincipal(UUID.randomUUID(), UUID.randomUUID(), "sms@example.com", "unused", role, true);
    }

    @Test void normalizesTanzanianMobileNumbers() {
        assertEquals("+255712345678", CustomerSmsService.normalizePhone("0712 345 678"));
        assertEquals("+255712345678", CustomerSmsService.normalizePhone("255712345678"));
        assertEquals("+255612345678", CustomerSmsService.normalizePhone("+255612345678"));
        for (String phone : List.of("+254712345678", "+2550712345678", "abc", ""))
            assertThrows(ResponseStatusException.class, () -> CustomerSmsService.normalizePhone(phone));
    }

    @Test void onlyBusinessAdministratorsCanUseSms() {
        for (var role : List.of(UserRole.STAFF, UserRole.MANAGER)) {
            var denied = assertThrows(ResponseStatusException.class, () -> service.configuration(actor(role)));
            assertEquals(403, denied.getStatusCode().value());
        }
        assertEquals(false, service.configuration(actor(UserRole.OWNER)).get("configured"));
        assertEquals(false, service.configuration(actor(UserRole.ADMIN)).get("configured"));
        verifyNoInteractions(jdbc, customers);
    }

    @Test void disabledConfigurationNeverSends() {
        var error = assertThrows(ResponseStatusException.class, () -> service.send(actor(UserRole.OWNER), UUID.randomUUID(), UUID.randomUUID(), "Order ready"));
        assertEquals(503, error.getStatusCode().value());
        verify(gateway, never()).send(anyString(), anyString());
        verifyNoInteractions(jdbc);
    }

    @Test void historyIsTenantScopedAndLimitedToTen() {
        var owner = actor(UserRole.OWNER);
        service.history(owner, 2);
        verify(jdbc).queryForList(contains("WHERE tenant_id=? ORDER BY created_at DESC, id LIMIT 10 OFFSET ?"), eq(owner.getTenantId()), eq(20));
    }

    @Test void foreignMessageCannotBeReadOrRefreshed() {
        var owner = actor(UserRole.OWNER);
        UUID id = UUID.randomUUID();
        when(jdbc.queryForList(anyString(), eq(owner.getTenantId()), eq(id))).thenReturn(List.of());
        var error = assertThrows(ResponseStatusException.class, () -> service.refresh(owner, id));
        assertEquals(404, error.getStatusCode().value());
        verifyNoInteractions(gateway);
    }
}
