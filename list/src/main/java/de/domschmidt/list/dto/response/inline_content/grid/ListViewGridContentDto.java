package de.domschmidt.list.dto.response.inline_content.grid;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("grid")
@Getter
public class ListViewGridContentDto extends AbstractListViewContentDto {

    Integer sm;
    Integer md;
    Integer lg;
    Integer xl;
    Integer xl2;
    Integer xl3;
    Integer xl4;
    Integer xl5;
    Integer xl6;
    Integer xl7;
    Integer cols;
    @Builder.Default
    List<AbstractListViewContentDto> content = new ArrayList<>();

}
