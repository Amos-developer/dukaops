package com.dukaops.backend.sales;
import java.math.*; import java.util.*; import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import org.springframework.web.server.ResponseStatusException; import org.springframework.http.HttpStatus;
import com.dukaops.backend.auth.*; import com.dukaops.backend.branch.*; import com.dukaops.backend.branch.inventory.*; import com.dukaops.backend.branch.inventory.repository.*; import com.dukaops.backend.product.*; import com.dukaops.backend.sales.dto.*;
@Service public class SalesService {
 @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
 private final SalesOrderRepository orders; private final SalesOrderItemRepository items; private final SalesPaymentRepository payments; private final BranchRepository branches; private final ProductRepository products; private final StockLevelRepository stock; private final StockMovementRepository movements; private final TenantUserRepository users; private final TenantUserBranchAssignmentRepository assignments;
 public SalesService(SalesOrderRepository o,SalesOrderItemRepository i,SalesPaymentRepository p,BranchRepository b,ProductRepository pr,StockLevelRepository s,StockMovementRepository m,TenantUserRepository u,TenantUserBranchAssignmentRepository a){orders=o;items=i;payments=p;branches=b;products=pr;stock=s;movements=m;users=u;assignments=a;}
 @Transactional public SalesOrderResponse checkout(TenantUserPrincipal actor,CheckoutRequest r){
  if(r.items()==null||r.items().isEmpty()) throw error(HttpStatus.BAD_REQUEST,"A sale must contain at least one product.");
  var existing=orders.findByTenant_IdAndIdempotencyKey(actor.getTenantId(),r.idempotencyKey()); if(existing.isPresent()) { requireBranch(actor, existing.get().getBranch().getId()); return response(existing.get()); }
  Branch branch=branches.findTenantScopedBranchById(r.branchId(),actor.getTenantId()).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Branch not found.")); requireBranch(actor,branch.getId()); TenantUser seller=users.findTenantScopedUserById(actor.getUserId(),actor.getTenantId()).orElseThrow();
  BigDecimal total=BigDecimal.ZERO; List<Product> ps=new ArrayList<>();
  Map<UUID, BigDecimal> requestedQuantities = new HashMap<>();
  for(var line:r.items()){ if(line.quantity()==null || line.quantity().signum()<=0) throw error(HttpStatus.BAD_REQUEST,"Sale quantities must be positive."); BigDecimal requested=requestedQuantities.merge(line.productId(),line.quantity(),BigDecimal::add); Product p=products.findTenantScopedProductById(line.productId(),actor.getTenantId()).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Product not found.")); if(p.getStatus()!=ProductStatus.ACTIVE) throw error(HttpStatus.BAD_REQUEST,"Product is not active."); var level=stock.findStockLevelForUpdate(actor.getTenantId(),branch.getId(),p.getId()).orElseThrow(()->error(HttpStatus.BAD_REQUEST,"This product is not stocked at the selected branch.")); if(level.getQuantity().compareTo(requested)<0) throw error(HttpStatus.BAD_REQUEST,"Insufficient stock for "+p.getName()+"."); total=total.add(p.getUnitPrice().multiply(line.quantity())); ps.add(p); }
  SalesOrder order=orders.save(new SalesOrder(branch.getTenant(),branch,seller,r.customerName(),r.customerPhone(),OrderStatus.COMPLETED,total.setScale(2,RoundingMode.HALF_UP),r.idempotencyKey()));
  for(int n=0;n<r.items().size();n++){var line=r.items().get(n);var p=ps.get(n);var level=stock.findStockLevelForUpdate(actor.getTenantId(),branch.getId(),p.getId()).orElseThrow(); level.applyQuantityChange(line.quantity().negate()); stock.save(level); items.save(new SalesOrderItem(branch.getTenant(),order,p,line.quantity(),p.getUnitPrice())); movements.save(new StockMovement(branch.getTenant(),branch,p,StockMovementType.SALE,line.quantity().negate(),level.getQuantity(),order.getId().toString(),"Sale checkout",seller)); }
  payments.save(new SalesPayment(branch.getTenant(),order,r.paymentMethod(),total,r.idempotencyKey())); return response(order);
 }
 @Transactional(readOnly=true) public Page<SalesOrderResponse> list(TenantUserPrincipal actor,String search,OrderStatus status,int page,int size) {
  var scope = SalesScope.visibleTo(actor);
  if (status != null) scope = scope.and((root, query, builder) -> builder.equal(root.get("status"), status));
  if (search != null && !search.isBlank()) {
   String pattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
   scope = scope.and((root, query, builder) -> builder.or(builder.like(builder.lower(root.get("customerName")), pattern), builder.like(builder.lower(root.get("idempotencyKey")), pattern)));
  }
  return orders.findAll(scope, PageRequest.of(Math.max(0,page), Math.min(Math.max(size,1),100), Sort.by(Sort.Direction.DESC,"createdAt"))).map(this::response);
 }
 private void requireBranch(TenantUserPrincipal a,UUID branch){if(a.getRole()==UserRole.OWNER||a.getRole()==UserRole.ADMIN)return; if(!assignments.existsByTenant_IdAndUser_IdAndBranch_Id(a.getTenantId(),a.getUserId(),branch)) throw error(HttpStatus.FORBIDDEN,"You do not have access to this branch.");}
 @Transactional(readOnly=true) public SalesOrderDetailResponse detail(TenantUserPrincipal a,UUID id){var o=orders.findByTenant_IdAndId(a.getTenantId(),id).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Order not found.")); requireBranch(a,o.getBranch().getId()); var lines=items.findAllByTenant_IdAndOrder_Id(a.getTenantId(),id).stream().map(i->new SalesOrderDetailResponse.Item(i.getProduct().getId(),i.getProduct().getName(),i.getProduct().getSku(),i.getQuantity(),i.getUnitPrice(),i.getLineTotal())).toList(); return new SalesOrderDetailResponse(o.getId(),o.getBranch().getId(),o.getCustomerName(),o.getCustomerPhone(),o.getStatus(),o.getTotalAmount(),o.getCreatedAt(),lines);}
 @Transactional public SalesOrderResponse updateStatus(TenantUserPrincipal a,UUID id,OrderStatus status){var o=orders.findByTenant_IdAndId(a.getTenantId(),id).orElseThrow(()->error(HttpStatus.NOT_FOUND,"Order not found.")); requireBranch(a,o.getBranch().getId()); if(o.getStatus()==OrderStatus.COMPLETED||o.getStatus()==OrderStatus.CANCELLED) throw error(HttpStatus.BAD_REQUEST,"This order can no longer change status."); o.setStatus(status); return response(o);}
 @Transactional(readOnly=true) public SalesSummaryResponse summary(TenantUserPrincipal actor) {
  var builder = entityManager.getCriteriaBuilder();
  var query = builder.createQuery(Object[].class);
  var root = query.from(SalesOrder.class);
  var completed = builder.equal(root.get("status"), OrderStatus.COMPLETED);
  query.multiselect(
    builder.sum(builder.<BigDecimal>selectCase().when(completed, root.<BigDecimal>get("totalAmount")).otherwise(BigDecimal.ZERO)),
    builder.sum(builder.<Long>selectCase().when(completed, 1L).otherwise(0L)),
    builder.sum(builder.<Long>selectCase().when(builder.equal(root.get("status"), OrderStatus.PENDING),1L).otherwise(0L)));
  query.where(SalesScope.visibleTo(actor).toPredicate(root, query, builder));
  Object[] totals = entityManager.createQuery(query).getSingleResult();
  BigDecimal revenue = totals[0] == null ? BigDecimal.ZERO : (BigDecimal) totals[0];
  long completedCount = totals[1] == null ? 0 : ((Number) totals[1]).longValue();
  long pendingCount = totals[2] == null ? 0 : ((Number) totals[2]).longValue();
  return new SalesSummaryResponse(revenue, completedCount, pendingCount, completedCount == 0 ? BigDecimal.ZERO : revenue.divide(BigDecimal.valueOf(completedCount),2,RoundingMode.HALF_UP));
 }
 private SalesOrderResponse response(SalesOrder o){return new SalesOrderResponse(o.getId(),o.getBranch().getId(),o.getCustomerName(),o.getStatus(),o.getTotalAmount(),o.getCreatedAt());} private ResponseStatusException error(HttpStatus s,String m){return new ResponseStatusException(s,m);}
}
