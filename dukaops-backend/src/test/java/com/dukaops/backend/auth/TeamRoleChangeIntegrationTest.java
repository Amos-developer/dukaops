package com.dukaops.backend.auth;

import java.util.*;
import com.dukaops.backend.auth.dto.*;
import com.dukaops.backend.branch.*;
import com.dukaops.backend.tenant.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TeamRoleChangeIntegrationTest {
    @Autowired TeamUserService service;
    @Autowired TenantRepository tenants;
    @Autowired TenantUserRepository users;
    @Autowired BranchRepository branches;
    @Autowired TenantUserBranchAssignmentRepository assignments;
    @Autowired EntityManager em;
    Tenant tenant;
    TenantUser staff;
    TenantUserPrincipal owner;
    Branch first, second;
    @BeforeEach void setup() {
        tenant = tenants.saveAndFlush(new Tenant("Role update", "role-update-" + UUID.randomUUID()));
        var account = users.saveAndFlush(new TenantUser(tenant, "Owner", UUID.randomUUID() + "@example.com", "test-only-hash", UserRole.OWNER));
        owner = new TenantUserPrincipal(account.getId(), tenant.getId(), account.getEmail(), account.getPasswordHash(), account.getRole(), true);
        staff = users.saveAndFlush(new TenantUser(tenant, "Staff", UUID.randomUUID() + "@example.com", "test-only-hash", UserRole.STAFF));
        first = branches.saveAndFlush(new Branch(tenant, "First", "FIRST", true));
        second = branches.saveAndFlush(new Branch(tenant, "Second", "SECOND", false));
        assignments.saveAndFlush(new TenantUserBranchAssignment(tenant, staff, first));
        em.clear();
    }
    @Test void changingStaffRolePreservesExistingBranchAssignment() {
        var before = users.findTenantScopedUserById(staff.getId(), tenant.getId()).orElseThrow().getBranchAssignments().iterator().next().getId();
        var response = service.updateRole(owner, staff.getId(), new UpdateTeamUserRoleRequest(UserRole.MANAGER));
        em.flush(); em.clear();
        var stored = users.findTenantScopedUserById(staff.getId(), tenant.getId()).orElseThrow();
        assertEquals(UserRole.MANAGER, response.role());
        assertEquals(UserRole.MANAGER, stored.getRole());
        assertEquals(1, stored.getBranchAssignments().size());
        assertEquals(before, stored.getBranchAssignments().iterator().next().getId());
    }
    @Test void resavingOverlappingBranchAssignmentsDoesNotDuplicateRows() {
        service.updateBranches(owner, staff.getId(), new UpdateTeamUserBranchesRequest(Set.of(first.getId(), second.getId())));
        em.flush(); em.clear();
        service.updateBranches(owner, staff.getId(), new UpdateTeamUserBranchesRequest(Set.of(first.getId())));
        em.flush(); em.clear();
        var stored = users.findTenantScopedUserById(staff.getId(), tenant.getId()).orElseThrow();
        assertEquals(1, stored.getBranchAssignments().size());
        assertEquals(first.getId(), stored.getBranchAssignments().iterator().next().getBranch().getId());
    }
}
