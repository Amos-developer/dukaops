package com.dukaops.backend.auth.dto;

import java.util.List;

public record TeamUserPageResponse(
        List<TeamUserResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
