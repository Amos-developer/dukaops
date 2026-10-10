
package com.dukaops.backend.tenant;

import com.dukaops.backend.tenant.dto.CreateTenantRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void createShouldRejectDuplicateSlug() {
        // Arrange
        CreateTenantRequest request =
                new CreateTenantRequest("Demo Shop", "demo-shop");

        when(tenantRepository.existsBySlug("demo-shop"))
                .thenReturn(true);

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> tenantService.create(request)
        );

        // Assert
        assertEquals(409, exception.getStatusCode().value());

        verify(tenantRepository, never()).save(any(Tenant.class));
    }
}
