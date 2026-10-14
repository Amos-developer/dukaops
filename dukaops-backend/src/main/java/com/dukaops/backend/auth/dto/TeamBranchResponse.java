package com.dukaops.backend.auth.dto;

import java.util.UUID;

public record TeamBranchResponse(
        UUID id,
        String name,
        String code
) {
}
