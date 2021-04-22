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

@DiscriminatorValue("TEXT")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_text", schema = "koku")
public class TextFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String text;
    @Enumerated(EnumType.STRING)
    FontSize fontSize;

    // copy constructor
    public TextFieldDefinitionType(final TextFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        this.text = fieldDefintionTypeToBeCopied.getText();
        this.fontSize = fieldDefintionTypeToBeCopied.getFontSize();
    }
}
