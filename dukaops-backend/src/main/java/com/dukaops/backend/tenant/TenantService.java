package com.dukaops.backend.tenant;

import com.dukaops.backend.tenant.dto.CreateTenantRequest;
import com.dukaops.backend.tenant.dto.TenantResponse;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public TenantResponse create(CreateTenantRequest request) {
        String slug = request.slug()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (tenantRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A business with this slug already exists"
            );
        }

        Tenant tenant = new Tenant(
            request.name().trim(),
            slug
        );

        return toResponse(tenantRepository.save(tenant));
    }

    @Transactional(readOnly = true)
    public List<TenantResponse> findAll() {
        return tenantRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt")
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TenantResponse findById(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Business not found"
            ));

        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getName(),
            tenant.getSlug(),
            tenant.getStatus(),
            tenant.getCreatedAt(),
            tenant.getUpdatedAt()
        );
    }
}