package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.CreateTeamUserRequest;
import com.dukaops.backend.auth.dto.UpdateTeamUserRequest;
import com.dukaops.backend.auth.dto.UpdateTeamUserStatusRequest;
import com.dukaops.backend.branch.Branch;
import com.dukaops.backend.branch.BranchRepository;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamUserServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantUserRepository tenantUserRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private TeamUserService service;

    private UUID tenantId;
    private UUID actorId;
    private TenantUserPrincipal ownerPrincipal;

    @BeforeEach
    void setUp() {
        service = new TeamUserService(
                tenantRepository,
                tenantUserRepository,
                branchRepository,
                passwordEncoder
        );

        tenantId = UUID.randomUUID();
        actorId = UUID.randomUUID();
        ownerPrincipal = principal(actorId, tenantId, UserRole.OWNER);
    }

    @Test
    void authorizedUserCanListTenantUsers() {
        TenantUser user = tenantUser(UserRole.STAFF);

        when(tenantUserRepository.findTenantUsers(
                eq(tenantId),
                eq(null),
                eq(null),
                eq(null),
                any()
        )).thenReturn(new PageImpl<>(List.of(user)));

        var response = service.listUsers(
                ownerPrincipal,
                null,
                null,
                null,
                0,
                20
        );

        assertEquals(1, response.content().size());
        assertEquals(UserRole.STAFF, response.content().getFirst().role());
    }

    @Test
    void authorizedUserCanCreateUserWithTenantBranches() {
        UUID branchId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);
        Branch branch = branch(branchId);

        when(tenant.getId()).thenReturn(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantUserRepository.existsByEmailIgnoreCase("staff@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("very-secure-password"))
                .thenReturn("hashed-password");
        when(branchRepository.findTenantScopedBranchById(branchId, tenantId))
                .thenReturn(Optional.of(branch));
        when(tenantUserRepository.save(any(TenantUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.createUser(
                ownerPrincipal,
                new CreateTeamUserRequest(
                        "Staff Member",
                        "Staff@Example.com",
                        "very-secure-password",
                        UserRole.STAFF,
                        Set.of(branchId)
                )
        );

        assertEquals("staff@example.com", response.email());
        assertEquals(1, response.assignedBranches().size());

        ArgumentCaptor<TenantUser> userCaptor =
                ArgumentCaptor.forClass(TenantUser.class);
        verify(tenantUserRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getPasswordHash()
                .equals("hashed-password"));
    }

    @Test
    void duplicateLoginIdentifierIsRejected() {
        when(tenantUserRepository.existsByEmailIgnoreCase("staff@example.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createUser(
                        ownerPrincipal,
                        new CreateTeamUserRequest(
                                "Staff Member",
                                "staff@example.com",
                                "very-secure-password",
                                UserRole.STAFF,
                                Set.of()
                        )
                )
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void invalidBranchAssignmentsAreRejected() {
        UUID branchId = UUID.randomUUID();
        Tenant tenant = mock(Tenant.class);

        when(tenant.getId()).thenReturn(tenantId);
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
        when(tenantUserRepository.existsByEmailIgnoreCase("staff@example.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("very-secure-password"))
                .thenReturn("hashed-password");
        when(branchRepository.findTenantScopedBranchById(branchId, tenantId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createUser(
                        ownerPrincipal,
                        new CreateTeamUserRequest(
                                "Staff Member",
                                "staff@example.com",
                                "very-secure-password",
                                UserRole.STAFF,
                                Set.of(branchId)
                        )
                )
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void unauthorizedRolesCannotManageUsers() {
        TenantUserPrincipal staffPrincipal =
                principal(UUID.randomUUID(), tenantId, UserRole.STAFF);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.listUsers(
                        staffPrincipal,
                        null,
                        null,
                        null,
                        0,
                        20
                )
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void adminCannotAssignPrivilegedRole() {
        TenantUserPrincipal adminPrincipal =
                principal(UUID.randomUUID(), tenantId, UserRole.ADMIN);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createUser(
                        adminPrincipal,
                        new CreateTeamUserRequest(
                                "Another Admin",
                                "admin2@example.com",
                                "very-secure-password",
                                UserRole.ADMIN,
                                Set.of()
                        )
                )
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void userCannotChangeOwnRole() {
        TenantUser self = new TenantUser(
                mock(Tenant.class),
                "Owner User",
                "owner@example.com",
                "hash",
                UserRole.ADMIN
        );

        when(tenantUserRepository.findTenantScopedUserById(actorId, tenantId))
                .thenReturn(Optional.of(self));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateUser(
                        principal(actorId, tenantId, UserRole.ADMIN),
                        actorId,
                        new UpdateTeamUserRequest(
                                "Owner User",
                                UserRole.MANAGER,
                                Set.of()
                        )
                )
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(tenantUserRepository, never()).save(any(TenantUser.class));
    }

    @Test
    void userFromAnotherTenantCannotBeRetrieved() {
        UUID userId = UUID.randomUUID();

        when(tenantUserRepository.findTenantScopedUserById(userId, tenantId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getUser(ownerPrincipal, userId)
        );

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void detailsCanBeUpdatedWithValidPermissions() {
        UUID userId = UUID.randomUUID();
        TenantUser user = tenantUser(UserRole.STAFF);

        when(tenantUserRepository.findTenantScopedUserById(userId, tenantId))
                .thenReturn(Optional.of(user));
        when(tenantUserRepository.save(user)).thenReturn(user);

        var response = service.updateUser(
                ownerPrincipal,
                userId,
                new UpdateTeamUserRequest(
                        "Updated Name",
                        UserRole.MANAGER,
                        Set.of()
                )
        );

        assertEquals("Updated Name", response.fullName());
        assertEquals(UserRole.MANAGER, response.role());
    }

    @Test
    void activationAndDeactivationWorksForNonOwner() {
        UUID userId = UUID.randomUUID();
        TenantUser user = tenantUser(UserRole.STAFF);

        when(tenantUserRepository.findTenantScopedUserById(userId, tenantId))
                .thenReturn(Optional.of(user));
        when(tenantUserRepository.save(user)).thenReturn(user);

        var response = service.updateStatus(
                ownerPrincipal,
                userId,
                new UpdateTeamUserStatusRequest(UserStatus.SUSPENDED)
        );

        assertEquals(UserStatus.SUSPENDED, response.status());
    }

    @Test
    void ownerAccountsCannotBeDemotedOrDeactivated() {
        UUID userId = UUID.randomUUID();
        TenantUser owner = tenantUser(UserRole.OWNER);

        when(tenantUserRepository.findTenantScopedUserById(userId, tenantId))
                .thenReturn(Optional.of(owner));

        ResponseStatusException demotion = assertThrows(
                ResponseStatusException.class,
                () -> service.updateUser(
                        ownerPrincipal,
                        userId,
                        new UpdateTeamUserRequest(
                                "Owner",
                                UserRole.ADMIN,
                                Set.of()
                        )
                )
        );

        ResponseStatusException deactivation = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        ownerPrincipal,
                        userId,
                        new UpdateTeamUserStatusRequest(UserStatus.SUSPENDED)
                )
        );

        assertEquals(403, demotion.getStatusCode().value());
        assertEquals(403, deactivation.getStatusCode().value());
    }

    private TenantUserPrincipal principal(
            UUID userId,
            UUID tenantId,
            UserRole role
    ) {
        return new TenantUserPrincipal(
                userId,
                tenantId,
                "owner@example.com",
                "hash",
                role,
                true
        );
    }

    private TenantUser tenantUser(UserRole role) {
        Tenant tenant = mock(Tenant.class);
        return new TenantUser(
                tenant,
                role.name() + " User",
                role.name().toLowerCase() + "@example.com",
                "hash",
                role
        );
    }

    private Branch branch(UUID branchId) {
        Branch branch = mock(Branch.class);
        when(branch.getId()).thenReturn(branchId);
        when(branch.getName()).thenReturn("Main Branch");
        when(branch.getCode()).thenReturn("MAIN");
        return branch;
    }
}
