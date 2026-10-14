package com.dukaops.backend.settings.dto;
import java.time.Instant; import java.util.UUID;
public record BusinessSettingsResponse(UUID tenantId,String businessName,String slug,String businessEmail,String businessPhone,String businessAddress,String currency,String timezone,String dateFormat,UUID defaultBranchId,String defaultBranchName,Instant updatedAt){}
