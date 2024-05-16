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
@Table(name = "field_definition_activity_pricelist_group_row_composing", schema = "koku")
public class ActivityPriceListGroupRowComposing extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    Integer positionIndex;

    @ManyToOne(cascade = CascadeType.ALL)
    DocumentRow row;

    @ManyToOne(cascade = CascadeType.ALL)
    ActivityPriceListFieldDefinitionType fieldDefinition;

    // copy constructor
    public ActivityPriceListGroupRowComposing(final ActivityPriceListFieldDefinitionType fieldDefinition, final ActivityPriceListGroupRowComposing rowToBeCopied) {
        this.id = null;
        this.fieldDefinition = fieldDefinition;
        this.positionIndex = rowToBeCopied.getPositionIndex();
        this.row = new DocumentRow(rowToBeCopied.getRow());
    }
}
