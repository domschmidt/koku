package de.domschmidt.koku.dto.formular.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public class AbstractFormEventDto {}
