package de.domschmidt.datatable.factory.dto_type_specifics.types;

import com.querydsl.core.types.Path;
import de.domschmidt.datatable.dto.type_specifics.SelectSettingsDto;
import de.domschmidt.datatable.factory.data.IEnumStringValue;
import de.domschmidt.datatable.factory.dto_type_specifics.ITypeSpecificSettingsTransformer;

import java.util.HashMap;
import java.util.Map;

public class EnumSettingsTransformer implements ITypeSpecificSettingsTransformer<SelectSettingsDto> {

    @Override
    public SelectSettingsDto transformTypeSpecificSettingsByPath(final Path<?> qDslPath) {
        final Map<Object, String> userPresentableValues = new HashMap<>();
        final Enum<?>[] enumConstants = ((Enum<?>[]) qDslPath.getType().getEnumConstants());
        for (final Enum<?> enumConstant : enumConstants) {
            if (enumConstant instanceof IEnumStringValue) {
                userPresentableValues.put(enumConstant.name(), ((IEnumStringValue<?>) enumConstant).getUserPresentableValue());
            }
        }

        return new SelectSettingsDto(
                userPresentableValues.isEmpty() ? null : userPresentableValues
        );
    }

    @Override
    public String getDtoType(final Class<?> expressionType) {
        return "Select";
    }
}
