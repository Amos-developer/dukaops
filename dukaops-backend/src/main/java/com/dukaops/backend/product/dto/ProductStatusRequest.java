package com.dukaops.backend.product.dto;

import com.dukaops.backend.product.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(
    @NotNull ProductStatus status
) {}
