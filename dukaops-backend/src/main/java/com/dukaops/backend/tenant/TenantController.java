package com.dukaops.backend.tenant;

import com.dukaops.backend.tenant.dto.CreateTenantRequest;
import com.dukaops.backend.tenant.dto.TenantResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse create(
            @Valid @RequestBody CreateTenantRequest request) {
        return tenantService.create(request);
    }

    @GetMapping
    public List<TenantResponse> findAll() {
        return tenantService.findAll();
    }

    @GetMapping("/{id}")
    public TenantResponse findById(@PathVariable UUID id) {
        return tenantService.findById(id);
    }
    
}
