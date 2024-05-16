package de.domschmidt.koku.controller.activity;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.activity.ActivityCategoryDto;
import de.domschmidt.koku.persistence.model.ActivityCategory;
import de.domschmidt.koku.service.IActivityCategoryService;
import de.domschmidt.koku.service.searchoptions.ActivityCategorySearchOptions;
import de.domschmidt.koku.transformer.ActivityCategoryToActivityCategoryDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities/categories")
public class ActivityCategoryController extends AbstractController<ActivityCategory, ActivityCategoryDto, ActivityCategorySearchOptions> {

    private final ActivityCategoryToActivityCategoryDtoTransformer transformer;

    @Autowired
    public ActivityCategoryController(
            final IActivityCategoryService activityCategoryService,
            final ActivityCategoryToActivityCategoryDtoTransformer transformer
    ) {
        super(activityCategoryService, transformer);
        this.transformer = transformer;
    }

    @GetMapping
    public List<ActivityCategoryDto> findAll(final ActivityCategorySearchOptions searchOptions) {
        final List<ActivityCategory> models = this.service.findAll(searchOptions);
        return this.transformer.transformToDtoList(models);
    }

    @GetMapping(value = "/{id}")
    public ActivityCategoryDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityCategoryDto create(@RequestBody ActivityCategoryDto newActivity) {
        final ActivityCategory model = this.transformer.transformToEntity(newActivity);
        final ActivityCategory savedModel = this.service.create(model);
        return this.transformer.transformToDto(savedModel);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody ActivityCategoryDto updatedDto) {
        final ActivityCategory model = this.transformer.transformToEntity(updatedDto);
        this.service.update(model);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        this.service.deleteById(id);
    }
}
