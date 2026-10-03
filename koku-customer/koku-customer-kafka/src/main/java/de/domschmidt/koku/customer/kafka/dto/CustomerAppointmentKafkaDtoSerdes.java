package de.domschmidt.koku.customer.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class CustomerAppointmentKafkaDtoSerdes extends Serdes.WrapperSerde<CustomerAppointmentKafkaDto> {

    public CustomerAppointmentKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<CustomerAppointmentKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<CustomerAppointmentKafkaDto> appointmentDtoJsonDeserializer =
                new JacksonJsonDeserializer<>(CustomerAppointmentKafkaDto.class);
        appointmentDtoJsonDeserializer.setUseTypeHeaders(false);
        return appointmentDtoJsonDeserializer;
    }
}
