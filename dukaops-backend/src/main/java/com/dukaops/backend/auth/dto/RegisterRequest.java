
package com.dukaops.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Size(max = 150)
        String businessName,

        @NotBlank
        @Size(max = 100)
        @Pattern(
            regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
            message = "Use lowercase letters, numbers, and hyphens"
        )
        String businessSlug,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @NotBlank
        @Size(min = 12, max = 64)
        String password,

        @NotBlank
        @Pattern(regexp = "^\\+255[1-9][0-9]{8}$", message = "Enter a valid Tanzanian phone number starting with +255")
        String phoneNumber
) {
}
