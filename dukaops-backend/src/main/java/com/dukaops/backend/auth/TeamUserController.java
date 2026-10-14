package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.CreateTeamUserRequest;
import com.dukaops.backend.auth.dto.TeamUserPageResponse;
import com.dukaops.backend.auth.dto.TeamUserResponse;
import com.dukaops.backend.auth.dto.UpdateTeamUserRequest;
import com.dukaops.backend.auth.dto.UpdateTeamUserStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/team/users")
public class TeamUserController {

    private final TeamUserService teamUserService;

    public TeamUserController(TeamUserService teamUserService) {
        this.teamUserService = teamUserService;
    }

    @GetMapping
    public TeamUserPageResponse listUsers(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return teamUserService.listUsers(
                principal,
                search,
                role,
                status,
                page,
                size
        );
    }

    @GetMapping("/{userId}")
    public TeamUserResponse getUser(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @PathVariable UUID userId
    ) {
        return teamUserService.getUser(principal, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamUserResponse createUser(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @Valid @RequestBody CreateTeamUserRequest request
    ) {
        return teamUserService.createUser(principal, request);
    }

    @PutMapping("/{userId}")
    public TeamUserResponse updateUser(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateTeamUserRequest request
    ) {
        return teamUserService.updateUser(principal, userId, request);
    }

    @PatchMapping("/{userId}/status")
    public TeamUserResponse updateStatus(
            @AuthenticationPrincipal TenantUserPrincipal principal,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateTeamUserStatusRequest request
    ) {
        return teamUserService.updateStatus(principal, userId, request);
    }
}
