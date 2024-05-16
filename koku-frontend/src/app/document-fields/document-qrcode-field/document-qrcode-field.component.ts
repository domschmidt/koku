import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {DOCUMENT_FIELD_DATA} from '../../document-designer-module/document-field-host.directive';
import {toCanvas} from 'qrcode';
import QrCodeFormularItemDto = KokuDto.QrCodeFormularItemDto;


@Component({
  selector: 'document-qrcode-field',
  templateUrl: './document-qrcode-field.component.html',
  styleUrls: ['./document-qrcode-field.component.scss']
})
export class DocumentQrcodeFieldComponent implements AfterViewInit {

  @ViewChild('qrcode') canvasEl: ElementRef<HTMLCanvasElement> | undefined;

  alignMap = {
    LEFT: 'start',
    CENTER: 'center',
    RIGHT: 'end'
  };
  justifyContent: string | undefined;

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: QrCodeFormularItemDto
  ) {
    if (data.align !== undefined) {
      this.justifyContent = this.alignMap[data.align];
    }
  }

  ngAfterViewInit(): void {
    if (this.data.value !== undefined && this.canvasEl) {
      toCanvas(this.canvasEl.nativeElement, this.data.value, {
        errorCorrectionLevel: 'H'
      });
    }
  }

}
