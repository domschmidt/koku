package de.domschmidt.koku.service.common;

import com.google.common.collect.Lists;
import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.common.DomainModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public abstract class AbstractService<T extends DomainModel, SearchOptions> implements IOperations<T, SearchOptions> {

    // read - one

    @Override
    @Transactional(readOnly = true)
    public T findById(final long id) {
        return getDao().findById(id).orElse(null);
    }

    // read - all

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(SearchOptions searchOptions) {
        return Lists.newArrayList(getDao().findAll());
    }

    // write

    @Override
    public T create(final T entity) {
        return getDao().save(entity);
    }

    @Override
    public T update(final T entity) {
        return getDao().save(entity);
    }

    @Override
    public void deleteById(final long entityId) {
        getDao().deleteById(entityId);
    }

    protected abstract PagingAndSortingRepository<T, Long> getDao();

}