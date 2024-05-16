import {Component, Inject} from '@angular/core';
import {DOCUMENT_FIELD_DATA, DOCUMENT_FIELD_META} from '../../document-designer-module/document-field-host.directive';
import ActivityPriceListFormularItemDto = KokuDto.ActivityPriceListFormularItemDto;


@Component({
  selector: 'document-activity-price-list-field',
  templateUrl: './document-activity-price-list-field.component.html',
  styleUrls: ['./document-activity-price-list-field.component.scss']
})
export class DocumentActivityPriceListFieldComponent {

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: ActivityPriceListFormularItemDto,
    @Inject(DOCUMENT_FIELD_META) public meta: any
  ) {
  }
}
