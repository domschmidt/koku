import {Component, Inject} from '@angular/core';
import {DOCUMENT_FIELD_DATA} from '../../document-designer-module/document-field-host.directive';
import DateFormularItemDto = KokuDto.DateFormularItemDto;


@Component({
  selector: 'document-date-field',
  templateUrl: './document-date-field.component.html',
  styleUrls: ['./document-date-field.component.scss']
})
export class DocumentDateFieldComponent {

  alignMap = {
    LEFT: 'left',
    CENTER: 'center',
    RIGHT: 'right'
  };
  textAlign: string | undefined;

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: DateFormularItemDto
  ) {
    if (data.align !== undefined) {
      this.textAlign = this.alignMap[data.align];
    }
  }

}
