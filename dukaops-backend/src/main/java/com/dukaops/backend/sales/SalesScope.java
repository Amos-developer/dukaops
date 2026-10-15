package com.dukaops.backend.sales;

import com.dukaops.backend.auth.*;
import org.springframework.data.jpa.domain.Specification;

public final class SalesScope {
    private SalesScope() {}
    public static Specification<SalesOrder> visibleTo(TenantUserPrincipal actor) {
        return (root, query, builder) -> {
            var tenant = builder.equal(root.get("tenant").get("id"), actor.getTenantId());
            if (actor.getRole() == UserRole.OWNER || actor.getRole() == UserRole.ADMIN) return tenant;
            var assigned = query.subquery(Integer.class);
            var assignment = assigned.from(TenantUserBranchAssignment.class);
            assigned.select(builder.literal(1)).where(
                    builder.equal(assignment.get("tenant").get("id"), actor.getTenantId()),
                    builder.equal(assignment.get("user").get("id"), actor.getUserId()),
                    builder.equal(assignment.get("branch").get("id"), root.get("branch").get("id")));
            return builder.and(tenant, builder.exists(assigned));
        };
    }
}
