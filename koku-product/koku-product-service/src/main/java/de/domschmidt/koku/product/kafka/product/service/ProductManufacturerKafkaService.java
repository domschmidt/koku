package de.domschmidt.koku.product.kafka.product.service;

import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.product.kafka.product.transformer.ProductManufacturerToKafkaProductManufacturerDtoTransformer;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class ProductManufacturerKafkaService {

    private final KafkaTemplate<Long, ProductManufacturerKafkaDto> customerAppointmentKafkaTemplate;

    @Autowired
    public ProductManufacturerKafkaService(
            final KafkaTemplate<Long, ProductManufacturerKafkaDto> customerAppointmentKafkaTemplate) {
        this.customerAppointmentKafkaTemplate = customerAppointmentKafkaTemplate;
    }

    public SendResult<Long, ProductManufacturerKafkaDto> sendProductManufacturer(
            final ProductManufacturer customerAppointment)
            throws ExecutionException, InterruptedException, TimeoutException {
        return this.customerAppointmentKafkaTemplate
                .send(
                        ProductManufacturerKafkaDto.TOPIC,
                        customerAppointment.getId(),
                        new ProductManufacturerToKafkaProductManufacturerDtoTransformer()
                                .transformToDto(customerAppointment))
                .get(10, TimeUnit.SECONDS);
    }
}
