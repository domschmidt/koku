package de.domschmidt.koku.utils;

import lombok.Getter;

@Getter
public enum DocumentCheckboxReplacementToken {

    CUSTOMER_HAY_FEVER("Kunde/Heuschnupfen", "[[${customer?.hayFever}]]"),
    CUSTOMER_PLASTER_ALLERGY("Kunde/Pflasterallergie", "[[${customer?.plasterAllergy}]]"),
    CUSTOMER_CYANOACRYLATE_ALLERGY("Kunde/Cyanacrylate Allergie", "[[${customer?.cyanoacrylateAllergy}]]"),
    CUSTOMER_ASTHMA("Kunde/Asthma", "[[${customer?.asthma}]]"),
    CUSTOMER_DRY_EYES("Kunde/Trockene Augen", "[[${customer?.dryEyes}]]"),
    CUSTOMER_CIRCULATION_PROBLEMS("Kunde/Kreislaufprobleme", "[[${customer?.circulationProblems}]]"),
    CUSTOMER_EPILEPSY("Kunde/Epilepsie", "[[${customer?.epilepsy}]]"),
    CUSTOMER_DIABETES("Kunde/Diabetes", "[[${customer?.diabetes}]]"),
    CUSTOMER_CLAUSTROPHOBIA("Kunde/Klaustrophobie", "[[${customer?.claustrophobia}]]"),
    CUSTOMER_NEURODERMATITIS("Kunde/Neurodermitis", "[[${customer?.neurodermatitis}]]"),
    CUSTOMER_CONTACTS("Kunde/Kontaktlinsen", "[[${customer?.contacts}]]"),
    CUSTOMER_GLASSES("Kunde/Brillenträger", "[[${customer?.glasses}]]");

    private final String tokenName;
    private final String replacementString;

    DocumentCheckboxReplacementToken(final String tokenName, final String replacementString) {
        this.tokenName = tokenName;
        this.replacementString = replacementString;
    }
}
