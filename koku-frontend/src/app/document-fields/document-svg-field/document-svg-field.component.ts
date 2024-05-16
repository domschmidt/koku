import {Component, Inject} from '@angular/core';
import {DOCUMENT_FIELD_DATA} from '../../document-designer-module/document-field-host.directive';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import SVGFormularItemDto = KokuDto.SVGFormularItemDto;


@Component({
  selector: 'document-svg-field',
  templateUrl: './document-svg-field.component.html',
  styleUrls: ['./document-svg-field.component.scss']
})
export class DocumentSvgFieldComponent {

  alignMap = {
    LEFT: 'start',
    CENTER: 'center',
    RIGHT: 'end'
  };
  justifyContent: string | undefined;
  sanitizedValue: SafeHtml | undefined;

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: SVGFormularItemDto,
    private domSanitizer: DomSanitizer
  ) {
    if (data.align !== undefined) {
      this.justifyContent = this.alignMap[data.align];
    }
    if (data.svgContentBase64encoded) {
      this.sanitizedValue = this.domSanitizer.bypassSecurityTrustHtml(atob(data.svgContentBase64encoded));
    }
  }

}
