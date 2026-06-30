package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("barcode")
@EqualsAndHashCode(callSuper = true)
@Getter
public class ListViewBarcodeContentDto extends AbstractListViewContentDto {

    List<AbstractListViewBarcodeContentDtoCaptureEventDto> onCaptureEvents;
}
