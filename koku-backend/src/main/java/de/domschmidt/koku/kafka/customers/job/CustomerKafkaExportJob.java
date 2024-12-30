package de.domschmidt.koku.kafka.customers.job;

import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.koku.kafka.customers.service.CustomerKafkaService;
import de.domschmidt.koku.persistence.dao.CustomerRepository;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.QCustomer;
import de.domschmidt.koku.transformer.CustomerToCustomerDtoTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CustomerKafkaExportJob {

    private final EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final CustomerKafkaService customerKafkaService;

    public CustomerKafkaExportJob(
            final EntityManager entityManager,
            final CustomerRepository customerRepository,
            final CustomerKafkaService customerKafkaService
            ) {
        this.entityManager = entityManager;
        this.customerRepository = customerRepository;
        this.customerKafkaService = customerKafkaService;
    }

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    @Transactional()
    public void runCustomerExportJob() {
        log.info("Started Customer Kafka Export Job");

        List<Customer> currentBatch = getNextCustomerBatch();
        while(!currentBatch.isEmpty()) {
            for (final Customer currentCustomer : currentBatch) {
                try {
                    this.customerKafkaService.sendCustomer(new CustomerToCustomerDtoTransformer().transformToDto(currentCustomer));
                    currentCustomer.setKafkaExported(LocalDateTime.now());
                } catch (final Exception e) {
                    log.error("Unable to send customer thru kafka", e);
                }
            }
            customerRepository.saveAll(currentBatch);
            currentBatch = getNextCustomerBatch();
        }

        log.info("Ended Customer Kafka Export Job");
    }

    private List<Customer> getNextCustomerBatch() {
        final QCustomer qCustomer = QCustomer.customer;
        return new JPAQuery<>(this.entityManager)
                .select(qCustomer)
                .from(qCustomer)
                .where(
                        qCustomer.kafkaExported.isNull()
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .orderBy(qCustomer.recorded.asc())
                .limit(10)
                .fetch();
    }

}
