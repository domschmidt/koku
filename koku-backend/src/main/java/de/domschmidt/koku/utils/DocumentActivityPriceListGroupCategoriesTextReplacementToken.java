package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentActivityPriceListGroupCategoriesTextReplacementToken {

    ACTIVITY_NAME("Tätigkeitskategorie", "[[${activity?.category?.description}]]")
    ;

    private final String tokenName;
    private final String replacementString;

    DocumentActivityPriceListGroupCategoriesTextReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
