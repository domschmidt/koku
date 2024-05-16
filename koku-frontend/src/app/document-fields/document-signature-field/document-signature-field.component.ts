import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {DOCUMENT_FIELD_DATA} from '../../document-designer-module/document-field-host.directive';
import SignaturePad from 'signature_pad';
import {throttle} from 'lodash';
import {ResizeSensor} from 'css-element-queries';
import SignatureFormularItemDto = KokuDto.SignatureFormularItemDto;


@Component({
  selector: 'document-signature-field',
  templateUrl: './document-signature-field.component.html',
  styleUrls: ['./document-signature-field.component.scss']
})
export class DocumentSignatureFieldComponent implements AfterViewInit {

  @ViewChild('canvasElement', {read: ElementRef, static: true}) canvas: ElementRef<HTMLCanvasElement> | undefined;
  @ViewChild('canvasWrapperElement', {
    read: ElementRef,
    static: true
  }) canvasWrapper: ElementRef<HTMLDivElement> | undefined;
  signaturePad: SignaturePad | undefined;
  width: number | undefined;
  height: number | undefined;
  private ASPECT_RATIO = 1 / 3;

  private throttledRedraw = throttle(() => {
    this.redrawCanvas();
  }, 300);

  constructor(
    @Inject(DOCUMENT_FIELD_DATA) public data: SignatureFormularItemDto
  ) {
  }

  ngAfterViewInit(): void {
    if (this.canvasWrapper) {
      new ResizeSensor(this.canvasWrapper.nativeElement, () => {
        this.throttledRedraw();
      });
    }
    this.redrawCanvas();
  }

  private redrawCanvas(): void {
    if (this.canvas && this.canvasWrapper) {
      if (this.signaturePad) {
        this.signaturePad.clear();
        this.signaturePad.off();
      }
      const newSignaturePad = new SignaturePad(this.canvas.nativeElement, {});
      newSignaturePad.addEventListener('endStroke', () => {
        this.data.dataUri = this.signaturePad?.toDataURL('image/png');
      });

      // apply size
      const width = this.canvasWrapper.nativeElement.clientWidth || 300;
      this.width = width;
      this.height = width * this.ASPECT_RATIO;

      if (this.data.dataUri) {
        newSignaturePad.fromDataURL(this.data.dataUri);
      }

      // apply size
      this.signaturePad = newSignaturePad;
    } else {
      alert('Signature pad not initialized properly!');
    }
  }


}
