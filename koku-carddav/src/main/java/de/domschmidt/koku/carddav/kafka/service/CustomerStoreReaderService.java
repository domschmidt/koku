package de.domschmidt.koku.carddav.kafka.service;

import de.domschmidt.koku.carddav.kafka.customers.KafkaCustomersConstants;
import de.domschmidt.koku.carddav.kafka.customers.dto.CustomerDto;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerStoreReaderService {

    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public CustomerStoreReaderService(
            final StreamsBuilderFactoryBean factoryBean
    ) {
        this.factoryBean = factoryBean;
    }

    public Map<Long, CustomerDto> getCustomerStore() {
        final Map<Long, CustomerDto> result = new HashMap<>();
        this.factoryBean.getKafkaStreams().store(StoreQueryParameters.fromNameAndType(KafkaCustomersConstants.STORE_CUSTOMERS, QueryableStoreTypes.keyValueStore()));
        final ReadOnlyKeyValueStore<Long, CustomerDto> customersStore = factoryBean.getKafkaStreams()
                .store(StoreQueryParameters.fromNameAndType(KafkaCustomersConstants.STORE_CUSTOMERS, QueryableStoreTypes.keyValueStore()));
        final KeyValueIterator<Long, CustomerDto> customersStoreIterator = customersStore.all();
        while (customersStoreIterator.hasNext()) {
            final KeyValue<Long, CustomerDto> currentItem = customersStoreIterator.next();
            result.put(currentItem.key, currentItem.value);
        }
        return result;
    }

}
