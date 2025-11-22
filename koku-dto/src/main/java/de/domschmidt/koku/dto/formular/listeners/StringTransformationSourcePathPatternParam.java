package de.domschmidt.koku.dto.formular.listeners;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("source-path")
public class StringTransformationSourcePathPatternParam extends AbstractStringTransformationPatternParam {

    String sourcePath;

}
