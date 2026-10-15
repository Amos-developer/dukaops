package com.dukaops.backend.auth;

import java.math.BigDecimal;
import java.util.UUID;
import com.dukaops.backend.tenant.*;
import com.dukaops.backend.branch.*;
import com.dukaops.backend.product.*;
import com.dukaops.backend.product.dto.ProductRequest;
import com.dukaops.backend.inventory.*;
import com.dukaops.backend.branch.inventory.StockLevel;
import com.dukaops.backend.branch.inventory.repository.StockLevelRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityManager;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SensitiveActionsIntegrationTest {
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;
    @Autowired ProductRepository products;
    @Autowired BranchRepository branches;
    @Autowired StockLevelRepository stock;
    @Autowired ProductService productService;
    @Autowired InventoryService inventory;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;
    private Tenant tenant;
    private TenantUser owner;
    private Product product;
    private Branch branch;
    @BeforeEach void setup() {
        tenant = tenants.saveAndFlush(new Tenant("Audit test", "audit-" + UUID.randomUUID()));
        owner = users.saveAndFlush(new TenantUser(tenant, "Owner", UUID.randomUUID() + "@example.com", "test-only-hash", UserRole.OWNER));
        branch = branches.saveAndFlush(new Branch(tenant, "Main", "MAIN", true));
        product = products.saveAndFlush(new Product(tenant, "ITEM", "Item", null, new BigDecimal("10"), new BigDecimal("5")));
        var level = new StockLevel(tenant, branch, product, BigDecimal.ZERO);
        level.applyQuantityChange(new BigDecimal("20")); stock.saveAndFlush(level);
        signIn(UserRole.OWNER);
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    private void signIn(UserRole role) {
        var principal = new TenantUserPrincipal(owner.getId(), tenant.getId(), owner.getEmail(), owner.getPasswordHash(), role, true);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
    private long events(String action) {
        entityManager.flush();
        return jdbc.queryForObject("select count(*) from security_audit_events where tenant_id=? and actor_id=? and action=?", Long.class, tenant.getId(), owner.getId(), action);
    }
    @Test void costChangesRecordBeforeAfterAndActor() {
        productService.update(tenant.getId(), product.getId(), new ProductRequest("ITEM", "Item", null, new BigDecimal("10"), new BigDecimal("7")));
        assertEquals(1, events("PRODUCT_COST_CHANGED"));
        assertEquals("5", jdbc.queryForObject("select before_value from security_audit_events where tenant_id=? and action='PRODUCT_COST_CHANGED'", String.class, tenant.getId()));
        assertEquals("7", jdbc.queryForObject("select after_value from security_audit_events where tenant_id=? and action='PRODUCT_COST_CHANGED'", String.class, tenant.getId()));
    }
    @Test void managerCannotChangeGlobalProductCosts() {
        signIn(UserRole.MANAGER);
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> productService.update(tenant.getId(), product.getId(), new ProductRequest("ITEM", "Item", null, BigDecimal.TEN, BigDecimal.ONE))).getStatusCode().value());
        assertEquals(0, events("PRODUCT_COST_CHANGED"));
    }
    @Test void adjustmentsRecordBranchQuantitiesAndReason() {
        inventory.adjustStock(tenant.getId(), owner.getId(), UserRole.OWNER,
                new AdjustStockRequest(branch.getId(), product.getId(), new BigDecimal("-2"), "COUNT", "Physical count correction"));
        assertEquals(1, events("STOCK_ADJUSTED"));
        assertEquals(branch.getId(), jdbc.queryForObject("select branch_id from security_audit_events where tenant_id=? and action='STOCK_ADJUSTED'", UUID.class, tenant.getId()));
    }
    @Test void lastActiveOwnerCannotBeDemotedInDatabase() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("update tenant_users set role='STAFF' where id=?", owner.getId()));
    }
    @Test void lastActiveOwnerCannotBeSuspendedInDatabase() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("update tenant_users set status='SUSPENDED' where id=?", owner.getId()));
    }
    @Test void lastActiveOwnerCannotBeDeletedInDatabase() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("delete from tenant_users where id=?", owner.getId()));
    }
}
