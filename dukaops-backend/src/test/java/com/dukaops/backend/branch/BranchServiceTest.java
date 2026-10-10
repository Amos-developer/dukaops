
package com.dukaops.backend.branch;

import com.dukaops.backend.branch.dto.CreateBranchRequest;
import com.dukaops.backend.tenant.Tenant;
import com.dukaops.backend.tenant.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private BranchService branchService;

    @Test
    void createShouldRejectUnknownTenant() {
        // Arrange
        UUID tenantId = UUID.randomUUID();

        CreateBranchRequest request =
                new CreateBranchRequest("Main Branch", "MAIN", true);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> branchService.create(tenantId, request)
        );

        // Assert
        assertEquals(404, exception.getStatusCode().value());

        verify(branchRepository, never()).save(any(Branch.class));
    }

    @Test
    void createShouldRejectDuplicateBranchCode() {
        // Arrange
        UUID tenantId = UUID.randomUUID();

        Tenant tenant = org.mockito.Mockito.mock(Tenant.class);

        CreateBranchRequest request =
                new CreateBranchRequest("Warehouse", "MAIN", false);

        when(tenantRepository.findById(tenantId))
                .thenReturn(Optional.of(tenant));

        when(branchRepository.existsByTenant_IdAndCode(
                tenantId, "MAIN"))
                .thenReturn(true);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> branchService.create(tenantId, request)
        );

        // Assert
        assertEquals(409, exception.getStatusCode().value());

        verify(branchRepository, never()).save(any(Branch.class));
    }
}
