package de.domschmidt.koku.dto.activity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.domschmidt.koku.dto.product.ProductDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(value = ActivityStepDto.class, name = "ActivityStepDto"),
                @JsonSubTypes.Type(value = ProductDto.class, name = "ProductDto"),
        }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ActivitySequenceItemDto {
        Long sequenceId;

}
