package de.domschmidt.koku.controller.activity;

import de.domschmidt.koku.controller.common.AbstractController;
import de.domschmidt.koku.dto.activity.ActivityStepDto;
import de.domschmidt.koku.persistence.model.ActivityStep;
import de.domschmidt.koku.service.IActivityStepService;
import de.domschmidt.koku.service.searchoptions.ActivityStepSearchOptions;
import de.domschmidt.koku.transformer.ActivityStepToActivityStepDtoTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activitysteps")
public class ActivityStepController extends AbstractController<ActivityStep, ActivityStepDto, ActivityStepSearchOptions> {

    @Autowired
    public ActivityStepController(final IActivityStepService activityStepService,
                                  final ActivityStepToActivityStepDtoTransformer transformer) {
        super(activityStepService, transformer);
    }

    @GetMapping
    public List<ActivityStepDto> findAll(final ActivityStepSearchOptions searchOptions) {
        return super.findAll(searchOptions);
    }

    @GetMapping(value = "/{id}")
    public ActivityStepDto findByIdTransformed(@PathVariable("id") Long id) {
        return super.findByIdTransformed(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityStepDto create(@RequestBody ActivityStepDto newActivity) {
        return super.create(newActivity);
    }

    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void update(@PathVariable("id") Long id, @RequestBody ActivityStepDto updatedDto) {
        super.update(id, updatedDto);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        final ActivityStep activityStep = super.findById(id);
        activityStep.setDeleted(true);
        this.service.update(activityStep);
    }
}
