package de.domschmidt.koku.dto.list.filters;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.filters.AbstractListViewFilterDto;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import java.util.List;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("toggle")
@Getter
public class ListViewToggleFilterDto extends AbstractListViewFilterDto {

    String label;

    @Singular
    List<QueryPredicate> enabledPredicates;

    @Singular
    List<QueryPredicate> disabledPredicates;

    @Singular
    List<QueryPredicate> neutralPredicates;

    ListViewToggleFilterDefaultStateEnum defaultState;
}
