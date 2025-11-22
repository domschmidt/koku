package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("propagate-global-event")
@Data
public class ListViewBarcodeContentDtoAfterCapturePropagateGlobalEventDto extends AbstractListViewBarcodeContentDtoCaptureEventDto {

    String eventName;

}