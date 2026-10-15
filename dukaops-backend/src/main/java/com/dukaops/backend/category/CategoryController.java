package com.dukaops.backend.category;

import java.util.*;
import jakarta.validation.Valid;
import com.dukaops.backend.auth.TenantUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }
    @GetMapping public List<CategoryService.Category> list(@AuthenticationPrincipal TenantUserPrincipal actor) { return service.list(actor); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public CategoryService.Category create(@AuthenticationPrincipal TenantUserPrincipal actor, @Valid @RequestBody CategoryService.Request request) { return service.save(actor, null, request); }
    @PutMapping("/{id}") public CategoryService.Category update(@AuthenticationPrincipal TenantUserPrincipal actor, @PathVariable UUID id, @Valid @RequestBody CategoryService.Request request) { return service.save(actor, id, request); }
    @GetMapping("/inventory") public CategoryService.Distribution inventory(@AuthenticationPrincipal TenantUserPrincipal actor,
            @RequestParam(required=false) UUID branchId, @RequestParam(defaultValue="quantity") String view) { return service.inventory(actor, branchId, view); }
}
