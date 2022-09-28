package de.domschmidt.datatable.factory.dto_type_specifics;

import de.domschmidt.datatable.factory.dto_type_specifics.types.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DtoTypeSpecificSettingsTransformerFactory {

    private static final Map<Class<?>, ITypeSpecificSettingsTransformer<?>> types;

    static {
        final Map<Class<?>, ITypeSpecificSettingsTransformer<?>> newTypeMap = new HashMap<>();

        newTypeMap.put(String.class, new AlphaNumericSettingsTransformer());
        newTypeMap.put(Character.class, new AlphaNumericSettingsTransformer());
        newTypeMap.put(UUID.class, new UUIDSettingsTransformer());
        newTypeMap.put(Long.class, new NumberSettingsTransformer());
        newTypeMap.put(Short.class, new NumberSettingsTransformer());
        newTypeMap.put(Integer.class, new NumberSettingsTransformer());
        newTypeMap.put(BigDecimal.class, new NumberSettingsTransformer());
        newTypeMap.put(Float.class, new NumberSettingsTransformer());
        newTypeMap.put(Double.class, new NumberSettingsTransformer());
        newTypeMap.put(Enum.class, new EnumSettingsTransformer());
        newTypeMap.put(LocalDate.class, new LocalDateSettingsTransformer());
        newTypeMap.put(LocalTime.class, new LocalTimeSettingsTransformer());
        newTypeMap.put(LocalDateTime.class, new LocalDateTimeSettingsTransformer());

        types = Collections.unmodifiableMap(newTypeMap);
    }

    public static ITypeSpecificSettingsTransformer<?> getByType(final Class<?> type) {
        if (type.isEnum()) {
            return types.get(Enum.class);
        }
        final ITypeSpecificSettingsTransformer<?> typeSpecificTransformer = types.get(type);
        if (typeSpecificTransformer == null) {
            System.out.println("no transformer found for type " + type);
        }
        return typeSpecificTransformer;
    }
}
