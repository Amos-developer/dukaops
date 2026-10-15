package com.dukaops.backend.product;

import java.util.List;
import java.util.UUID;

import com.dukaops.backend.auth.TenantUserPrincipal;
import com.dukaops.backend.product.dto.ProductRequest;
import com.dukaops.backend.product.dto.ProductResponse;
import com.dukaops.backend.product.dto.ProductStatusRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal TenantUserPrincipal principal) {

        ProductResponse response = productService.create(
            principal.getTenantId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID productId,
            @AuthenticationPrincipal TenantUserPrincipal principal) {
        productService.delete(principal, productId);
    }

    @GetMapping
    public List<ProductResponse> list(
            @AuthenticationPrincipal TenantUserPrincipal principal) {

        return productService.list(principal.getTenantId());
    }

    @GetMapping("/{productId}")
    public ProductResponse get(
            @PathVariable UUID productId,
            @AuthenticationPrincipal TenantUserPrincipal principal) {

        return productService.get(principal.getTenantId(), productId);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal TenantUserPrincipal principal) {

        return productService.update(
            principal.getTenantId(), productId, request);
    }

    @PatchMapping("/{productId}/status")
    public ProductResponse changeStatus(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductStatusRequest request,
            @AuthenticationPrincipal TenantUserPrincipal principal) {

        return productService.changeStatus(
            principal.getTenantId(), productId, request.status());
    }
}
