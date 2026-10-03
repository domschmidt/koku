package de.domschmidt.koku.user.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class UserAppointmentKafkaDtoSerdes extends Serdes.WrapperSerde<UserAppointmentKafkaDto> {

    public UserAppointmentKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<UserAppointmentKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<UserAppointmentKafkaDto> appointmentDtoJsonDeserializer =
                new JacksonJsonDeserializer<>(UserAppointmentKafkaDto.class);
        appointmentDtoJsonDeserializer.setUseTypeHeaders(false);
        return appointmentDtoJsonDeserializer;
    }
}
