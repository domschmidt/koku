package de.domschmidt.koku.customer.kafka.customers.service;

import de.domschmidt.koku.customer.kafka.customers.transformer.CustomerToKafkaCustomerDtoTransformer;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.customer.persistence.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CustomerKafkaService {

    private final KafkaTemplate<Long, CustomerKafkaDto> userKafkaTemplate;

    @Autowired
    public CustomerKafkaService(
            final KafkaTemplate<Long, CustomerKafkaDto> customerKafkaTemplate
    ) {
        this.userKafkaTemplate = customerKafkaTemplate;
    }

    public SendResult<Long, CustomerKafkaDto> sendCustomer(
            final Customer customer
    ) throws ExecutionException, InterruptedException, TimeoutException {
        return this.userKafkaTemplate.send(
                CustomerKafkaDto.TOPIC,
                customer.getId(),
                new CustomerToKafkaCustomerDtoTransformer().transformToDto(customer)
        ).get(
                10, TimeUnit.SECONDS
        );
    }
}
