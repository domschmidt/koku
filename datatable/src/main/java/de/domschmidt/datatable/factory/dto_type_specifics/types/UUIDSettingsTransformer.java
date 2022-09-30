package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.type_specifics.AlphaNumericSettingsDto;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;

public class UUIDSettingsTransformer implements ITypeSpecificSettingsTransformer<AlphaNumericSettingsDto> {

    @Override
    public AlphaNumericSettingsDto transformTypeSpecificSettingsByPath(final Path<?> qDslPath) {
        return new AlphaNumericSettingsDto(36, 36);
    }

    @Override
    public String getDtoType(final Class<?> expressionType) {
        return "AlphaNumeric";
    }

}
