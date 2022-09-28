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

@DiscriminatorValue("QRCODE")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_qrcode", schema = "koku")
public class QRCodeFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;

    String content;

    // copy constructor
    public QRCodeFieldDefinitionType(final QRCodeFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        this.content = fieldDefintionTypeToBeCopied.getContent();
    }

}
