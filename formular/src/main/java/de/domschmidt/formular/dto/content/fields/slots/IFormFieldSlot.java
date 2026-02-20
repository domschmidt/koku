package de.domschmidt.formular.dto.content.fields.slots;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public interface IFormFieldSlot {}
