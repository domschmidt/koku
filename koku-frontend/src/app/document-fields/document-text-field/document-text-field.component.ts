import {Component, Inject} from '@angular/core';
import {
  DOCUMENT_FIELD_DATA,
  DOCUMENT_FIELD_OPTIONS,
  DocumentFieldOptions
} from "../../document-designer-module/document-field-host.directive";
import TextFormularItemDto = KokuDto.TextFormularItemDto;


@Component({
  selector: 'document-text-field',
  templateUrl: './document-text-field.component.html',
  styleUrls: ['./document-text-field.component.scss']
})
export class DocumentTextFieldComponent {

  alignMap = {
    'LEFT': 'left',
    'CENTER': 'center',
    'RIGHT': 'right'
  };
  textAlign: string | undefined;

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: TextFormularItemDto,
    @Inject(DOCUMENT_FIELD_OPTIONS) public options: DocumentFieldOptions
  ) {
    if (data.align !== undefined) {
      this.textAlign = this.alignMap[data.align];
    }
  }

}
