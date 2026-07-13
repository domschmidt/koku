package de.domschmidt.koku.file.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dto.formular.fields.input.InputFormularField;
import de.domschmidt.koku.dto.formular.fields.select.SelectFormularField;
import de.domschmidt.koku.file.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.list.dto.response.ListViewDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;

class FileViewContractTest {

    private final FileController controller = new FileController(null, null, null, null);

    @Test
    void listExposesCaptureFilteringCrudAndSynchronization() {
        final ListViewDto view = controller.getListView(null, null, null);

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getFilters()).isNotEmpty();
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedItems()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getItemActions()).isNotEmpty();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
        assertThat(view.getGlobalItemStyling()).isNotEmpty();
    }

    @Test
    void contextualListCanBeCreatedForEmbeddedUse() {
        final ListViewDto view = controller.getListView(42L, "services/customers/:customerId/files", "POST");

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(controller
                        .getListView(42L, "services/customers/:customerId/files", null)
                        .getRoutedContents())
                .isNotEmpty();
    }

    @Test
    void formSupportsAnEmptyCustomerStore() {
        final CustomerKTableProcessor processor = mock(CustomerKTableProcessor.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        final KeyValueIterator iterator = mock(KeyValueIterator.class);
        when(processor.getCustomers()).thenReturn(store);
        when(store.all()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);

        final FormViewDto view = new FileController(null, null, null, processor).getFormularView();
        final InputFormularField filename = view.getContents().values().stream()
                .filter(InputFormularField.class::isInstance)
                .map(InputFormularField.class::cast)
                .filter(field -> "filename".equals(field.getValuePath()))
                .findFirst()
                .orElseThrow();
        final SelectFormularField customer = view.getContents().values().stream()
                .filter(SelectFormularField.class::isInstance)
                .map(SelectFormularField.class::cast)
                .filter(field -> "customerId".equals(field.getValuePath()))
                .findFirst()
                .orElseThrow();

        assertThat(view.getAlias()).isEqualTo("file");
        assertThat(filename.getReadonly()).isTrue();
        assertThat(customer.getReadonly()).isTrue();
        assertThat(customer.getPossibleValues()).isEmpty();
        assertThat(view.getPlacements()).isNotEmpty();
    }

    @Test
    void formMapsCustomersFromTheStore() {
        final CustomerKTableProcessor processor = mock(CustomerKTableProcessor.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        final CustomerKafkaDto customerDto = CustomerKafkaDto.builder()
                .firstname("Ada")
                .lastname("Lovelace")
                .deleted(false)
                .build();
        final KeyValueIterator<Long, CustomerKafkaDto> iterator = new KeyValueIterator<>() {
            private boolean available = true;

            @Override
            public void close() {
                // The iterator is an in-memory test fixture and owns no closeable resource.
            }

            @Override
            public Long peekNextKey() {
                return 42L;
            }

            @Override
            public boolean hasNext() {
                return this.available;
            }

            @Override
            public KeyValue<Long, CustomerKafkaDto> next() {
                this.available = false;
                return KeyValue.pair(42L, customerDto);
            }
        };
        when(processor.getCustomers()).thenReturn(store);
        when(store.all()).thenReturn(iterator);

        final SelectFormularField customer = new FileController(null, null, null, processor)
                .getFormularView().getContents().values().stream()
                        .filter(SelectFormularField.class::isInstance)
                        .map(SelectFormularField.class::cast)
                        .findFirst()
                        .orElseThrow();

        assertThat(customer.getPossibleValues()).singleElement().satisfies(value -> {
            assertThat(value.getId()).isEqualTo("42");
            assertThat(value.getText()).isEqualTo("Ada Lovelace");
            assertThat(value.getDisabled()).isFalse();
        });
    }

    @Test
    void fileQuerySupportsGlobalAndCustomerScopedEmptyPages() {
        final FileController queryController = new FileController(emptyEntityManager(), null, null, null);

        assertThat(queryController.findAll(null, null).getResults()).isEmpty();
        assertThat(queryController.findAll(42L, null).getResults()).isEmpty();
    }

    private static EntityManager emptyEntityManager() {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory factory = mock(EntityManagerFactory.class);
        final Query query = mock(Query.class, RETURNS_SELF);
        when(entityManager.getEntityManagerFactory()).thenReturn(factory);
        when(factory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());
        when(query.getSingleResult()).thenReturn(0L);
        return entityManager;
    }
}
