package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentReplacementToken {

    CUSTOMER_FIRST_NAME("Kunde/Vorname", "[[${customer?.firstName}]]"),
    CUSTOMER_LAST_NAME("Kunde/Nachname", "[[${customer?.lastName}]]"),
    CUSTOMER_ADDRESS("Kunde/Adresse", "[[${customer?.lastName}]]"),
    CUSTOMER_POSTAL_CODE("Kunde/Postleitzahl", "[[${customer?.postalCode}]]"),
    CUSTOMER_CITY("Kunde/Ort", "[[${customer?.city}]]"),

    DOCUMENT_NAME("Dokumentenname", "[[${document.description}]]"),
    DATE("Datum", "[[${#temporals.format(localDate, 'dd.MM.yyyy')}]]"),
    DATE_TIME("Datum mit Zeit", "[[${#temporals.format(localDateTime, 'dd.MM.yyyy HH:mm')}]]"),
    TIME("Zeit", "[[${#temporals.format(localTime, 'HH:mm')}]]");

    private final String tokenName;
    private final String replacementString;

    DocumentReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
