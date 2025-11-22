package de.domschmidt.koku.document.persistence.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;

@Converter
public class GenericJsonConverter<T> implements AttributeConverter<T, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final Class<T> baseClass;

    public GenericJsonConverter(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) return null;
        try {
            return mapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new RuntimeException("JSON serialization failed for " + baseClass.getName(), e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            return mapper.readValue(dbData, baseClass);
        } catch (IOException e) {
            throw new RuntimeException("JSON deserialization failed for " + baseClass.getName(), e);
        }
    }
}
