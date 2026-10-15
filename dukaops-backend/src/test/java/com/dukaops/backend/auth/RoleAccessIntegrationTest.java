package com.dukaops.backend.auth;

import java.math.BigDecimal;
import java.util.UUID;
import com.dukaops.backend.branch.*;
import com.dukaops.backend.sales.*;
import com.dukaops.backend.tenant.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoleAccessIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;
    @Autowired BranchRepository branches;
    @Autowired TenantUserBranchAssignmentRepository assignments;
    @Autowired SalesOrderRepository orders;
    @Autowired SalesService sales;
    @Autowired AccessPolicy policy;
    private Tenant tenant;
    private Branch assigned;
    private Branch hidden;
    private Branch foreign;
    private TenantUser staff;
    private TenantUser manager;
    private TenantUser owner;
    private TenantUser admin;

    @BeforeEach void setup() {
        tenant = tenants.saveAndFlush(new Tenant("Role tests", "roles-" + UUID.randomUUID()));
        var other = tenants.saveAndFlush(new Tenant("Other tenant", "roles-other-" + UUID.randomUUID()));
        assigned = branches.saveAndFlush(new Branch(tenant, "Assigned", "ASSIGNED", true));
        hidden = branches.saveAndFlush(new Branch(tenant, "Hidden", "HIDDEN", false));
        foreign = branches.saveAndFlush(new Branch(other, "Foreign", "FOREIGN", true));
        staff = account(tenant, UserRole.STAFF);
        manager = account(tenant, UserRole.MANAGER);
        owner = account(tenant, UserRole.OWNER);
        admin = account(tenant, UserRole.ADMIN);
        assignments.saveAndFlush(new TenantUserBranchAssignment(tenant, staff, assigned));
        assignments.saveAndFlush(new TenantUserBranchAssignment(tenant, manager, assigned));
        orders.saveAndFlush(new SalesOrder(tenant, assigned, owner, "Customer", null, OrderStatus.COMPLETED, new BigDecimal("10.00"), UUID.randomUUID().toString()));
        orders.saveAndFlush(new SalesOrder(tenant, hidden, owner, "Hidden customer", null, OrderStatus.COMPLETED, new BigDecimal("90.00"), UUID.randomUUID().toString()));
        var otherOwner = account(other, UserRole.OWNER);
        orders.saveAndFlush(new SalesOrder(other, foreign, otherOwner, "Foreign customer", null, OrderStatus.COMPLETED, new BigDecimal("500.00"), UUID.randomUUID().toString()));
    }
    private TenantUser account(Tenant business, UserRole role) {
        return users.saveAndFlush(new TenantUser(business, "Test " + role, UUID.randomUUID() + "@example.com", "unused-test-password-hash", role));
    }
    private TenantUserPrincipal principal(TenantUser account) {
        return new TenantUserPrincipal(account.getId(), account.getTenant().getId(), account.getEmail(), account.getPasswordHash(), account.getRole(), true);
    }
    @Test void staffViewsAssignedStockButCannotModifyIt() throws Exception {
        mvc.perform(get("/api/inventory/branches/" + assigned.getId() + "/stock").with(user(principal(staff)))).andExpect(status().isOk());
        mvc.perform(get("/api/inventory/branches/" + hidden.getId() + "/stock").with(user(principal(staff)))).andExpect(status().isForbidden());
        mvc.perform(post("/api/inventory/receipts").with(user(principal(staff))).contentType("application/json").content("{}")).andExpect(status().isForbidden());
    }
    @Test void salesListsAndSummariesRespectAssignmentsAndTenant() {
        for (var account : java.util.List.of(staff, manager)) {
            assertEquals(1, sales.list(principal(account), null, null, 0, 10).getTotalElements());
            assertEquals(0, new BigDecimal("10.00").compareTo(sales.summary(principal(account)).revenue()));
        }
        assertEquals(2, sales.list(principal(owner), null, null, 0, 10).getTotalElements());
        assertEquals(0, new BigDecimal("100.00").compareTo(sales.summary(principal(admin)).revenue()));
    }
    @Test void ownerCannotAccessAnotherTenantBranch() throws Exception {
        mvc.perform(get("/api/tenants/" + tenant.getId() + "/branches/" + foreign.getId()).with(user(principal(owner)))).andExpect(status().isNotFound());
    }
    @Test void managerCanEditAssignedBranchOnly() throws Exception {
        mvc.perform(put("/api/tenants/" + tenant.getId() + "/branches/" + assigned.getId()).with(user(principal(manager))).contentType("application/json").content("{\"name\":\"Renamed\",\"code\":\"ASSIGNED\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/tenants/" + tenant.getId() + "/branches/" + hidden.getId()).with(user(principal(manager))).contentType("application/json").content("{\"name\":\"No access\",\"code\":\"HIDDEN\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/team/users").with(user(principal(manager)))).andExpect(status().isForbidden());
    }
    @Test void onlyOwnerCanAccessBillingAndDeleteProducts() throws Exception {
        mvc.perform(get("/api/settings/subscription").with(user(principal(owner)))).andExpect(status().isOk());
        mvc.perform(get("/api/settings/subscription").with(user(principal(admin)))).andExpect(status().isForbidden());
        mvc.perform(delete("/api/products/" + UUID.randomUUID()).with(user(principal(admin)))).andExpect(status().isForbidden());
    }
    @Test void adminManagesUsersButCannotAssignOwnership() throws Exception {
        mvc.perform(get("/api/team/users").with(user(principal(admin)))).andExpect(status().isOk());
        mvc.perform(patch("/api/team/users/" + staff.getId() + "/role").with(user(principal(admin))).contentType("application/json").content("{\"role\":\"OWNER\"}")).andExpect(status().isForbidden());
    }
    @Test void staffCannotReadReportsAndManagerMustSelectAssignedBranch() throws Exception {
        mvc.perform(get("/api/reports/overview").with(user(principal(staff)))).andExpect(status().isForbidden());
        mvc.perform(get("/api/reports/overview").with(user(principal(manager)))).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/overview").param("branchId", hidden.getId().toString()).with(user(principal(manager)))).andExpect(status().isForbidden());
        mvc.perform(get("/api/reports/sales").param("branchId", assigned.getId().toString()).with(user(principal(manager)))).andExpect(status().isOk());
    }
    @Test void tenantEnumerationAndAnonymousAccessAreBlocked() throws Exception {
        mvc.perform(get("/api/tenants").with(user(principal(owner)))).andExpect(status().isForbidden());
        mvc.perform(get("/api/team/users")).andExpect(status().isUnauthorized());
    }
    @Test void staffCannotCheckoutInUnassignedBranch() throws Exception {
        String payload = "{\"branchId\":\"" + hidden.getId() + "\",\"paymentMethod\":\"CASH\",\"idempotencyKey\":\"" + UUID.randomUUID()
                + "\",\"items\":[{\"productId\":\"" + UUID.randomUUID() + "\",\"quantity\":1}]}";
        mvc.perform(post("/api/sales/checkout").with(user(principal(staff))).contentType("application/json").content(payload)).andExpect(status().isForbidden());
    }
}
