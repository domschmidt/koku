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
        final Size sizeAnnotation = qDslPath.getAnnotatedElement().getDeclaredAnnotation(Size.class);
        final Integer maxCharacterLength;
        final Integer minCharacterLength;
        if (sizeAnnotation != null) {
            maxCharacterLength = sizeAnnotation.max() != 0 ? sizeAnnotation.max() : null;
            minCharacterLength = sizeAnnotation.min() != 0 ? sizeAnnotation.min() : null;
        } else {
            maxCharacterLength = null;
            minCharacterLength = null;
        }

        return new AlphaNumericSettingsDto(
                maxCharacterLength,
                minCharacterLength
        );
    }

    @Override
    public String getDtoType(final Path<?> qDslPath) {
        return "AlphaNumeric";
    }

}
