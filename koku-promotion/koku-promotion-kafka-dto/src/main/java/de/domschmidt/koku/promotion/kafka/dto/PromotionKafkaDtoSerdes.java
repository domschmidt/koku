package de.domschmidt.koku.promotion.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class PromotionKafkaDtoSerdes extends Serdes.WrapperSerde<PromotionKafkaDto> {

    public PromotionKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<PromotionKafkaDto> getDeserializer() {
        final JsonDeserializer<PromotionKafkaDto> promotionDtoJsonDeserializer = new JsonDeserializer<>(PromotionKafkaDto.class);
        promotionDtoJsonDeserializer.setUseTypeHeaders(false);
        return promotionDtoJsonDeserializer;
    }

}
