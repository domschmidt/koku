package de.domschmidt.datatable.factory.dto_type_specifics;

import com.querydsl.core.types.Path;

public interface ITypeSpecificSettingsTransformer<S> {

    S transformTypeSpecificSettingsByPath(Path<?> qDslPath);

    default String getDtoType(final Path<?> qDslPath) {
        return qDslPath.getType().getSimpleName();
    }

}
