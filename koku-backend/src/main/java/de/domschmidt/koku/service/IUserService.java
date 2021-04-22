package de.domschmidt.koku.service;

import de.domschmidt.koku.persistence.IOperations;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.service.searchoptions.KokuUserSearchOptions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IUserService extends IOperations<KokuUser, KokuUserSearchOptions> {

    @Override
    @Transactional(readOnly = true)
    List<KokuUser> findAll(final KokuUserSearchOptions userSearchOptions);

    @Override
    @Transactional(readOnly = true)
    KokuUser findById(final long id);
}