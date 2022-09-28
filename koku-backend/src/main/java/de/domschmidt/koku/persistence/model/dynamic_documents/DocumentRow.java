package de.domschmidt.koku.persistence.model.dynamic_documents;

import de.domschmidt.koku.persistence.model.common.DomainModel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_row", schema = "koku")
public class DocumentRow extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    Integer positionIndex;

    @Enumerated(EnumType.STRING)
    DocumentRowAlign align;

    @OneToMany(mappedBy = "row", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    List<DocumentField> fields;

    @ManyToOne(cascade = CascadeType.PERSIST)
    DynamicDocument document;

    // copy constructor
    public DocumentRow(final DynamicDocument document, final DocumentRow rowToBeCopied) {
        this.id = null;
        this.document = document;
        this.align = rowToBeCopied.getAlign();
        if (rowToBeCopied.getFields() != null) {
            final List<DocumentField> fields = new ArrayList<>();
            for (final DocumentField currentField : rowToBeCopied.getFields()) {
                fields.add(new DocumentField(this, currentField));
            }
            this.fields = fields;
        }
    }
}
