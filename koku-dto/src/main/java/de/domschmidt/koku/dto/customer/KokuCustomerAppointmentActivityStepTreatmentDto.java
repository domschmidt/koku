package de.domschmidt.koku.dto.customer;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor

@FieldNameConstants
@JsonTypeName("activity-step")
public class KokuCustomerAppointmentActivityStepTreatmentDto extends KokuCustomerAppointmentTreatmentDto {

    Long activityStepId;

}
