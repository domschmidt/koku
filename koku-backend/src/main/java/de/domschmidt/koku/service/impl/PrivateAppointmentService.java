package de.domschmidt.koku.service.impl;

import com.google.common.collect.Lists;
import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.dao.PrivateAppointmentRepository;
import de.domschmidt.koku.persistence.model.PrivateAppointment;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.service.IPrivateAppointmentService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.PrivateAppointmentSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PrivateAppointmentService extends AbstractService<PrivateAppointment, PrivateAppointmentSearchOptions> implements IPrivateAppointmentService {

    private final PrivateAppointmentRepository privateAppointmentRepository;
    private final KokuUserRepository kokuUserRepository;

    public PrivateAppointmentService(final PrivateAppointmentRepository privateAppointmentRepository,
                                     final KokuUserRepository kokuUserRepository) {
        this.privateAppointmentRepository = privateAppointmentRepository;
        this.kokuUserRepository = kokuUserRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<PrivateAppointment, Long> getDao() {
        return this.privateAppointmentRepository;
    }

    @Override
    public List<PrivateAppointment> findAllMyNextAppointments(final LocalDateTime from, final LocalDateTime until) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return this.privateAppointmentRepository
                .findAllByUser_UsernameEqualsIgnoreCaseAndStartIsGreaterThanEqualAndStartLessThanEqualAndDeletedIsFalse(
                        authentication.getName(),
                        from,
                        until
                );
    }

    @Override
    public PrivateAppointment update(PrivateAppointment entity) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Optional<KokuUser> kokuUserOptional = this.kokuUserRepository.findByUsernameEqualsIgnoreCase(authentication.getName());
        if (kokuUserOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            entity.setUser(kokuUserOptional.get());
            return super.update(entity);
        }
    }

    @Override
    public PrivateAppointment create(PrivateAppointment entity) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Optional<KokuUser> kokuUserOptional = this.kokuUserRepository.findByUsernameEqualsIgnoreCase(authentication.getName());
        if (kokuUserOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            entity.setUser(kokuUserOptional.get());
            return super.create(entity);
        }
    }

    @Override
    public void deleteById(long entityId) {
        final PrivateAppointment privateAppointment = findById(entityId);
        privateAppointment.setDeleted(true);
        update(privateAppointment);
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<PrivateAppointment> findAll(final PrivateAppointmentSearchOptions privateAppointmentSearchOptions) {
        return Lists.newArrayList(getDao().findAll());
    }

}
