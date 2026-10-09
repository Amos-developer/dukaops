package com.dukaops.backend.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(

    @NotBlank(message = "Branch name is required")
    @Size(max = 150)
    String name,

    @NotBlank(message = "Branch code is required")
    @Size(max = 30)
    @Pattern(
        regexp = "(?i)^[a-z0-9]+(?:[-_][a-z0-9]+)*$",
        message = "Code must contain letters, numbers, hyphens or underscores"
    )
    String code,

    Boolean isMain

) {
}