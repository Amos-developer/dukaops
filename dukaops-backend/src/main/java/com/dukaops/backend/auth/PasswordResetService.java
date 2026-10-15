package com.dukaops.backend.auth;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.dukaops.backend.auth.dto.*;

@Service
public class PasswordResetService {
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    @Value("${spring.mail.host:}") private String smtpHost;
    private final TenantUserRepository users;
    private final PasswordResetRepository tokens;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mail;
    private final String sender;
    private final String frontendUrl;
    public PasswordResetService(TenantUserRepository users, PasswordResetRepository tokens,
            PasswordEncoder encoder, ObjectProvider<JavaMailSender> mail,
            @Value("${app.mail.from:}") String sender,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.users = users; this.tokens = tokens; this.encoder = encoder;
        this.mail = mail; this.sender = sender; this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }
    @Transactional
    public void request(ForgotPasswordRequest request) {
        var delivery = mail.getIfAvailable();
        if (delivery == null || sender.isBlank() || smtpHost == null || smtpHost.isBlank()) throw unavailable();
        URI origin = URI.create(frontendUrl);
        if (!java.util.Set.of("http", "https").contains(origin.getScheme()) || origin.getHost() == null
                || origin.getQuery() != null || origin.getFragment() != null) throw unavailable();
        var account = users.findByEmailForPasswordReset(request.email().trim().toLowerCase(Locale.ROOT));
        if (account.isEmpty() || account.get().getStatus() != UserStatus.ACTIVE) return;
        var user = account.get();
        Instant now = Instant.now();
        if (tokens.existsByUser_IdAndCreatedAtAfter(user.getId(), now.minusSeconds(60))) return;
        byte[] randomBytes = new byte[32]; new SecureRandom().nextBytes(randomBytes);
        String raw = HexFormat.of().formatHex(randomBytes);
        tokens.saveAndFlush(new PasswordResetToken(user, hash(raw), hash(user.getPasswordHash()), now));
        var message = new SimpleMailMessage();
        message.setFrom(sender); message.setTo(user.getEmail()); message.setSubject("Reset your DukaOps password");
        message.setText("A password reset was requested for your DukaOps account.\n\n"
                + frontendUrl + "/reset-password#token=" + raw
                + "\n\nThis link expires in 30 minutes and can be used once. If you did not request it, ignore this email.");
        try { delivery.send(message); } catch (MailException exception) { throw unavailable(); }
    }
    @Transactional
    public void reset(ResetPasswordRequest request) {
        var token = tokens.findByTokenHash(hash(request.token())).orElseThrow(this::invalid);
        var account = users.findByEmailForPasswordReset(token.getUser().getEmail()).orElseThrow(this::invalid);
        entityManager.refresh(account);
        entityManager.refresh(token);
        Instant now = Instant.now();
        entityCheck(token, account, now);
        if (encoder.matches(request.password(), account.getPasswordHash()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a different password.");
        account.changePassword(encoder.encode(request.password()));
        token.consume(now);
    }
    private void entityCheck(PasswordResetToken token, TenantUser account, Instant now) {
        if (token.getUsedAt() != null || !token.getExpiresAt().isAfter(now)
                || account.getStatus() != UserStatus.ACTIVE
                || !token.getPasswordFingerprint().equals(hash(account.getPasswordHash()))) throw invalid();
    }
    static String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
    private ResponseStatusException invalid() { return new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link is invalid or has expired. Request a new link."); }
    private ResponseStatusException unavailable() { return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Password recovery is temporarily unavailable. Please try again later."); }
}
