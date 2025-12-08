package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

@FieldNameConstants
@JsonTypeName("product")
public class KokuCustomerAppointmentProductTreatmentDto extends KokuCustomerAppointmentTreatmentDto {

    Long productId;

}
