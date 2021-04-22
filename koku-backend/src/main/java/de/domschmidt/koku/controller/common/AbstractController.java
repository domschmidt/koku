package de.domschmidt.koku.controller.common;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.common.DomainModel;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class AbstractController<DomainModelInst extends DomainModel, DtoModel, SearchOptions> {

    protected final IOperations<DomainModelInst, SearchOptions> service;
    protected final ITransformer<DomainModelInst, DtoModel> transformer;

    public AbstractController(final IOperations<DomainModelInst, SearchOptions> service,
                              final ITransformer<DomainModelInst, DtoModel> transformer) {
        this.service = service;
        this.transformer = transformer;
    }

    @Transactional(readOnly = true)
    protected List<DtoModel> findAll(SearchOptions searchOptions) {
        final List<DomainModelInst> models = this.service.findAll(searchOptions);
        return this.transformer.transformToDtoList(models);
    }

    @Transactional(readOnly = true)
    protected DtoModel findByIdTransformed(final Long id) {
        final DomainModelInst model = findById(id);
        if (model == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return this.transformer.transformToDto(model);
    }

    protected DomainModelInst findById(Long id) {
        return this.service.findById(id);
    }

    protected DtoModel create(final DtoModel newDto) {
        final DomainModelInst model = this.transformer.transformToEntity(newDto);
        final DomainModelInst savedModel = this.service.create(model);
        return this.transformer.transformToDto(savedModel);
    }

    protected void update(final Long id, final DtoModel updatedDto) {
        final DomainModelInst model = this.transformer.transformToEntity(updatedDto);
        this.service.update(model);
    }

    protected void delete(final Long id) {
        this.service.deleteById(id);
    }
}
