package de.domschmidt.koku.customer.kafka.customers.service;

import de.domschmidt.koku.customer.kafka.customers.transformer.CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class CustomerAppointmentKafkaService {

    private final KafkaTemplate<Long, CustomerAppointmentKafkaDto> customerAppointmentKafkaTemplate;

    @Autowired
    public CustomerAppointmentKafkaService(
            final KafkaTemplate<Long, CustomerAppointmentKafkaDto> customerAppointmentKafkaTemplate) {
        this.customerAppointmentKafkaTemplate = customerAppointmentKafkaTemplate;
    }

    public SendResult<Long, CustomerAppointmentKafkaDto> sendCustomerAppointment(
            final CustomerAppointment customerAppointment)
            throws ExecutionException, InterruptedException, TimeoutException {
        return this.customerAppointmentKafkaTemplate
                .send(
                        CustomerAppointmentKafkaDto.TOPIC,
                        customerAppointment.getId(),
                        new CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer()
                                .transformToDto(customerAppointment))
                .get(10, TimeUnit.SECONDS);
    }
}
