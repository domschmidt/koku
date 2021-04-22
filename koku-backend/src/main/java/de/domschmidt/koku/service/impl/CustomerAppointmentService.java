package de.domschmidt.koku.service.impl;

import com.google.common.collect.Lists;
import de.domschmidt.koku.persistence.dao.CustomerAppointmentRepository;
import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.service.ICustomerAppointmentService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.CustomerAppointmentSearchOptions;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CustomerAppointmentService extends AbstractService<CustomerAppointment, CustomerAppointmentSearchOptions> implements ICustomerAppointmentService {

    private final CustomerAppointmentRepository customerAppointmentRepository;
    private final KokuUserRepository userRepository;

    public CustomerAppointmentService(final CustomerAppointmentRepository customerAppointmentRepository,
                                      final KokuUserRepository userRepository) {
        this.customerAppointmentRepository = customerAppointmentRepository;
        this.userRepository = userRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<CustomerAppointment, Long> getDao() {
        return this.customerAppointmentRepository;
    }

    @Override
    public CustomerAppointment create(CustomerAppointment entity) {
        final CustomerAppointment result = super.create(entity);
        if (result.getUser() != null && result.getUser().getId() != null) {
            // refresh user after save
            result.setUser(this.userRepository.findById(result.getUser().getId()).get());
        }
        return result;
    }

    @Override
    public List<CustomerAppointment> findAllAppointments(final CustomerAppointmentSearchOptions searchOptions) {
        final Optional<KokuUser> kokuUser;
        if (searchOptions.getUserId() != null) {
            kokuUser = this.userRepository.findById(searchOptions.getUserId());
        } else {
            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            kokuUser = this.userRepository.findByUsernameEqualsIgnoreCase(authentication.getName());
        }
        if (kokuUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return this.customerAppointmentRepository
                    .findAllByUserEqualsAndStartIsGreaterThanEqualAndStartLessThanEqual(
                            kokuUser.get(),
                            searchOptions.getStart().atStartOfDay(),
                            searchOptions.getEnd().atTime(LocalTime.MAX)
                    );
        }
    }

    @Override
    public List<CustomerAppointment> findAllAppointmentsOfAllUsers(final CustomerAppointmentSearchOptions searchOptions) {
        return this.customerAppointmentRepository
                .findAllByStartIsGreaterThanEqualAndStartLessThanEqual(
                        searchOptions.getStart().atStartOfDay(),
                        searchOptions.getEnd().atTime(LocalTime.MAX)
                );
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAppointment> findAll(final CustomerAppointmentSearchOptions customerAppointmentSearchOptions) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Optional<KokuUser> kokuUserOptional = this.userRepository.findByUsernameEqualsIgnoreCase(authentication.getName());
        if (kokuUserOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            return Lists.newArrayList(this.customerAppointmentRepository.findAllByUserEquals(kokuUserOptional.get()));
        }
    }

}
