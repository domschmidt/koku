package de.domschmidt.koku.product.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.product.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.product.kafka.product.config.KafkaProductConfig;
import de.domschmidt.koku.product.kafka.product.config.KafkaProductManufacturerConfig;
import de.domschmidt.koku.product.kafka.product.service.ProductKafkaMaintenanceService;
import de.domschmidt.koku.product.kafka.product.service.ProductKafkaService;
import de.domschmidt.koku.product.kafka.product.service.ProductManufacturerKafkaMaintenanceService;
import de.domschmidt.koku.product.kafka.product.service.ProductManufacturerKafkaService;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductManufacturerRepository;
import de.domschmidt.koku.product.persistence.ProductRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaInfrastructureTest {

    @Test
    void producerConfigurationsUseConfiguredBrokerAndCreateTemplates() {
        assertThat(new KafkaConfiguration().getBootstrapAddress()).isNull();
        final KafkaConfiguration configuration = mock(KafkaConfiguration.class);
        when(configuration.getBootstrapAddress()).thenReturn("broker:9092");
        final KafkaProductConfig productConfig = new KafkaProductConfig(configuration);
        final KafkaProductManufacturerConfig manufacturerConfig = new KafkaProductManufacturerConfig(configuration);

        assertThat(((DefaultKafkaProducerFactory<?, ?>) productConfig.customerAppointmentProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(((DefaultKafkaProducerFactory<?, ?>) manufacturerConfig.productManufacturerKafkaDtoProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(productConfig.customerAppointmentKafkaTemplate()).isNotNull();
        assertThat(manufacturerConfig.productManufacturerKafkaTemplate()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void servicesPublishTransformedEntitiesWithTheirIdentity() throws Exception {
        final KafkaTemplate<Long, ProductKafkaDto> productTemplate = mock(KafkaTemplate.class);
        final KafkaTemplate<Long, ProductManufacturerKafkaDto> manufacturerTemplate = mock(KafkaTemplate.class);
        final SendResult<Long, ProductKafkaDto> productResult = mock(SendResult.class);
        final SendResult<Long, ProductManufacturerKafkaDto> manufacturerResult = mock(SendResult.class);
        when(productTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(productResult));
        when(manufacturerTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(manufacturerResult));
        final Product product = new Product();
        product.setId(21L);
        product.setName("Serum");
        final ProductManufacturer manufacturer = new ProductManufacturer();
        manufacturer.setId(22L);
        manufacturer.setName("Maker");

        assertThat(new ProductKafkaService(productTemplate).sendProduct(product))
                .isSameAs(productResult);
        assertThat(new ProductManufacturerKafkaService(manufacturerTemplate).sendProductManufacturer(manufacturer))
                .isSameAs(manufacturerResult);

        final ArgumentCaptor<ProductKafkaDto> productDto = ArgumentCaptor.forClass(ProductKafkaDto.class);
        final ArgumentCaptor<ProductManufacturerKafkaDto> manufacturerDto =
                ArgumentCaptor.forClass(ProductManufacturerKafkaDto.class);
        verify(productTemplate).send(eq(ProductKafkaDto.TOPIC), eq(21L), productDto.capture());
        verify(manufacturerTemplate).send(eq(ProductManufacturerKafkaDto.TOPIC), eq(22L), manufacturerDto.capture());
        assertThat(productDto.getValue().getName()).isEqualTo("Serum");
        assertThat(manufacturerDto.getValue().getName()).isEqualTo("Maker");
    }

    @Test
    void maintenancePublishesAndHandlesFailures() throws Exception {
        final ProductRepository products = mock(ProductRepository.class);
        final ProductManufacturerRepository manufacturers = mock(ProductManufacturerRepository.class);
        final ProductKafkaService productService = mock(ProductKafkaService.class);
        final ProductManufacturerKafkaService manufacturerService = mock(ProductManufacturerKafkaService.class);
        final Product product = new Product();
        final ProductManufacturer manufacturer = new ProductManufacturer();
        when(products.findAll()).thenReturn(List.of(product));
        when(manufacturers.findAll()).thenReturn(List.of(manufacturer));

        new ProductKafkaMaintenanceService(products, productService).onApplicationEvent(null);
        new ProductManufacturerKafkaMaintenanceService(manufacturers, manufacturerService).onApplicationEvent(null);

        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(productService)
                .sendProduct(product);
        doThrow(new InterruptedException("stopped")).when(manufacturerService).sendProductManufacturer(manufacturer);
        new ProductKafkaMaintenanceService(products, productService).onApplicationEvent(null);
        new ProductManufacturerKafkaMaintenanceService(manufacturers, manufacturerService).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();
        verify(productService, atLeastOnce()).sendProduct(product);
        verify(manufacturerService, atLeastOnce()).sendProductManufacturer(manufacturer);

        doThrow(new InterruptedException("stopped")).when(productService).sendProduct(product);
        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(manufacturerService)
                .sendProductManufacturer(manufacturer);
        new ProductKafkaMaintenanceService(products, productService).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();
        new ProductManufacturerKafkaMaintenanceService(manufacturers, manufacturerService).onApplicationEvent(null);
    }
}
