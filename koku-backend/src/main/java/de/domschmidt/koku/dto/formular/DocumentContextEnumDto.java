package de.domschmidt.koku.dto.formular;

import lombok.Getter;

@Getter
public enum DocumentContextEnumDto {

    CUSTOMER("Kunde"),
    NONE("Kein Kontext");

    private final String description;

    DocumentContextEnumDto(final String description) {
        this.description = description;
    }
}
