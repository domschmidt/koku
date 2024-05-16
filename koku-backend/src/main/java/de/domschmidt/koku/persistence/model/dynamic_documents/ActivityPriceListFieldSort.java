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
@Table(name = "field_definition_activity_pricelist_sort", schema = "koku")
public class ActivityPriceListFieldSort extends DomainModel implements Serializable {

    @Id
    @GeneratedValue(generator = "koku_seq")
    @SequenceGenerator(schema = "koku", name = "koku_seq")
    Long id;
    Long sortById;
    Integer positionIndex;

    @ManyToOne(cascade = CascadeType.ALL)
    ActivityPriceListFieldDefinitionType fieldDefinition;

    // copy constructor
    public ActivityPriceListFieldSort(final ActivityPriceListFieldDefinitionType fieldDefinition, final ActivityPriceListFieldSort toBeCopied) {
        this.id = null;
        this.sortById = toBeCopied.getSortById();
        this.fieldDefinition = fieldDefinition;
        this.positionIndex = toBeCopied.getPositionIndex();
    }
}
