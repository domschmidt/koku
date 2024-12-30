package de.domschmidt.koku.carddav.kafka.customers.service;

import de.domschmidt.koku.carddav.kafka.customers.KafkaCustomersConstants;
import de.domschmidt.koku.carddav.kafka.customers.dto.CustomerDto;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.springframework.stereotype.Component;

@Component
public class CustomersKTableProcessor {

    public void process(final KStream<Long, CustomerDto> stream) {
        final KeyValueBytesStoreSupplier customers = Stores.persistentKeyValueStore(KafkaCustomersConstants.STORE_CUSTOMERS);
        final KTable<Long, CustomerDto> customerTable = stream.toTable();
        customerTable.mapValues(value -> value, Materialized.as(customers));
    }

}