package com.dukaops.backend.settings;

import java.time.ZoneId;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

final class PreferencesValidation {
    private PreferencesValidation() {}

    static void validate(String currency, String timezone, String dateFormat) {
        try {
            Currency.getInstance(currency.trim().toUpperCase(Locale.ROOT));
            ZoneId.of(timezone.trim());
        } catch (IllegalArgumentException | java.time.DateTimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Choose a valid currency and timezone.");
        }
        if (!Set.of("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd").contains(dateFormat.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Choose a supported date format.");
        }
    }
}
