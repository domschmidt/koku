package de.domschmidt.koku.carddav.kafka.customers.config;

import de.domschmidt.koku.carddav.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.carddav.kafka.customers.dto.CustomerDto;
import de.domschmidt.koku.carddav.kafka.customers.dto.CustomerDtoSerdes;
import de.domschmidt.koku.carddav.kafka.customers.service.CustomersKTableProcessor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class CustomerKafkaConfiguration {

    private final KafkaConfiguration kafkaConfiguration;
    private final String customersTopic;
    private final CustomersKTableProcessor ktableProcessorCustomers;
    private final String streamsApplicationId;
    private final String consumerGroupInstanceId;

    @Autowired
    public CustomerKafkaConfiguration(
            final KafkaConfiguration kafkaConfiguration,
            final CustomersKTableProcessor ktableProcessorCustomers,
            final @Value("${kafka.customers.topic}") String customersTopic,
            final @Value("${carddav.kafka.streams.application-id}") String streamsApplicationId,
            final @Value("${carddav.kafka.group-instance-id}") String consumerGroupInstanceId
    ) {
        this.kafkaConfiguration = kafkaConfiguration;
        this.ktableProcessorCustomers = ktableProcessorCustomers;
        this.customersTopic = customersTopic;
        this.streamsApplicationId = streamsApplicationId;
        this.consumerGroupInstanceId = consumerGroupInstanceId;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration customersStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, this.streamsApplicationId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfiguration.getBootstrapAddress());
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, new CustomerDtoSerdes().getClass());
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, this.consumerGroupInstanceId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public ConsumerFactory<Long, CustomerDto> customerConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                this.kafkaConfiguration.getBootstrapAddress()
        );
        props.put(ConsumerConfig.GROUP_ID_CONFIG, this.consumerGroupInstanceId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        JsonDeserializer<CustomerDto> customerDtoJsonDeserializer = new JsonDeserializer<>(CustomerDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(props, new LongDeserializer(), customerDtoJsonDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, CustomerDto>
    customerKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<Long, CustomerDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(customerConsumerFactory());
        return factory;
    }

    @Bean
    public KStream<Long, CustomerDto> kStream(StreamsBuilder kStreamBuilder) {
        final KStream<Long, CustomerDto> stream = kStreamBuilder.stream(this.customersTopic, Consumed.with(Serdes.Long(), new CustomerDtoSerdes()));
        this.ktableProcessorCustomers.process(stream);
        return stream;
    }

}