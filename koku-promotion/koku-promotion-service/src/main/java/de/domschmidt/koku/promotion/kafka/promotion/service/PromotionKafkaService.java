package de.domschmidt.koku.promotion.kafka.promotion.service;

import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import de.domschmidt.koku.promotion.kafka.promotion.transformer.PromotionToKafkaPromotionDtoTransformer;
import de.domschmidt.koku.promotion.persistence.Promotion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PromotionKafkaService {

    private final KafkaTemplate<Long, PromotionKafkaDto> promotionKafkaTemplate;

    @Autowired
    public PromotionKafkaService(
            final KafkaTemplate<Long, PromotionKafkaDto> promotionKafkaTemplate
    ) {
        this.promotionKafkaTemplate = promotionKafkaTemplate;
    }

    public SendResult<Long, PromotionKafkaDto> sendPromotion(
            final Promotion promotion
    ) throws ExecutionException, InterruptedException, TimeoutException {
        return this.promotionKafkaTemplate.send(
                PromotionKafkaDto.TOPIC,
                promotion.getId(),
                new PromotionToKafkaPromotionDtoTransformer().transformToDto(promotion)
        ).get(
                10, TimeUnit.SECONDS
        );
    }
}
