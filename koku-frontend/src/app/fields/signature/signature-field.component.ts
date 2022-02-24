import {ChangeDetectorRef, Component, ElementRef, forwardRef, OnInit, ViewChild} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import SignaturePad from "signature_pad";
import {ResizeSensor} from "css-element-queries";
import {throttle} from "lodash";


@Component({
  selector: 'signature-field',
  templateUrl: './signature-field.component.html',
  styleUrls: ['./signature-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SignatureFieldComponent),
      multi: true
    }
  ]
})
export class SignatureFieldComponent implements ControlValueAccessor, OnInit {

  @ViewChild('canvasElement', {read: ElementRef, static: true}) canvas: ElementRef<HTMLCanvasElement> | undefined;
  @ViewChild('canvasWrapperElement', {
    read: ElementRef,
    static: true
  }) canvasWrapper: ElementRef<HTMLDivElement> | undefined;
  internalValue: string = '';
  signaturePad: SignaturePad | undefined;
  width: number | undefined;
  height: number | undefined;
  private onChange: any;
  private ASPECT_RATIO = 1 / 3;

  constructor(
    private readonly changeDetector: ChangeDetectorRef) {
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  writeValue(value: string): void {
    if (value !== undefined) {
      this.internalValue = value;
    }
  }

  redrawCanvas() {
    if (this.canvas && this.canvasWrapper) {
      if (this.signaturePad) {
        this.internalValue = this.signaturePad.toDataURL('image/png');
        this.signaturePad.clear();
        this.signaturePad.off();
      }
      const newSignaturePad = new SignaturePad(this.canvas.nativeElement, {});
      newSignaturePad.addEventListener('endStroke', () => {
        this.onChange(this.signaturePad?.toDataURL('image/png'));
      });

      // apply size
      const width = this.canvasWrapper.nativeElement.clientWidth || 300;
      this.width = width;
      this.height = width * this.ASPECT_RATIO;
      this.changeDetector.detectChanges();

      if (this.internalValue) {
        newSignaturePad.fromDataURL(this.internalValue);
      }

      // apply size
      this.signaturePad = newSignaturePad;
    } else {
      alert('Signature pad not initialized properly!');
    }
  }

  ngOnInit(): void {
    if (this.canvas && this.canvasWrapper) {
      const throttledRedraw = throttle(() => {
        this.redrawCanvas();
      }, 300);
      new ResizeSensor(this.canvasWrapper.nativeElement, () => {
        throttledRedraw();
      });
      throttledRedraw();
    }
  }


}
