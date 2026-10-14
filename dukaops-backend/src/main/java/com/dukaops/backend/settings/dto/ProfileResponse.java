package com.dukaops.backend.settings.dto;
import java.time.Instant; import java.util.UUID;
public record ProfileResponse(UUID userId,String fullName,String email,String role,String status,Instant createdAt,Instant updatedAt){}
