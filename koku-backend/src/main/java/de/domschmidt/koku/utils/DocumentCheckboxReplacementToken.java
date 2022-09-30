package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentCheckboxReplacementToken {

    ;

    private final String tokenName;
    private final String replacementString;

    DocumentCheckboxReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
