package com.dukaops.backend.auth.dto;
import jakarta.validation.constraints.*;
public record ResetPasswordRequest(@NotBlank @Pattern(regexp = "[a-f0-9]{64}") String token,
        @NotBlank @Size(min = 12, max = 64) String password) {}
