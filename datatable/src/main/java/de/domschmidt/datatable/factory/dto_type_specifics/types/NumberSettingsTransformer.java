package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.type_specifics.NumberSettingsDto;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class NumberSettingsTransformer implements ITypeSpecificSettingsTransformer<NumberSettingsDto> {

    @Override
    public NumberSettingsDto transformTypeSpecificSettingsByPath(final Path<?> qDslPath) {
        BigDecimal min;
        BigDecimal max;
        Integer integralDigits;
        Integer fractionalDigits;

        final Min minAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(Min.class);
        final DecimalMin decimalMinAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(DecimalMin.class);
        if (minAnnotation != null && minAnnotation.value() != 0) {
            min = BigDecimal.valueOf(minAnnotation.value());
        } else if (decimalMinAnnotation != null && decimalMinAnnotation.value() != null && !decimalMinAnnotation.value().trim().isEmpty()) {
            min = new BigDecimal(decimalMinAnnotation.value());
        } else {
            min = null;
        }
        final Max maxAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(Max.class);
        final DecimalMax decimalMaxAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(DecimalMax.class);
        if (maxAnnotation != null && maxAnnotation.value() != 0) {
            max = BigDecimal.valueOf(maxAnnotation.value());
        } else if (decimalMaxAnnotation != null && decimalMaxAnnotation.value() != null && !decimalMaxAnnotation.value().trim().isEmpty()) {
            max = new BigDecimal(decimalMaxAnnotation.value());
        } else {
            max = null;
        }
        final Digits digitsAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(Digits.class);
        if (digitsAnnotation != null) {
            integralDigits = digitsAnnotation.integer();
            fractionalDigits = digitsAnnotation.fraction();
        } else {
            integralDigits = null;
            fractionalDigits = null;
        }

        return new NumberSettingsDto(
                min,
                max,
                integralDigits,
                fractionalDigits
        );
    }

    @Override
    public String getDtoType(final Class<?> expressionType) {
        return "Number";
    }
}
