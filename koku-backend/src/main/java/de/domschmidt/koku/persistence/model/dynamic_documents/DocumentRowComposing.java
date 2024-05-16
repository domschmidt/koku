package de.domschmidt.koku.persistence.model.dynamic_documents;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_row_composing", schema = "koku")
public class DocumentRowComposing extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    Integer positionIndex;

    @ManyToOne(cascade = CascadeType.ALL)
    DocumentRow row;

    @ManyToOne
    DynamicDocument document;

    // copy constructor
    public DocumentRowComposing(final DynamicDocument document, final DocumentRowComposing rowToBeCopied) {
        this.id = null;
        this.document = document;
        this.positionIndex = rowToBeCopied.getPositionIndex();
        this.row = new DocumentRow(rowToBeCopied.getRow());
    }
}
