import {Component, Inject} from '@angular/core';
import {DOCUMENT_FIELD_DATA} from '../../document-designer-module/document-field-host.directive';
import CheckboxFormularItemDto = KokuDto.CheckboxFormularItemDto;


@Component({
  selector: 'document-checkbox-field',
  templateUrl: './document-checkbox-field.component.html',
  styleUrls: ['./document-checkbox-field.component.scss']
})
export class DocumentCheckboxFieldComponent {

  alignMap = {
    LEFT: 'left',
    CENTER: 'center',
    RIGHT: 'right'
  };
  textAlign: string | undefined;

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: CheckboxFormularItemDto
  ) {
    if (data.align !== undefined) {
      this.textAlign = this.alignMap[data.align];
    }
  }

}
