package de.domschmidt.datatable.dto.type_specifics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SelectSettingsDto {

    Map<Object, String> userPresentableValues;

}
