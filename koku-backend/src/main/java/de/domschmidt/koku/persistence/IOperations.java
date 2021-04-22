package de.domschmidt.koku.persistence;

import de.domschmidt.koku.persistence.model.common.DomainModel;

import java.util.List;

public interface IOperations<T extends DomainModel, SearchOptions> {

    // read - one

    T findById(final long id);

    // read - all

    List<T> findAll(final SearchOptions searchOptions);

    // write

    T create(final T entity);

    T update(final T entity);

    void deleteById(final long entityId);
}