package de.domschmidt.koku.dto.charts;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChartTypeEnum {

    LINE("line"),
    BAR("bar"),
    RADAR("radar"),
    DOUGHNUT("doughnut"),
    POLAR_AREA("polarArea"),
    BUBBLE("bubble"),
    PIE("pie"),
    SCATTER("scatter");

    final String name;

    ChartTypeEnum(String name) {
        this.name = name;
    }

    @JsonValue
    public String getValue() {
        return this.name;
    }

}
