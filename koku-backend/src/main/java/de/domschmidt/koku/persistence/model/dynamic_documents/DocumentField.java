package de.domschmidt.koku.persistence.model.dynamic_documents;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import de.domschmidt.koku.persistence.model.enums.Alignment;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_field", schema = "koku")
public class DocumentField extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    Integer positionIndex;

    @ManyToOne(cascade = CascadeType.PERSIST)
    public DocumentRow row;

    @ManyToOne(cascade = CascadeType.ALL)
    public FieldDefinitionType fieldDefinitionType;

    @Enumerated(EnumType.STRING)
    Alignment alignment;
    Integer xs;
    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;

    // copy constructor
    public DocumentField(final DocumentRow documentRow, final DocumentField fieldToBeCopied) {
        this.id = null;
        this.positionIndex = fieldToBeCopied.getPositionIndex();
        this.row = documentRow;
        this.alignment = fieldToBeCopied.getAlignment();
        this.xs = fieldToBeCopied.getXs();
        this.sm = fieldToBeCopied.getSm();
        this.md = fieldToBeCopied.getMd();
        this.lg = fieldToBeCopied.getLg();
        this.xl = fieldToBeCopied.getXl();
        final FieldDefinitionType fieldDefintionTypeToBeCopied = fieldToBeCopied.getFieldDefinitionType();
        if (fieldDefintionTypeToBeCopied instanceof SVGFieldDefinitionType) {
            this.fieldDefinitionType = new SVGFieldDefinitionType((SVGFieldDefinitionType) fieldDefintionTypeToBeCopied);
        } else if (fieldDefintionTypeToBeCopied instanceof SignatureFieldDefinitionType) {
            this.fieldDefinitionType = new SignatureFieldDefinitionType((SignatureFieldDefinitionType) fieldDefintionTypeToBeCopied);
        } else if (fieldDefintionTypeToBeCopied instanceof TextFieldDefinitionType) {
            this.fieldDefinitionType = new TextFieldDefinitionType((TextFieldDefinitionType) fieldDefintionTypeToBeCopied);
        } else if (fieldDefintionTypeToBeCopied instanceof CheckboxFieldDefinitionType) {
            this.fieldDefinitionType = new CheckboxFieldDefinitionType((CheckboxFieldDefinitionType) fieldDefintionTypeToBeCopied);
        }
    }
}
