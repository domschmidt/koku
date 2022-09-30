package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.type_specifics.DateTimeSettingsDto;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;

public class LocalDateTimeSettingsTransformer implements ITypeSpecificSettingsTransformer<DateTimeSettingsDto> {

    @Override
    public DateTimeSettingsDto transformTypeSpecificSettingsByPath(final Path<?> qDslPath) {
        return null;
    }

    @Override
    public String getDtoType(final Class<?> expressionType) {
        return "DateTime";
    }

}
