package com.dukaops.backend.auth;

import java.time.Instant;
import java.util.Optional;
import com.dukaops.backend.auth.dto.*;
import com.dukaops.backend.tenant.Tenant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private final TenantUserRepository users = mock(TenantUserRepository.class);
    private final PasswordResetRepository tokens = mock(PasswordResetRepository.class);
    private final JavaMailSender mail = mock(JavaMailSender.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private PasswordResetService service;
    private TenantUser account;
    private final String rawToken = "a".repeat(64);

    @BeforeEach void setup() {
        @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(mail);
        service = new PasswordResetService(users, tokens, encoder, provider, "support@example.com", "http://localhost:5173");
        ReflectionTestUtils.setField(service, "smtpHost", "smtp.example.com");
        ReflectionTestUtils.setField(service, "entityManager", mock(EntityManager.class));
        account = new TenantUser(new Tenant("Test business", "reset-test"), "Juma", "juma@example.com", encoder.encode("OriginalPassword123"), UserRole.STAFF);
        when(users.findByEmailForPasswordReset(account.getEmail())).thenReturn(Optional.of(account));
    }
    private PasswordResetToken issue(Instant now) {
        var token = new PasswordResetToken(account, PasswordResetService.hash(rawToken), PasswordResetService.hash(account.getPasswordHash()), now);
        when(tokens.findByTokenHash(PasswordResetService.hash(rawToken))).thenReturn(Optional.of(token));
        return token;
    }
    @Test void emailContainsResetLinkButStoredTokenIsHashed() {
        var saved = org.mockito.ArgumentCaptor.forClass(PasswordResetToken.class);
        var message = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        service.request(new ForgotPasswordRequest(account.getEmail()));
        verify(tokens).saveAndFlush(saved.capture());
        verify(mail).send(message.capture());
        assertTrue(message.getValue().getText().contains("/reset-password#token="));
        String emailedToken = message.getValue().getText().split("#token=")[1].split("\\s")[0];
        assertEquals(PasswordResetService.hash(emailedToken), ReflectionTestUtils.getField(saved.getValue(), "tokenHash"));
        assertNotEquals(emailedToken, ReflectionTestUtils.getField(saved.getValue(), "tokenHash"));
        assertEquals(account, saved.getValue().getUser());
        assertTrue(saved.getValue().getExpiresAt().isAfter(Instant.now()));
    }
    @Test void unknownAccountDoesNotSendMail() {
        when(users.findByEmailForPasswordReset("unknown@example.com")).thenReturn(Optional.empty());
        service.request(new ForgotPasswordRequest("unknown@example.com"));
        verifyNoInteractions(tokens, mail);
    }
    @Test void repeatedRequestsAreThrottled() {
        when(tokens.existsByUser_IdAndCreatedAtAfter(eq(account.getId()), any())).thenReturn(true);
        service.request(new ForgotPasswordRequest(account.getEmail()));
        verifyNoInteractions(mail);
    }
    @Test void resetHashesPasswordAndPreventsReuse() {
        var token = issue(Instant.now());
        service.reset(new ResetPasswordRequest(rawToken, "NewPassword12345"));
        assertTrue(encoder.matches("NewPassword12345", account.getPasswordHash()));
        assertNotNull(token.getUsedAt());
        assertThrows(ResponseStatusException.class, () -> service.reset(new ResetPasswordRequest(rawToken, "AnotherPassword123")));
    }
    @Test void expiredAndUnknownLinksAreRejected() {
        issue(Instant.now().minusSeconds(3600));
        assertThrows(ResponseStatusException.class, () -> service.reset(new ResetPasswordRequest(rawToken, "NewPassword12345")));
        when(tokens.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.reset(new ResetPasswordRequest(rawToken, "NewPassword12345")));
        assertTrue(encoder.matches("OriginalPassword123", account.getPasswordHash()));
    }
    @Test void passwordChangesInvalidateOlderLinks() {
        issue(Instant.now());
        account.changePassword(encoder.encode("ChangedElsewhere123"));
        assertThrows(ResponseStatusException.class, () -> service.reset(new ResetPasswordRequest(rawToken, "NewPassword12345")));
    }
    @Test void unconfiguredSmtpReturnsUnavailable() {
        ReflectionTestUtils.setField(service, "smtpHost", "");
        var failure = assertThrows(ResponseStatusException.class, () -> service.request(new ForgotPasswordRequest(account.getEmail())));
        assertEquals(503, failure.getStatusCode().value());
        verifyNoInteractions(users, tokens, mail);
    }
}
