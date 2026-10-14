package com.dukaops.backend.settings.dto;
import jakarta.validation.constraints.*; import java.util.UUID;
public record BusinessSettingsRequest(@Email String businessEmail,@Size(max=40) String businessPhone,@Size(max=2000) String businessAddress,@NotBlank @Size(min=3,max=3) String currency,@NotBlank String timezone,@NotBlank String dateFormat,UUID defaultBranchId){}
