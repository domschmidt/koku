package de.domschmidt.list.dto.response.inline_content.list;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("endpoint")
@Data
public class EndpointListViewListContentContextDto extends AbstractListViewListContentContextDto {

    EndpointListViewContextMethodEnum endpointMethod;
    String endpointUrl;
}
