package de.domschmidt.koku.dav.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dav.kafka.customers.service.CustomerAppointmentKTableProcessor;
import de.domschmidt.koku.dav.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.dav.kafka.users.service.UserAppointmentKTableProcessor;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;

class DavRepositoryTest {

    @Test
    void customerAppointmentsAreSortedFilteredAndAddressableById() {
        final CustomerAppointmentKafkaDto later = CustomerAppointmentKafkaDto.builder()
                .id(2L)
                .start(LocalDateTime.of(2026, java.time.Month.JULY, 13, 10, 0))
                .build();
        final CustomerAppointmentKafkaDto earlier = CustomerAppointmentKafkaDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, java.time.Month.JULY, 12, 10, 0))
                .build();
        final CustomerAppointmentKafkaDto deleted =
                CustomerAppointmentKafkaDto.builder().id(3L).deleted(true).build();
        final ReadOnlyKeyValueStore<Long, CustomerAppointmentKafkaDto> store = store(
                KeyValue.pair(2L, later),
                KeyValue.pair(9L, null),
                KeyValue.pair(3L, deleted),
                KeyValue.pair(1L, earlier));
        when(store.get(1L)).thenReturn(earlier);
        when(store.get(3L)).thenReturn(deleted);
        final CustomerAppointmentKTableProcessor processor = mock(CustomerAppointmentKTableProcessor.class);
        when(processor.getCustomerAppointments()).thenReturn(store);
        final CustomerAppointmentRepository repository = new CustomerAppointmentRepository(processor);

        assertThat(repository.findAllAppointments()).containsExactly(earlier, later, deleted);
        assertThat(repository.findActiveAppointments()).containsExactly(earlier, later);
        assertThat(repository.findActiveAppointment(1L)).contains(earlier);
        assertThat(repository.findActiveAppointment(3L)).isEmpty();
        assertThat(repository.findAppointment(99L)).isEmpty();
    }

    @Test
    void privateAppointmentsAreSortedAndSoftDeletedAppointmentsStayHidden() {
        final UserAppointmentKafkaDto active = UserAppointmentKafkaDto.builder()
                .id(1L)
                .start(LocalDateTime.of(2026, java.time.Month.JULY, 12, 10, 0))
                .build();
        final UserAppointmentKafkaDto deleted = UserAppointmentKafkaDto.builder()
                .id(2L)
                .start(LocalDateTime.of(2026, java.time.Month.JULY, 13, 10, 0))
                .deleted(true)
                .build();
        final ReadOnlyKeyValueStore<Long, UserAppointmentKafkaDto> store =
                store(KeyValue.pair(2L, deleted), KeyValue.pair(1L, active));
        when(store.get(1L)).thenReturn(active);
        when(store.get(2L)).thenReturn(deleted);
        final UserAppointmentKTableProcessor processor = mock(UserAppointmentKTableProcessor.class);
        when(processor.getUserAppointments()).thenReturn(store);
        final UserAppointmentRepository repository = new UserAppointmentRepository(processor);

        assertThat(repository.findAllAppointments()).containsExactly(active, deleted);
        assertThat(repository.findActiveAppointment(1L)).contains(active);
        assertThat(repository.findActiveAppointment(2L)).isEmpty();
        assertThat(repository.findAppointment(99L)).isEmpty();
    }

    @Test
    void contactsFilterNullAndSoftDeletedRecords() {
        final CustomerKafkaDto active = CustomerKafkaDto.builder().id(1L).build();
        final CustomerKafkaDto deleted =
                CustomerKafkaDto.builder().id(2L).deleted(true).build();
        final ReadOnlyKeyValueStore<Long, CustomerKafkaDto> store =
                store(KeyValue.pair(1L, active), KeyValue.pair(9L, null), KeyValue.pair(2L, deleted));
        when(store.get(1L)).thenReturn(active);
        when(store.get(2L)).thenReturn(deleted);
        final CustomerKTableProcessor processor = mock(CustomerKTableProcessor.class);
        when(processor.getCustomers()).thenReturn(store);
        final CustomerContactRepository repository = new CustomerContactRepository(processor);

        assertThat(repository.findAllContacts()).containsExactly(active, deleted);
        assertThat(repository.findActiveContacts()).containsExactly(active);
        assertThat(repository.findActiveContact(1L)).contains(active);
        assertThat(repository.findActiveContact(2L)).isEmpty();
        assertThat(repository.findContact(99L)).isEmpty();
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private static <K, V> ReadOnlyKeyValueStore<K, V> store(KeyValue<K, V>... entries) {
        final ReadOnlyKeyValueStore<K, V> store = mock(ReadOnlyKeyValueStore.class);
        when(store.all()).thenAnswer(ignored -> iterator(Arrays.asList(entries).iterator()));
        return store;
    }

    private static <K, V> KeyValueIterator<K, V> iterator(Iterator<KeyValue<K, V>> delegate) {
        return new KeyValueIterator<>() {
            @Override
            public void close() {
                // The iterator only wraps an in-memory list and owns no closeable resource.
            }

            @Override
            public K peekNextKey() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public KeyValue<K, V> next() {
                return delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
            }
        };
    }
}
