package de.domschmidt.koku.kafka.customers.service;

import de.domschmidt.koku.dto.customer.CustomerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class CustomerKafkaService {

    private final KafkaTemplate<Long, CustomerDto> customerKafkaTemplate;
    private final String customersTopicName;

    @Autowired
    public CustomerKafkaService(
            final KafkaTemplate<Long, CustomerDto> customerKafkaTemplate,
            final @Value("${kafka.customers.topic:customers}") String customersTopicName
    ) {
        this.customerKafkaTemplate = customerKafkaTemplate;
        this.customersTopicName = customersTopicName;
    }

    public SendResult<Long, CustomerDto> sendCustomer(final CustomerDto customer) throws ExecutionException, InterruptedException {
        return this.customerKafkaTemplate.send(this.customersTopicName, customer.getId(), customer).get();
    }
}
