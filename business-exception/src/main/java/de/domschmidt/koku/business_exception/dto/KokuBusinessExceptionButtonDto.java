package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

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
