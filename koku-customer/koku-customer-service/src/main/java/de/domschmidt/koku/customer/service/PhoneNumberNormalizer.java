package de.domschmidt.koku.customer.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {

    private static final String DEFAULT_REGION = "DE";

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public String normalize(final String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return phoneNumber;
        }
        try {
            final var parsedPhoneNumber = phoneNumberUtil.parse(phoneNumber, DEFAULT_REGION);
            if (!phoneNumberUtil.isValidNumber(parsedPhoneNumber)) {
                return phoneNumber.trim();
            }
            return phoneNumberUtil.format(parsedPhoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (final NumberParseException ignored) {
            return phoneNumber.trim();
        }
    }
}
