package com.dukaops.backend.category;

import java.math.BigDecimal;
import java.util.*;
import com.dukaops.backend.auth.*;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final JdbcTemplate jdbc;
    private final AccessPolicy access;
    public CategoryService(JdbcTemplate jdbc, AccessPolicy access) { this.jdbc = jdbc; this.access = access; }
    public record Request(@NotBlank @Size(max=100) String name, @NotNull Boolean active) {}
    public record Category(UUID id, String name, boolean active, long productCount) {}
    public record Slice(UUID categoryId, String name, BigDecimal quantity, BigDecimal value, BigDecimal unvaluedQuantity) {}
    public record Distribution(List<Slice> categories, BigDecimal totalQuantity, BigDecimal totalValue, BigDecimal unvaluedQuantity) {}

    private void authenticated(TenantUserPrincipal actor) {
        if (actor == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
    private void admin(TenantUserPrincipal actor) {
        authenticated(actor);
        if (!access.businessAdmin(actor)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners and administrators can manage categories.");
    }

    public List<Category> list(TenantUserPrincipal actor) {
        authenticated(actor);
        return jdbc.query("""
                SELECT c.id,c.name,c.active,count(p.id) AS product_count
                FROM product_categories c LEFT JOIN products p ON p.tenant_id=c.tenant_id AND p.category_id=c.id
                WHERE c.tenant_id=? GROUP BY c.id ORDER BY lower(c.name),c.id
                """, (rs, n) -> new Category(rs.getObject("id", UUID.class), rs.getString("name"), rs.getBoolean("active"), rs.getLong("product_count")), actor.getTenantId());
    }

    @Transactional
    public Category save(TenantUserPrincipal actor, UUID id, Request request) {
        admin(actor);
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isBlank() || name.length() > 100 || request.active() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a category name of up to 100 characters and its status.");
        jdbc.queryForObject("SELECT id FROM tenants WHERE id=? FOR UPDATE", UUID.class, actor.getTenantId());
        if (id != null && jdbc.queryForObject("SELECT count(*) FROM product_categories WHERE tenant_id=? AND id=?", Long.class, actor.getTenantId(), id) == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        UUID categoryId = id == null ? UUID.randomUUID() : id;
        if (jdbc.queryForObject("SELECT count(*) FROM product_categories WHERE tenant_id=? AND lower(name)=lower(?) AND id<>?", Long.class, actor.getTenantId(), name, categoryId) > 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A category with this name already exists.");
        if (id == null) jdbc.update("INSERT INTO product_categories(id,tenant_id,name,active) VALUES(?,?,?,?)", categoryId, actor.getTenantId(), name, request.active());
        else jdbc.update("UPDATE product_categories SET name=?,active=?,updated_at=now() WHERE tenant_id=? AND id=?", name, request.active(), actor.getTenantId(), id);
        return list(actor).stream().filter(c -> c.id().equals(categoryId)).findFirst().orElseThrow();
    }

    public void validateAssignment(UUID tenantId, UUID categoryId, UUID currentCategoryId) {
        if (categoryId == null) return;
        // Hold a shared row lock until the surrounding product transaction commits.
        var rows = jdbc.queryForList("SELECT active FROM product_categories WHERE tenant_id=? AND id=? FOR SHARE", tenantId, categoryId);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found.");
        if (!Boolean.TRUE.equals(rows.getFirst().get("active")) && !categoryId.equals(currentCategoryId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose an active category.");
    }

    public Distribution inventory(TenantUserPrincipal actor, UUID branchId, String view) {
        authenticated(actor);
        if (!List.of("quantity", "value").contains(view)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose quantity or value.");
        boolean costsAllowed = actor.getRole() != UserRole.STAFF;
        if (view.equals("value") && !costsAllowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (branchId != null) access.requireBranch(actor, branchId);
        String sql = """
                SELECT c.id AS category_id,coalesce(c.name,'Uncategorized') AS name,
                       coalesce(sum(s.quantity),0) AS quantity,
                       coalesce(sum(s.quantity*p.cost_price),0) AS value,
                       coalesce(sum(CASE WHEN p.cost_price IS NULL THEN s.quantity ELSE 0 END),0) AS unvalued_quantity
                FROM stock_levels s JOIN products p ON p.tenant_id=s.tenant_id AND p.id=s.product_id
                LEFT JOIN product_categories c ON c.tenant_id=p.tenant_id AND c.id=p.category_id
                WHERE s.tenant_id=?
                """;
        var args = new ArrayList<Object>(); args.add(actor.getTenantId());
        if (branchId != null) { sql += " AND s.branch_id=?"; args.add(branchId); }
        if (!access.businessAdmin(actor)) {
            sql += " AND EXISTS (SELECT 1 FROM tenant_user_branch_assignments a WHERE a.tenant_id=s.tenant_id AND a.branch_id=s.branch_id AND a.user_id=?)";
            args.add(actor.getUserId());
        }
        sql += " GROUP BY c.id,c.name ORDER BY quantity DESC,name";
        var slices = jdbc.query(sql, (rs,n) -> new Slice(rs.getObject("category_id", UUID.class), rs.getString("name"), rs.getBigDecimal("quantity"),
                costsAllowed ? rs.getBigDecimal("value") : null, costsAllowed ? rs.getBigDecimal("unvalued_quantity") : null), args.toArray());
        BigDecimal quantity = slices.stream().map(Slice::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal value = costsAllowed ? slices.stream().map(Slice::value).reduce(BigDecimal.ZERO, BigDecimal::add) : null;
        BigDecimal unvalued = costsAllowed ? slices.stream().map(Slice::unvaluedQuantity).reduce(BigDecimal.ZERO, BigDecimal::add) : null;
        return new Distribution(slices, quantity, value, unvalued);
    }
}
