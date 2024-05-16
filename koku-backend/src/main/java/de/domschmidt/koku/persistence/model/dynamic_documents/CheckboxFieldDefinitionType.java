package de.domschmidt.koku.persistence.model.dynamic_documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter

@DiscriminatorValue("CHECKBOX")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_checkbox", schema = "koku")
public class CheckboxFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String context;
    Boolean value;
    Boolean readOnly;
    String label;
    Integer fontSize;

    // copy constructor
    public CheckboxFieldDefinitionType(final CheckboxFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        this.context = fieldDefintionTypeToBeCopied.getContext();
        this.value = Boolean.TRUE.equals(fieldDefintionTypeToBeCopied.getValue());
        this.fontSize = fieldDefintionTypeToBeCopied.getFontSize();
        this.label = fieldDefintionTypeToBeCopied.getLabel();
        this.readOnly = Boolean.TRUE.equals(fieldDefintionTypeToBeCopied.getReadOnly());
    }
}
