package com.dukaops.backend.messaging;

import java.util.*;
import com.dukaops.backend.auth.*;
import com.dukaops.backend.contact.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class CustomerSmsService {
    private final CustomerRepository customers;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    private final TwilioGateway gateway;

    public CustomerSmsService(CustomerRepository customers, JdbcTemplate jdbc,
            TransactionTemplate transactions, TwilioGateway gateway) {
        this.customers = customers; this.jdbc = jdbc; this.transactions = transactions; this.gateway = gateway;
    }

    void authorize(TenantUserPrincipal actor) {
        if (actor == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (actor.getRole() != UserRole.OWNER && actor.getRole() != UserRole.ADMIN)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public static String normalizePhone(String value) {
        String phone = value == null ? "" : value.trim().replaceAll("[\\s()-]", "");
        if (phone.matches("0[67][0-9]{8}")) phone = "+255" + phone.substring(1);
        if (phone.matches("255[67][0-9]{8}")) phone = "+" + phone;
        if (!phone.matches("\\+255[67][0-9]{8}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The customer needs a valid Tanzanian mobile number.");
        return phone;
    }

    public Map<String, Object> configuration(TenantUserPrincipal actor) {
        authorize(actor);
        return Map.of("configured", gateway.configured(), "provider", "Twilio", "maxCharacters", 480);
    }

    public List<Map<String, Object>> history(TenantUserPrincipal actor, int page) {
        authorize(actor);
        return jdbc.queryForList("SELECT id, customer_id AS \"customerId\", recipient, body, status, created_at AS \"createdAt\" FROM customer_sms WHERE tenant_id=? ORDER BY created_at DESC, id LIMIT 10 OFFSET ?",
                actor.getTenantId(), Math.max(0, Math.min(page, 100000)) * 10);
    }

    public Map<String, Object> details(TenantUserPrincipal actor, UUID id) {
        authorize(actor);
        var rows = jdbc.queryForList("SELECT id, customer_id AS \"customerId\", recipient, body, status, provider_sid AS \"providerSid\", created_at AS \"createdAt\" FROM customer_sms WHERE tenant_id=? AND id=?", actor.getTenantId(), id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return rows.getFirst();
    }

    public Map<String, Object> send(TenantUserPrincipal actor, UUID customerId, UUID key, String body) {
        authorize(actor);
        if (body == null || body.isBlank() || body.length() > 480)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a message of up to 480 characters.");
        if (!gateway.configured()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMS sending is not configured yet.");
        // Serialize claims per tenant; commit the claim BEFORE the external request. Never retry uncertain sends.
        UUID id = UUID.randomUUID();
        boolean claimed = Boolean.TRUE.equals(transactions.execute(tx -> {
            jdbc.queryForObject("SELECT id FROM tenants WHERE id=? FOR UPDATE", UUID.class, actor.getTenantId());
            var previous = jdbc.queryForList("SELECT id, customer_id, body FROM customer_sms WHERE tenant_id=? AND request_key=?", actor.getTenantId(), key);
            if (!previous.isEmpty()) {
                var old = previous.getFirst();
                if (!customerId.equals(old.get("customer_id")) || !body.equals(old.get("body")))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "This request was already used for another message.");
                return false;
            }
            var customer = customers.findByTenant_IdAndId(actor.getTenantId(), customerId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (customer.getStatus() != ContactStatus.ACTIVE)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Activate this customer before sending a message.");
            Integer recent = jdbc.queryForObject("SELECT count(*) FROM customer_sms WHERE tenant_id=? AND created_at > now() - interval '1 hour'", Integer.class, actor.getTenantId());
            if (recent != null && recent >= 100) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Hourly SMS limit reached.");
            jdbc.update("INSERT INTO customer_sms(id,tenant_id,customer_id,actor_id,request_key,recipient,body,status) VALUES(?,?,?,?,?,?,?,?)",
                    id, actor.getTenantId(), customerId, actor.getUserId(), key, normalizePhone(customer.getPhone()), body, "submitting");
            return true;
        }));
        if (!claimed) {
            UUID existing = jdbc.queryForObject("SELECT id FROM customer_sms WHERE tenant_id=? AND request_key=?", UUID.class, actor.getTenantId(), key);
            return details(actor, existing);
        }
        var message = details(actor, id);
        try {
            var result = gateway.send((String) message.get("recipient"), body);
            jdbc.update("UPDATE customer_sms SET provider_sid=?,status=? WHERE tenant_id=? AND id=?", result.sid(), result.status(), actor.getTenantId(), id);
        } catch (RestClientResponseException failure) {
            // Explicit 4xx rejection is known not to have created a message; never expose provider bodies.
            String status = failure.getStatusCode().is4xxClientError() ? "failed" : "unknown";
            jdbc.update("UPDATE customer_sms SET status=? WHERE tenant_id=? AND id=?", status, actor.getTenantId(), id);
        } catch (Exception failure) {
            // A timeout may happen AFTER Twilio accepted the SMS. Do not automatically send it again.
            jdbc.update("UPDATE customer_sms SET status='unknown' WHERE tenant_id=? AND id=?", actor.getTenantId(), id);
        }
        return details(actor, id);
    }

    public Map<String, Object> refresh(TenantUserPrincipal actor, UUID id) {
        var message = details(actor, id);
        if (!gateway.configured()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "SMS sending is not configured yet.");
        if (message.get("providerSid") instanceof String sid) {
            try {
                var result = gateway.fetch(sid);
                jdbc.update("UPDATE customer_sms SET status=? WHERE tenant_id=? AND id=?", result.status(), actor.getTenantId(), id);
            } catch (Exception failure) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "We could not refresh the delivery status. Try again later.");
            }
        }
        return details(actor, id);
    }
}
