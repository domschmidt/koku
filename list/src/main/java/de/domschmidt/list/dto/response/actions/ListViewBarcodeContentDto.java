package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("barcode")
@Getter
public class ListViewBarcodeContentDto extends AbstractListViewContentDto {

    List<AbstractListViewBarcodeContentDtoCaptureEventDto> onCaptureEvents;

}
