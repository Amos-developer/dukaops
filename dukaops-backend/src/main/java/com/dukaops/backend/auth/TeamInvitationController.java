package com.dukaops.backend.auth;

import com.dukaops.backend.auth.dto.AcceptTeamInvitationRequest;
import com.dukaops.backend.auth.dto.CreateTeamInvitationRequest;
import com.dukaops.backend.auth.dto.TeamInvitationResponse;
import com.dukaops.backend.auth.dto.TeamUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/team/invitations")
public class TeamInvitationController {
    private final TeamInvitationService service;
    public TeamInvitationController(TeamInvitationService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamInvitationResponse create(@AuthenticationPrincipal TenantUserPrincipal principal, @Valid @RequestBody CreateTeamInvitationRequest request) {
        return service.create(principal, request);
    }

    @PostMapping("/{token}/accept")
    public TeamUserResponse accept(@PathVariable String token, @Valid @RequestBody AcceptTeamInvitationRequest request) {
        return service.accept(token, request);
    }
}
