package de.domschmidt.koku.product.kafka.product.service;

import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.product.transformer.ProductToKafkaProductDtoTransformer;
import de.domschmidt.koku.product.persistence.Product;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class ProductKafkaService {

    private final KafkaTemplate<Long, ProductKafkaDto> productKafkaTemplate;

    @Autowired
    public ProductKafkaService(final KafkaTemplate<Long, ProductKafkaDto> customerKafkaTemplate) {
        this.productKafkaTemplate = customerKafkaTemplate;
    }

    public SendResult<Long, ProductKafkaDto> sendProduct(final Product customer)
            throws ExecutionException, InterruptedException, TimeoutException {
        return this.productKafkaTemplate
                .send(
                        ProductKafkaDto.TOPIC,
                        customer.getId(),
                        new ProductToKafkaProductDtoTransformer().transformToDto(customer))
                .get(10, TimeUnit.SECONDS);
    }
}
