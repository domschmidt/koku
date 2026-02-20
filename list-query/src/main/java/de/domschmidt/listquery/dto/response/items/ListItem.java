package de.domschmidt.listquery.dto.response.items;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ListItem {

    String id;
    Map<String, Object> values;
}
