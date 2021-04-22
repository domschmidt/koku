package de.domschmidt.koku.controller.promotion;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.promotion.PromotionDto;
import de.domschmidt.koku.persistence.model.Promotion;
import de.domschmidt.koku.service.IPromotionService;
import de.domschmidt.koku.service.searchoptions.PromotionSearchOptions;
import de.domschmidt.koku.transformer.PromotionToPromotionDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promotions")
public class PromotionController extends AbstractController<Promotion, PromotionDto, PromotionSearchOptions> {

    @Autowired
    public PromotionController(final IPromotionService promotionService,
                               final PromotionToPromotionDtoTransformer transformer) {
        super(promotionService, transformer);
    }

    @GetMapping
    public List<PromotionDto> findAll(final PromotionSearchOptions searchOptions) {
        return super.findAll(searchOptions);
    }

    @GetMapping(value = "/{id}")
    public PromotionDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionDto create(@RequestBody PromotionDto newActivity) {
        return super.create(newActivity);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody PromotionDto updatedDto) {
        super.update(id, updatedDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final Promotion promotion = super.findById(id);
        promotion.setDeleted(true);
        this.service.update(promotion);
    }
}
