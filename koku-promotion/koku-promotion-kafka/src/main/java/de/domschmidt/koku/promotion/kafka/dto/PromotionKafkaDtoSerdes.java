package de.domschmidt.koku.promotion.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class PromotionKafkaDtoSerdes extends Serdes.WrapperSerde<PromotionKafkaDto> {

    public PromotionKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<PromotionKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<PromotionKafkaDto> promotionDtoJsonDeserializer =
                new JacksonJsonDeserializer<>(PromotionKafkaDto.class);
        promotionDtoJsonDeserializer.setUseTypeHeaders(false);
        return promotionDtoJsonDeserializer;
    }
}
