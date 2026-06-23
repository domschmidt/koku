package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("close-button")
@Data
@EqualsAndHashCode(callSuper = false)
public class KokuBusinessExceptionCloseButtonDto extends KokuBusinessExceptionButtonDto {}
