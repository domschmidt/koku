package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.CustomerRepository;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.service.ICustomerService;
import de.domschmidt.koku.service.common.AbstractService;
import de.domschmidt.koku.service.searchoptions.CustomerSearchOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CustomerService extends AbstractService<Customer, CustomerSearchOptions> implements ICustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // API

    @Override
    protected PagingAndSortingRepository<Customer, Long> getDao() {
        return this.customerRepository;
    }

    @Override
    public List<Customer> findAllCustomersWithBirthdayBetween(final LocalDate from, final LocalDate until) {
        final int dayFrom = from.getDayOfMonth();
        final int monthFrom = from.getMonthValue();
        final int dayUntil = until.getDayOfMonth();
        final int monthUntil = until.getMonthValue();
        final String fromStr = StringUtils.leftPad(String.valueOf(monthFrom), 2, "0") + StringUtils.leftPad(String.valueOf(dayFrom), 2, "0");
        final String untilStr = StringUtils.leftPad(String.valueOf(monthUntil), 2, "0") + StringUtils.leftPad(String.valueOf(dayUntil), 2, "0");

        return this.customerRepository.findAllByBirthdayDayMonth(fromStr, untilStr);
    }

    // overridden to be secured

    @Override
    @Transactional(readOnly = true)
    public List<Customer> findAll(final CustomerSearchOptions customerSearchOptions) {
        return this.customerRepository.findAllByDeletedIsFalseAndFirstNameContainingIgnoreCaseOrDeletedIsFalseAndLastNameContainingIgnoreCaseOrderByLastNameAsc(
                customerSearchOptions.getSearch(),
                customerSearchOptions.getSearch()
        );
    }

}
