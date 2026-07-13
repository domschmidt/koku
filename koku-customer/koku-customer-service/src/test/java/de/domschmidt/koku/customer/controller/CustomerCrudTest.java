package de.domschmidt.koku.customer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKafkaService;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerRepository;
import de.domschmidt.koku.customer.transformer.CustomerToCustomerDtoTransformer;
import de.domschmidt.koku.dto.customer.KokuCustomerDto;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class CustomerCrudTest {
    private final EntityManager entityManager = mock(EntityManager.class);
    private final CustomerRepository repository = mock(CustomerRepository.class);
    private final CustomerToCustomerDtoTransformer transformer = mock(CustomerToCustomerDtoTransformer.class);
    private final CustomerKafkaService kafkaService = mock(CustomerKafkaService.class);
    private CustomerController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerController(entityManager, repository, transformer, kafkaService);
    }

    @Test
    void readSummaryAndMissingPathsAreDefined() {
        final Customer customer = customer(false, 2L);
        when(repository.findById(5L)).thenReturn(Optional.of(customer));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(customer))
                .thenReturn(KokuCustomerDto.builder().id(5L).build());
        assertThat(controller.read(5L).getId()).isEqualTo(5L);
        assertThat(controller.readSummary(5L).getId()).isEqualTo(5L);
        assertThatThrownBy(() -> controller.read(6L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.readSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void staleUpdateRequiresForce() throws Exception {
        final Customer customer = customer(false, 3L);
        final KokuCustomerDto update = KokuCustomerDto.builder().version(2L).build();
        when(entityManager.getReference(Customer.class, 5L)).thenReturn(customer);
        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(customer, update);
        verify(kafkaService).sendCustomer(customer);
    }

    @Test
    void lifecycleAndCreateContractsAreDefined() {
        final Customer customer = customer(false, 0L);
        when(entityManager.getReference(Customer.class, 5L)).thenReturn(customer);
        controller.delete(5L);
        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        controller.restore(5L);
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);

        final KokuCustomerDto input = KokuCustomerDto.builder().firstName("Ada").build();
        when(transformer.transformToEntity(any(Customer.class), org.mockito.Mockito.same(input)))
                .thenReturn(customer);
        when(repository.saveAndFlush(customer)).thenReturn(customer);
        controller.create(input);
        verify(transformer, org.mockito.Mockito.atLeastOnce()).transformToDto(customer);
    }

    @Test
    void kafkaFailureIsExposed() throws Exception {
        final Customer customer = customer(false, 2L);
        when(kafkaService.sendCustomer(customer)).thenThrow(new ExecutionException(new IllegalStateException()));
        assertThatThrownBy(() -> controller.sendCustomerUpdate(customer)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void kafkaInterruptRestoresThreadStatusAndIsExposed() throws Exception {
        final Customer customer = customer(false, 2L);
        when(kafkaService.sendCustomer(customer)).thenThrow(new InterruptedException("stopped"));

        assertThatThrownBy(() -> controller.sendCustomerUpdate(customer)).isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static Customer customer(boolean deleted, Long version) {
        final Customer customer = new Customer();
        customer.setId(5L);
        customer.setFirstname("Ada");
        customer.setLastname("Lovelace");
        customer.setVersion(version);
        customer.setDeleted(deleted);
        return customer;
    }
}
