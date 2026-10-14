package com.dukaops.backend.settings.dto;
import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.Size;
public record ProfileRequest(@NotBlank @Size(max=150) String fullName){}
