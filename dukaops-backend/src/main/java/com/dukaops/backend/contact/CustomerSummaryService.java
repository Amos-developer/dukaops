package com.dukaops.backend.contact;

import com.dukaops.backend.auth.TenantUserPrincipal;
import com.dukaops.backend.auth.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerSummaryService {
    private final CustomerRepository customers;
    public CustomerSummaryService(CustomerRepository customers) { this.customers = customers; }
    public record Summary(long totalCustomers) {}

    @Transactional(readOnly = true)
    public Summary summary(TenantUserPrincipal actor) {
        if (actor == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (actor.getRole() != UserRole.OWNER && actor.getRole() != UserRole.ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer information is restricted to owners and administrators.");
        return new Summary(customers.countByTenant_Id(actor.getTenantId()));
    }
}
