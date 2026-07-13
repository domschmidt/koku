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
import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.product.kafka.product.service.ProductKafkaService;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductPriceHistoryEntry;
import de.domschmidt.koku.product.persistence.ProductRepository;
import de.domschmidt.koku.product.transformer.ProductToProductDtoTransformer;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ProductCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final ProductRepository repository = mock(ProductRepository.class);
    private final ProductKafkaService kafkaService = mock(ProductKafkaService.class);
    private final ProductToProductDtoTransformer transformer = mock(ProductToProductDtoTransformer.class);
    private ProductController controller;

    @BeforeEach
    void setUp() {
        controller = new ProductController(entityManager, repository, kafkaService, transformer);
    }

    @Test
    void readSummaryAndPriceHistoryUsePersistedProduct() {
        final Product product = product(5L, 2L, false);
        final ProductPriceHistoryEntry price = new ProductPriceHistoryEntry();
        price.setPrice(new BigDecimal("19.90"));
        price.setRecorded(LocalDateTime.of(2026, java.time.Month.JULY, 1, 12, 0));
        product.setPriceHistory(List.of(price));
        final KokuProductDto dto = KokuProductDto.builder().id(5L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(product));
        when(transformer.transformToDto(product)).thenReturn(dto);

        assertThat(controller.read(5L)).isSameAs(dto);
        assertThat(controller.readSummary(5L).getId()).isEqualTo(5L);
        assertThat(controller.readProductPriceHistory(5L).getSeries().getFirst().getData())
                .containsExactly(new BigDecimal("19.90"));
    }

    @Test
    void readRejectsMissingProduct() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.read(9L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> controller.readSummary(9L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.readProductPriceHistory(9L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateTransformsFlushesPublishesAndReturnsDto() throws Exception {
        final Product product = product(5L, 2L, false);
        final KokuProductDto update = KokuProductDto.builder().version(2L).build();
        when(entityManager.getReference(Product.class, 5L)).thenReturn(product);

        controller.update(5L, false, update);

        verify(transformer).transformToEntity(product, update);
        verify(entityManager).flush();
        verify(kafkaService).sendProduct(product);
    }

    @Test
    void updateRejectsStaleVersionUnlessForced() throws Exception {
        final Product product = product(5L, 3L, false);
        final KokuProductDto update = KokuProductDto.builder().version(2L).build();
        when(entityManager.getReference(Product.class, 5L)).thenReturn(product);

        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(product, update);
    }

    @Test
    void deleteAndRestoreToggleLifecycle() {
        final Product product = product(5L, 2L, false);
        when(entityManager.getReference(Product.class, 5L)).thenReturn(product);

        controller.delete(5L);
        assertThat(product.isDeleted()).isTrue();
        controller.restore(5L);
        assertThat(product.isDeleted()).isFalse();
    }

    @Test
    void lifecycleRejectsRepeatedOperations() {
        final Product product = product(5L, 2L, true);
        when(entityManager.getReference(Product.class, 5L)).thenReturn(product);

        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        product.setDeleted(false);
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createPersistsPublishesAndTransforms() throws Exception {
        final KokuProductDto input = KokuProductDto.builder().name("Serum").build();
        final Product entity = product(5L, 0L, false);
        when(transformer.transformToEntity(any(Product.class), org.mockito.Mockito.same(input)))
                .thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        controller.create(input);

        verify(kafkaService).sendProduct(entity);
        verify(transformer).transformToDto(entity);
    }

    @Test
    void kafkaFailureIsExposedAsServerError() throws Exception {
        final Product product = product(5L, 2L, false);
        when(kafkaService.sendProduct(product)).thenThrow(new ExecutionException(new IllegalStateException("broker")));

        assertThatThrownBy(() -> controller.sendProductUpdate(product))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendProduct(product);
        assertThatThrownBy(() -> controller.sendProductUpdate(product)).isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static Product product(Long id, Long version, boolean deleted) {
        final Product product = new Product();
        product.setId(id);
        product.setVersion(version);
        product.setName("Serum");
        product.setDeleted(deleted);
        return product;
    }
}
