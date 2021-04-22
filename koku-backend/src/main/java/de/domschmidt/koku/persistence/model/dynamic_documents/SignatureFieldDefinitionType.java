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

@DiscriminatorValue("SIGNATURE")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_signature", schema = "koku")
public class SignatureFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    // copy constructor
    public SignatureFieldDefinitionType(final SignatureFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
    }
}
