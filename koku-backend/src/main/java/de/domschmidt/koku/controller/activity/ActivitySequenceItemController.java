package de.domschmidt.koku.controller.activity;

import de.domschmidt.koku.dto.activity.ActivitySequenceItemDto;
import de.domschmidt.koku.persistence.model.ActivityStep;
import de.domschmidt.koku.persistence.model.Product;
import de.domschmidt.koku.service.IActivityStepService;
import de.domschmidt.koku.service.IProductService;
import de.domschmidt.koku.service.searchoptions.ActivitySequenceSearchOptions;
import de.domschmidt.koku.service.searchoptions.ActivityStepSearchOptions;
import de.domschmidt.koku.service.searchoptions.ProductSearchOptions;
import de.domschmidt.koku.transformer.ActivitySequenceItemToIActivitySequenceItemDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activitysequences")
public class ActivitySequenceItemController {

    private final ActivitySequenceItemToIActivitySequenceItemDtoTransformer transformer;
    private final IProductService productService;
    private final IActivityStepService activityStepService;

    @Autowired
    public ActivitySequenceItemController(final IActivityStepService activityStepService,
                                          final IProductService productService,
                                          final ActivitySequenceItemToIActivitySequenceItemDtoTransformer transformer) {
        this.transformer = transformer;
        this.productService = productService;
        this.activityStepService = activityStepService;
    }

    @GetMapping
    public List<ActivitySequenceItemDto> findAll(final ActivitySequenceSearchOptions searchOptions) {
        final List<ActivityStep> allActivitySteps = this.activityStepService.findAll(ActivityStepSearchOptions.builder()
                .search(searchOptions.getSearch())
                .build());
        final List<Product> allProducts = this.productService.findAll(ProductSearchOptions.builder()
                .search(searchOptions.getSearch())
                .build());

        return this.transformer.combineAndTransformLists(allActivitySteps, allProducts);
    }
}
