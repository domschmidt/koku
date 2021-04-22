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
@Table(name = "document", schema = "koku")
public class DynamicDocument extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    String description;
    boolean deleted;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    List<DocumentRow> rows;

    // copy constructor
    public DynamicDocument(final DynamicDocument documentToBeCopied) {
        this.id = null;
        this.description = "Kopie von " + documentToBeCopied.getDescription();
        this.deleted = documentToBeCopied.isDeleted();
        if (documentToBeCopied.getRows() != null) {
            final List<DocumentRow> rows = new ArrayList<>();
            for (final DocumentRow currentRow : documentToBeCopied.getRows()) {
                rows.add(new DocumentRow(this, currentRow));
            }
            this.rows = rows;
        }
    }
}
