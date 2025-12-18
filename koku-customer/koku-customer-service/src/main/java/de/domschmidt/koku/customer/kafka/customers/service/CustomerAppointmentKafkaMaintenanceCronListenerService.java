package de.domschmidt.koku.customer.kafka.customers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerAppointmentKafkaMaintenanceCronListenerService {

    final CustomerAppointmentKafkaMaintenanceService executor;

    @Scheduled(cron = "0 0 0 * * *")
    public void execute() {
        executor.runMaintenance();
    }

}
