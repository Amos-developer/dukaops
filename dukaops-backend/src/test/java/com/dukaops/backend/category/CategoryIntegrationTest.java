package com.dukaops.backend.category;

import java.math.BigDecimal;
import java.util.UUID;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.branch.*;
import com.dukaops.backend.branch.inventory.StockLevel;
import com.dukaops.backend.branch.inventory.repository.StockLevelRepository;
import com.dukaops.backend.product.*;
import com.dukaops.backend.product.dto.ProductRequest;
import com.dukaops.backend.tenant.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryIntegrationTest {
    @Autowired CategoryService categories;
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;
    @Autowired BranchRepository branches;
    @Autowired TenantUserBranchAssignmentRepository assignments;
    @Autowired ProductRepository products;
    @Autowired ProductService productService;
    @Autowired StockLevelRepository stock;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    Tenant tenant, other;
    Branch main, second, foreignBranch;
    TenantUserPrincipal owner, admin, manager, staff, foreignOwner;

    TenantUserPrincipal account(Tenant business, UserRole role) {
        var user = users.saveAndFlush(new TenantUser(business, "Category test", UUID.randomUUID() + "@example.com", "test-only-hash", role));
        return new TenantUserPrincipal(user.getId(), business.getId(), user.getEmail(), user.getPasswordHash(), role, true);
    }
    @BeforeEach void setup() {
        tenant = tenants.saveAndFlush(new Tenant("Categories", "categories-" + UUID.randomUUID()));
        other = tenants.saveAndFlush(new Tenant("Other", "categories-other-" + UUID.randomUUID()));
        owner = account(tenant, UserRole.OWNER); admin = account(tenant, UserRole.ADMIN);
        manager = account(tenant, UserRole.MANAGER); staff = account(tenant, UserRole.STAFF); foreignOwner = account(other, UserRole.OWNER);
        main = branches.saveAndFlush(new Branch(tenant, "Main", "MAIN", true));
        second = branches.saveAndFlush(new Branch(tenant, "Second", "SECOND", false));
        foreignBranch = branches.saveAndFlush(new Branch(other, "Foreign", "FOREIGN", true));
        for (var actor : new TenantUserPrincipal[] { manager, staff }) {
            var user = users.findById(actor.getUserId()).orElseThrow();
            assignments.saveAndFlush(new TenantUserBranchAssignment(tenant, user, main));
        }
        signIn(owner);
    }
    void signIn(TenantUserPrincipal actor) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities()));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    void amount(String expected, BigDecimal actual) { assertEquals(0, new BigDecimal(expected).compareTo(actual)); }
    Product product(Tenant business, UUID categoryId, BigDecimal cost) {
        var product = new Product(business, UUID.randomUUID().toString(), "Item", null, BigDecimal.TEN, cost);
        product.setCategoryId(categoryId);
        return products.saveAndFlush(product);
    }
    void stock(Tenant business, Branch branch, Product product, String quantity) {
        var level = new StockLevel(business, branch, product, BigDecimal.ZERO);
        level.applyQuantityChange(new BigDecimal(quantity)); stock.saveAndFlush(level);
    }

    @Test void administratorsManageNamesAndStatusWithinTenant() {
        var category = categories.save(owner, null, new CategoryService.Request(" Drinks ", true));
        assertEquals("Drinks", category.name());
        var updated = categories.save(admin, category.id(), new CategoryService.Request("Beverages", false));
        assertFalse(updated.active());
        assertTrue(categories.list(foreignOwner).isEmpty());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> categories.save(foreignOwner, category.id(), new CategoryService.Request("Stolen", true))).getStatusCode().value());
    }
    @Test void rejectsDuplicateAndInvalidNames() {
        categories.save(owner, null, new CategoryService.Request("Drinks", true));
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> categories.save(owner, null, new CategoryService.Request(" drinks ", true))).getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> categories.save(owner, null, new CategoryService.Request("   ", true))).getStatusCode().value());
        // Identical names in different tenants are valid.
        assertNotNull(categories.save(foreignOwner, null, new CategoryService.Request("Drinks", true)));
    }
    @Test void productCreateUpdateAndUnassignUseRealCategory() {
        var category = categories.save(owner, null, new CategoryService.Request("Drinks", true));
        var product = productService.create(tenant.getId(), new ProductRequest("NEW", "New", null, BigDecimal.TEN, BigDecimal.ONE, category.id()));
        assertEquals(category.id(), product.categoryId());
        var changed = productService.update(tenant.getId(), product.id(), new ProductRequest("NEW", "New", null, BigDecimal.TEN, BigDecimal.ONE, null));
        assertNull(changed.categoryId());
    }
    @Test void foreignAndArchivedCategoriesCannotBeAssigned() {
        var foreign = categories.save(foreignOwner, null, new CategoryService.Request("Foreign", true));
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> categories.validateAssignment(tenant.getId(), foreign.id(), null)).getStatusCode().value());
        var archived = categories.save(owner, null, new CategoryService.Request("Archived", false));
        assertEquals(400, assertThrows(ResponseStatusException.class, () -> categories.validateAssignment(tenant.getId(), archived.id(), null)).getStatusCode().value());
        // Existing assignments remain valid when editing another product field.
        assertDoesNotThrow(() -> categories.validateAssignment(tenant.getId(), archived.id(), archived.id()));
    }
    @Test void quantityAndCostTotalsRespectBranchAndTenant() {
        var category = categories.save(owner, null, new CategoryService.Request("Drinks", true));
        var item = product(tenant, category.id(), new BigDecimal("2.50"));
        stock(tenant, main, item, "10"); stock(tenant, second, item, "20");
        stock(tenant, main, product(tenant, null, null), "3");
        stock(other, foreignBranch, product(other, null, BigDecimal.TEN), "999");
        var total = categories.inventory(owner, null, "quantity");
        amount("33", total.totalQuantity()); amount("75", total.totalValue()); amount("3", total.unvaluedQuantity());
        assertEquals(2, total.categories().size());
        assertTrue(total.categories().stream().anyMatch(slice -> slice.categoryId() == null && slice.name().equals("Uncategorized")));
        var branch = categories.inventory(owner, main.getId(), "value");
        amount("13", branch.totalQuantity()); amount("25", branch.totalValue());
        assertEquals(404, assertThrows(ResponseStatusException.class, () -> categories.inventory(owner, foreignBranch.getId(), "quantity")).getStatusCode().value());
    }
    @Test void managerAndStaffSeeOnlyAssignedBranchesAndStaffCannotReadValues() {
        var item = product(tenant, null, BigDecimal.ONE);
        stock(tenant, main, item, "4"); stock(tenant, second, item, "100");
        amount("4", categories.inventory(manager, null, "quantity").totalQuantity());
        amount("4", categories.inventory(staff, null, "quantity").totalQuantity());
        assertNull(categories.inventory(staff, null, "quantity").totalValue());
        assertNull(categories.inventory(staff, null, "quantity").categories().getFirst().value());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> categories.inventory(manager, second.getId(), "quantity")).getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> categories.inventory(staff, null, "value")).getStatusCode().value());
    }
    @Test void emptyInventoryIsZeroNotInventedData() {
        var distribution = categories.inventory(owner, null, "quantity");
        assertTrue(distribution.categories().isEmpty()); amount("0", distribution.totalQuantity()); amount("0", distribution.totalValue());
    }
    @Test void apiRejectsUnauthorizedAndInvalidWrites() throws Exception {
        mvc.perform(get("/api/categories").with(anonymous())).andExpect(status().isUnauthorized());
        for (var actor : new TenantUserPrincipal[] { manager, staff }) {
            mvc.perform(post("/api/categories").with(user(actor)).contentType("application/json").content("{\"name\":\"Drinks\",\"active\":true}")).andExpect(status().isForbidden());
            assertEquals(403, assertThrows(ResponseStatusException.class, () -> categories.save(actor, null, new CategoryService.Request("Drinks", true))).getStatusCode().value());
        }
        mvc.perform(post("/api/categories").with(user(owner)).contentType("application/json").content("{\"name\":\"\",\"active\":true}")).andExpect(status().isBadRequest());
    }
    @Test void databaseRejectsCrossTenantCategoryAssignment() {
        var foreign = categories.save(foreignOwner, null, new CategoryService.Request("Foreign", true));
        var item = product(tenant, null, BigDecimal.ONE);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE products SET category_id=? WHERE tenant_id=? AND id=?", foreign.id(), tenant.getId(), item.getId()));
    }
}
