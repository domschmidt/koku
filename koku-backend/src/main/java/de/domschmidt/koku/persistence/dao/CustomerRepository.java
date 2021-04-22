package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAllByDeletedIsFalseAndFirstNameContainingIgnoreCaseOrDeletedIsFalseAndLastNameContainingIgnoreCaseOrderByLastNameAsc(
            String firstNameContainingIgnoreCase,
            String lastNameContainingIgnoreCase
    );

    @Query("select c from Customer c" +
            " where c.deleted = FALSE" +
            " AND FUNCTION('to_char', c.birthday, 'MMDD') >= ?1" +
            " AND FUNCTION('to_char', c.birthday, 'MMDD') <= ?2" +
            " OR (?1 > ?2 AND (FUNCTION('to_char', c.birthday, 'MMDD') >= ?1 OR FUNCTION('to_char', c.birthday, 'MMDD') <= ?2))")
    List<Customer> findAllByBirthdayDayMonth(
        String monthDayFrom,
        String monthDayTo
    );
}
