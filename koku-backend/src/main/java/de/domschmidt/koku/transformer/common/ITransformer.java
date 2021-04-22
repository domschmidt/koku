package de.domschmidt.koku.transformer.common;

import de.domschmidt.koku.persistence.model.common.DomainModel;

import java.util.List;

public interface ITransformer<DomainModelInst extends DomainModel, DtoModel> {

    List<DtoModel> transformToDtoList(final List<DomainModelInst> modelList);

    DtoModel transformToDto(final DomainModelInst model);

    DomainModelInst transformToEntity(final DtoModel dtoModel);
}
