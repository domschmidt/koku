package de.domschmidt.koku.persistence.model.dynamic_documents;

import lombok.Getter;

@Getter
public enum ActivityPriceListGroupBy {

    CATEGORY("[[${activity?.category?.id}]]")
    ;

    private final String groupBySelector;

    ActivityPriceListGroupBy(
            final String groupBySelector
    ) {
        this.groupBySelector = groupBySelector;
    }
}
