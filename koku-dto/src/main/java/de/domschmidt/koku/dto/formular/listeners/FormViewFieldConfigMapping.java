package de.domschmidt.koku.dto.formular.listeners;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FormViewFieldConfigMapping {

    String targetConfigPath;
    AbstractConfigMappingDto valueMapping;
}
