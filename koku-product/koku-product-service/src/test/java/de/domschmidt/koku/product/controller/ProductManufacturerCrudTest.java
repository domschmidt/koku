package de.domschmidt.koku.product.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.product.KokuProductManufacturerDto;
import de.domschmidt.koku.product.kafka.product.service.ProductManufacturerKafkaService;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductManufacturerRepository;
import de.domschmidt.koku.product.transformer.ProductManufacturerToProductManufacturerDtoTransformer;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ProductManufacturerCrudTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ProductManufacturerRepository repository = mock(ProductManufacturerRepository.class);
    private final ProductManufacturerKafkaService kafkaService = mock(ProductManufacturerKafkaService.class);
    private final ProductManufacturerToProductManufacturerDtoTransformer transformer =
            mock(ProductManufacturerToProductManufacturerDtoTransformer.class);
    private ProductManufacturersController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductManufacturersController(entityManager, repository, kafkaService, transformer);
    }

    @Test
    void readSummaryAndMissingPathsAreDefined() {
        final ProductManufacturer manufacturer = manufacturer(false, 2L);
        when(repository.findById(5L)).thenReturn(Optional.of(manufacturer));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(manufacturer))
                .thenReturn(KokuProductManufacturerDto.builder().id(5L).build());
        assertThat(controller.read(5L).getId()).isEqualTo(5L);
        assertThat(controller.readSummary(5L).getId()).isEqualTo(5L);
        assertThatThrownBy(() -> controller.read(6L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.readSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void staleUpdateRequiresForce() throws Exception {
        final ProductManufacturer manufacturer = manufacturer(false, 3L);
        final KokuProductManufacturerDto update =
                KokuProductManufacturerDto.builder().version(2L).build();
        when(entityManager.getReference(ProductManufacturer.class, 5L)).thenReturn(manufacturer);
        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(manufacturer, update);
        verify(kafkaService).sendProductManufacturer(manufacturer);
    }

    @Test
    void lifecycleAndCreateContractsAreDefined() {
        final ProductManufacturer manufacturer = manufacturer(false, 0L);
        when(entityManager.getReference(ProductManufacturer.class, 5L)).thenReturn(manufacturer);
        controller.delete(5L);
        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        controller.restore(5L);
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);

        final KokuProductManufacturerDto input =
                KokuProductManufacturerDto.builder().name("Maker").build();
        when(transformer.transformToEntity(any(ProductManufacturer.class), org.mockito.Mockito.same(input)))
                .thenReturn(manufacturer);
        when(repository.saveAndFlush(manufacturer)).thenReturn(manufacturer);
        controller.create(input);
        verify(transformer, org.mockito.Mockito.atLeastOnce()).transformToDto(manufacturer);
    }

    @Test
    void kafkaFailureIsExposed() throws Exception {
        final ProductManufacturer manufacturer = manufacturer(false, 2L);
        when(kafkaService.sendProductManufacturer(manufacturer))
                .thenThrow(new ExecutionException(new IllegalStateException()));
        assertThatThrownBy(() -> controller.sendProductManufacturerUpdate(manufacturer))
                .isInstanceOf(ResponseStatusException.class);
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendProductManufacturer(manufacturer);
        assertThatThrownBy(() -> controller.sendProductManufacturerUpdate(manufacturer))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static ProductManufacturer manufacturer(boolean deleted, Long version) {
        final ProductManufacturer manufacturer = new ProductManufacturer(5L);
        manufacturer.setName("Maker");
        manufacturer.setVersion(version);
        manufacturer.setDeleted(deleted);
        return manufacturer;
    }
}
