package com.dukaops.backend.settings;

import java.util.UUID;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.branch.*;
import com.dukaops.backend.settings.dto.*;
import com.dukaops.backend.tenant.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SettingsIntegrationTest {
    @Autowired SettingsService settings;
    @Autowired ProfileService profiles;
    @Autowired SubscriptionSettingsController subscriptions;
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;
    @Autowired BranchRepository branches;
    @Autowired PasswordEncoder encoder;
    @Autowired EntityManager entityManager;
    private Tenant tenant;
    private TenantUser owner;
    private TenantUserPrincipal principal;

    @BeforeEach void setup() {
        tenant = tenants.save(new Tenant("Settings test", "settings-" + UUID.randomUUID()));
        owner = users.save(new TenantUser(tenant, "Test Owner", UUID.randomUUID() + "@example.com",
                encoder.encode("CurrentPassword123"), UserRole.OWNER));
        principal = actor(owner, UserRole.OWNER, tenant.getId());
    }
    @AfterEach void clearSecurity() { SecurityContextHolder.clearContext(); }
    private TenantUserPrincipal actor(TenantUser user, UserRole role, UUID tenantId) {
        return new TenantUserPrincipal(user.getId(), tenantId, user.getEmail(), user.getPasswordHash(), role, true);
    }
    private void reload() { entityManager.flush(); entityManager.clear(); }

    @Test void businessAndPreferencesPersistWithoutChangingIdentity() {
        var branch = branches.save(new Branch(tenant, "Main", "MAIN", true));
        settings.update(principal, new BusinessSettingsRequest("business@example.com", "12345", "Dar es Salaam",
                "TZS", "Africa/Dar_es_Salaam", "dd/MM/yyyy", branch.getId()));
        reload();
        var result = settings.get(principal);
        assertEquals(tenant.getId(), result.tenantId());
        assertEquals(tenant.getSlug(), result.slug());
        assertEquals("business@example.com", result.businessEmail());
        assertEquals(branch.getId(), result.defaultBranchId());
        settings.updatePreferences(principal, new PreferencesRequest("USD", "UTC", "yyyy-MM-dd", null));
        reload();
        assertEquals("USD", settings.preferences(principal).currency());
        assertEquals("business@example.com", settings.get(principal).businessEmail());
    }
    @Test void staffCannotUpdateBusinessSettings() {
        var staff = actor(owner, UserRole.STAFF, tenant.getId());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> settings.updatePreferences(staff,
                new PreferencesRequest("TZS", "UTC", "yyyy-MM-dd", null))).getStatusCode().value());
    }
    @Test void crossTenantDefaultBranchIsRejected() {
        var other = tenants.save(new Tenant("Other", "other-" + UUID.randomUUID()));
        var branch = branches.save(new Branch(other, "Other branch", "OTHER", true));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> settings.updatePreferences(principal,
                new PreferencesRequest("TZS", "UTC", "yyyy-MM-dd", branch.getId()))).getStatusCode().value());
    }
    @Test void profileUpdatePreservesRoleAndTenant() {
        profiles.update(principal, new ProfileRequest("Updated Name"));
        reload();
        assertEquals("Updated Name", profiles.get(principal).fullName());
        assertEquals("OWNER", profiles.get(principal).role());
        assertEquals(tenant.getId(), users.findById(owner.getId()).orElseThrow().getTenant().getId());
    }
    @Test void profileCannotBeReadUsingAnotherTenant() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> profiles.get(actor(owner, UserRole.OWNER, UUID.randomUUID()))).getStatusCode().value());
    }
    @Test void passwordChangePersistsEncodedPassword() {
        profiles.password(principal, new ChangePasswordRequest("CurrentPassword123", "NextPassword123"));
        reload();
        var hash = users.findById(owner.getId()).orElseThrow().getPasswordHash();
        assertTrue(encoder.matches("NextPassword123", hash));
        assertFalse(encoder.matches("CurrentPassword123", hash));
        assertNotEquals("NextPassword123", hash);
    }
    @Test void incorrectCurrentPasswordDoesNotChangeHash() {
        var original = owner.getPasswordHash();
        assertThrows(ResponseStatusException.class,
                () -> profiles.password(principal, new ChangePasswordRequest("WrongPassword", "NextPassword123")));
        assertEquals(original, owner.getPasswordHash());
    }
    @Test void subscriptionIsUnavailableForOwnerAndForbiddenForStaff() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        assertFalse(subscriptions.get().available());
        var staff = actor(owner, UserRole.STAFF, tenant.getId());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                staff, null, staff.getAuthorities()));
        assertThrows(AccessDeniedException.class, () -> subscriptions.get());
    }
}
