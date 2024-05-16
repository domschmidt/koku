package de.domschmidt.koku.persistence.model.dynamic_documents;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

@DiscriminatorValue("ACTIVITY_PRICE_LIST")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "field_definition_activity_pricelist", schema = "koku")
public class ActivityPriceListFieldDefinitionType extends FieldDefinitionType implements Serializable {

    @OneToMany(mappedBy = "fieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    List<ActivityPriceListItemRowComposing> itemRows;

    @OneToMany(mappedBy = "fieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    List<ActivityPriceListGroupRowComposing> groupRows;

    @OneToMany(mappedBy = "fieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("positionIndex ASC")
    List<ActivityPriceListFieldSort> sortByIds;

    @Enumerated(EnumType.STRING)
    ActivityPriceListGroupBy groupBy;

    // copy constructor
    public ActivityPriceListFieldDefinitionType(final ActivityPriceListFieldDefinitionType fieldDefintionTypeToBeCopied) {
        this.id = null;
        if (fieldDefintionTypeToBeCopied.getItemRows() != null) {
            final List<ActivityPriceListItemRowComposing> itemRows = new ArrayList<>();
            for (final ActivityPriceListItemRowComposing currentItemRow : fieldDefintionTypeToBeCopied.getItemRows()) {
                itemRows.add(new ActivityPriceListItemRowComposing(
                        this,
                        currentItemRow
                ));
            }
            this.itemRows = itemRows;
        }
        if (fieldDefintionTypeToBeCopied.getGroupRows() != null) {
            final List<ActivityPriceListGroupRowComposing> groupRows = new ArrayList<>();
            for (final ActivityPriceListGroupRowComposing currentItemRow : fieldDefintionTypeToBeCopied.getGroupRows()) {
                groupRows.add(new ActivityPriceListGroupRowComposing(
                        this,
                        currentItemRow
                ));
            }
            this.groupRows = groupRows;
        }
        if (fieldDefintionTypeToBeCopied.getSortByIds() != null) {
            final List<ActivityPriceListFieldSort> resultSortInfo = new ArrayList<>();
            for (final ActivityPriceListFieldSort currentSortInfo : fieldDefintionTypeToBeCopied.getSortByIds()) {
                resultSortInfo.add(new ActivityPriceListFieldSort(
                        this,
                        currentSortInfo
                ));
            }
            this.sortByIds = resultSortInfo;
        }
        this.groupBy = fieldDefintionTypeToBeCopied.getGroupBy();
    }
}
