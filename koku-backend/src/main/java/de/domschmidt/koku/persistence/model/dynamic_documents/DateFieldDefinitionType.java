package de.domschmidt.koku.persistence.model.dynamic_documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter

@DiscriminatorValue("DATE")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_date", schema = "koku")
public class DateFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String context;

    LocalDate value;
    Integer fontSize;
    boolean readOnly;

    Integer dayDiff;
    Integer monthDiff;
    Integer yearDiff;

    // copy constructor
    public DateFieldDefinitionType(final DateFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        this.context = fieldDefintionTypeToBeCopied.getContext();
        this.value = fieldDefintionTypeToBeCopied.getValue();
        this.fontSize = fieldDefintionTypeToBeCopied.getFontSize();
        this.readOnly = fieldDefintionTypeToBeCopied.isReadOnly();
        this.dayDiff = fieldDefintionTypeToBeCopied.getDayDiff();
        this.monthDiff = fieldDefintionTypeToBeCopied.getMonthDiff();
        this.yearDiff = fieldDefintionTypeToBeCopied.getYearDiff();
    }

}
