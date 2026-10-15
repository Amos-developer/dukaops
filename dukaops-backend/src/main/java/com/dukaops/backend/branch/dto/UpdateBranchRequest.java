package com.dukaops.backend.branch.dto;
import jakarta.validation.constraints.*;
public record UpdateBranchRequest(@NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 30) String code) {}
