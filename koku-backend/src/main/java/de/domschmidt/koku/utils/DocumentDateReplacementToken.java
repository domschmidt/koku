package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentDateReplacementToken {

    CAPTURE_DATE("Erfassungsdatum", "[[${localDate}]]");

    private final String tokenName;
    private final String replacementString;

    DocumentDateReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
