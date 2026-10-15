package com.dukaops.backend.messaging;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class TwilioGateway {
    private final String account, token, sender;
    private final boolean enabled;
    private final RestClient client;

    public TwilioGateway(@Value("${dukaops.sms.enabled:false}") boolean enabled,
            @Value("${dukaops.sms.account-sid:}") String account,
            @Value("${dukaops.sms.auth-token:}") String token,
            @Value("${dukaops.sms.from:}") String sender) {
        this.enabled = enabled; this.account = account; this.token = token; this.sender = sender;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        client = RestClient.builder().requestFactory(factory).baseUrl("https://api.twilio.com").build();
    }

    public boolean configured() {
        return enabled && account.matches("AC[0-9a-fA-F]{32}") && !token.isBlank() && !sender.isBlank();
    }

    public record Result(String sid, String status) {}

    private Result result(Map<String, Object> response) {
        if (response == null || !(response.get("sid") instanceof String sid) || !sid.matches("SM[0-9a-fA-F]{32}"))
            throw new IllegalStateException("Invalid messaging provider response");
        return new Result(sid, String.valueOf(response.getOrDefault("status", "queued")));
    }

    public Result send(String phone, String body) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("From", sender); form.add("To", phone); form.add("Body", body);
        return result(client.post().uri("/2010-04-01/Accounts/{account}/Messages.json", account)
                .headers(h -> h.setBasicAuth(account, token)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(Map.class));
    }

    public Result fetch(String sid) {
        if (!sid.matches("SM[0-9a-fA-F]{32}")) throw new IllegalArgumentException("Invalid message reference");
        return result(client.get().uri("/2010-04-01/Accounts/{account}/Messages/{sid}.json", account, sid)
                .headers(h -> h.setBasicAuth(account, token)).retrieve().body(Map.class));
    }
}
