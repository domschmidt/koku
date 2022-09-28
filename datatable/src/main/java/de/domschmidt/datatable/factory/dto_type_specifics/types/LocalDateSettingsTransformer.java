package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.type_specifics.DateSettingsDto;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;

public class LocalDateSettingsTransformer implements ITypeSpecificSettingsTransformer<DateSettingsDto> {

    @Override
    public DateSettingsDto transformTypeSpecificSettingsByPath(final Path<?> qDslPath) {
        return null;
    }

    @Override
    public String getDtoType(final Path<?> qDslPath) {
        return "Date";
    }

}
