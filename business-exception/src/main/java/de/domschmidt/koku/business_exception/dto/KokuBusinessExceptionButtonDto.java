package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@SuperBuilder
public abstract class KokuBusinessExceptionButtonDto {

    String title;
    String text;
    String icon;
    Boolean loading;
    Boolean disabled;
    List<KokuBusinessExceptionButtonStyle> styles;
    KokuBusinessExceptionButtonSizeEnum size;

}
