package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findAllByNameContainingIgnoreCaseAndDeletedIsFalseOrderByNameAsc(String description);

    @Query("select p from Promotion p" +
            " where p.deleted = FALSE" +
            " AND (p.startDate <= ?2 OR p.startDate IS NULL)" +
            " AND (p.endDate >= ?2 OR p.endDate IS NULL)" +
            " AND upper(p.name) like upper(?1)"
    )
    List<Promotion> findAllByNameLikeIgnoreCaseAndDeletedIsFalseAndStartDateBeforeOrEqualToAndEndDateAfterOrEqualTo(
            String description,
            LocalDate now
    );

}
