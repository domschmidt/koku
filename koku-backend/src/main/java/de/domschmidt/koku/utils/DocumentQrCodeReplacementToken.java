package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentQrCodeReplacementToken {

    DOCUMENT_NAME("Dokumentenname", "[[${document.description}]]"),
    RANDOM_UUID("Zufall UUID", "[[${randomUuid}]]"),
    DATE("Datum", "[[${#temporals.format(localDate, 'dd.MM.yyyy')}]]"),
    DATE_TIME("Datum mit Zeit", "[[${#temporals.format(localDateTime, 'dd.MM.yyyy HH:mm')}]]"),
    TIME("Zeit", "[[${#temporals.format(localTime, 'HH:mm')}]]");

    private final String tokenName;
    private final String replacementString;

    DocumentQrCodeReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
