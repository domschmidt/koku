package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.type_specifics.AlphaNumericSettingsDto;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;

import javax.validation.constraints.Size;

public class AlphaNumericSettingsTransformer implements ITypeSpecificSettingsTransformer<AlphaNumericSettingsDto> {

    @Override
    public AlphaNumericSettingsDto transformTypeSpecificSettingsByPath(
            final Path<?> qDslPath
    ) {
        final AlphaNumericSettingsDto result;
        final Size sizeAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(Size.class);
        if (sizeAnnotation != null) {
            final Integer maxCharacterLength = sizeAnnotation.max() != 0 ? sizeAnnotation.max() : null;
            final Integer minCharacterLength = sizeAnnotation.min() != 0 ? sizeAnnotation.min() : null;
            result = new AlphaNumericSettingsDto(
                    maxCharacterLength,
                    minCharacterLength
            );
        } else {
            result = null;
        }

        return result;
    }

    @Override
    public String getDtoType(final Class<?> expressionType) {
        return "AlphaNumeric";
    }

}
