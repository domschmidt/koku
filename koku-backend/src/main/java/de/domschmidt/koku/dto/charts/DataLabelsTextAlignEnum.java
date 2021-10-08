package de.domschmidt.koku.dto.charts;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DataLabelsTextAlignEnum {

    START("start"),
    CENTER("center"),
    END("end"),
    LEFT("left"),
    RIGHT("right");

    final String align;

    DataLabelsTextAlignEnum(String align) {
        this.align = align;
    }

    @JsonValue
    public String getValue() {
        return this.align;
    }
}
