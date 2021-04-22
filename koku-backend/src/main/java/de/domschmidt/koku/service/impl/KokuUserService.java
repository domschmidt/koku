package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.service.IUserService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.KokuUserSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KokuUserService extends AbstractService<KokuUser, KokuUserSearchOptions> implements IUserService {

    private final KokuUserRepository userRepository;

    public KokuUserService(final KokuUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<KokuUser, Long> getDao() {
        return this.userRepository;
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<KokuUser> findAll(final KokuUserSearchOptions userSearchOptions) {
        return this.userRepository.findAllByDeletedIsFalseAndUsernameContainingIgnoreCaseOrUserDetails_FirstnameContainingIgnoreCaseOrUserDetails_LastnameContainingIgnoreCaseOrderByUserDetails_FirstnameAsc(userSearchOptions.getSearch(), userSearchOptions.getSearch(), userSearchOptions.getSearch());
    }

}
