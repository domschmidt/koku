package de.domschmidt.koku.persistence.model.dynamic_documents;

import de.domschmidt.koku.dto.formular.FontSizeDto;
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
    boolean value;
    boolean readOnly;
    String label;
    @Enumerated(EnumType.STRING)
    FontSize fontSize;

    // copy constructor
    public CheckboxFieldDefinitionType(final CheckboxFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        this.context = fieldDefintionTypeToBeCopied.getContext();
        this.label = fieldDefintionTypeToBeCopied.getLabel();
        this.value = fieldDefintionTypeToBeCopied.isValue();
        this.fontSize = fieldDefintionTypeToBeCopied.getFontSize();
        this.readOnly = fieldDefintionTypeToBeCopied.isReadOnly();
    }
}
