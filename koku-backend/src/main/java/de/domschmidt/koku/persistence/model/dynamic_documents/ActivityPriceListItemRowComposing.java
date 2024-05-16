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
@Table(name = "field_definition_activity_pricelist_item_row_composing", schema = "koku")
public class ActivityPriceListItemRowComposing extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    Integer positionIndex;

    @ManyToOne(cascade = CascadeType.ALL)
    DocumentRow row;

    @ManyToOne
    ActivityPriceListFieldDefinitionType fieldDefinition;

    // copy constructor
    public ActivityPriceListItemRowComposing(final ActivityPriceListFieldDefinitionType fieldDefinition, final ActivityPriceListItemRowComposing rowToBeCopied) {
        this.id = null;
        this.fieldDefinition = fieldDefinition;
        this.positionIndex = rowToBeCopied.getPositionIndex();
        this.row = new DocumentRow(rowToBeCopied.getRow());
    }
}
