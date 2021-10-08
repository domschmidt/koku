package de.domschmidt.koku.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes(
        value = {
                @JsonSubTypes.Type(value = DiagramDashboardColumnContent.class, name = "DiagramDashboardColumnContent"),
                @JsonSubTypes.Type(value = DeferredDashboardColumnContent.class, name = "DeferredDashboardColumnContent"),
                @JsonSubTypes.Type(value = TableDashboardColumnContent.class, name = "TableDashboardColumnContent"),
        }
)
public interface IDashboardColumnContent {
}
