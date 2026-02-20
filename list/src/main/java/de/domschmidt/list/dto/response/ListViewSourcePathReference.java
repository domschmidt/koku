package de.domschmidt.list.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeName("source-path-reference")
public class ListViewSourcePathReference extends ListViewReference {

    String valuePath;
}
