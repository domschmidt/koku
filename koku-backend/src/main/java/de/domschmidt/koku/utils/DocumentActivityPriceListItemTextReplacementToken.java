package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentActivityPriceListItemTextReplacementToken {

    ACTIVITY_NAME("Aktivitätsname", "[[${activity?.description}]]"),
    ACTIVITY_PRICE("Aktueller Aktivitätspreis", "[[${#numbers.formatDecimal(activity?.priceHistory[#lists.size(activity?.priceHistory)-1].price, 0, 'COMMA', 2, 'POINT')}]]"),
    ;

    private final String tokenName;
    private final String replacementString;

    DocumentActivityPriceListItemTextReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
