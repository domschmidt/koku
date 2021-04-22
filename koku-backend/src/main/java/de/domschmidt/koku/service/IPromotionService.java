package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.Promotion;
import de.domschmidt.koku.service.searchoptions.PromotionSearchOptions;

import java.util.List;

public interface IPromotionService extends IOperations<Promotion, PromotionSearchOptions> {

    List<Promotion> findAll(final PromotionSearchOptions promotionSearchOptions);

}