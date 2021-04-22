import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";


@Component({
  selector: 'svg-field',
  templateUrl: './svg-field.component.html',
  styleUrls: ['./svg-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SvgFieldComponent),
      multi: true
    }
  ]
})
export class SvgFieldComponent implements ControlValueAccessor {

  internalValue: SafeHtml = '';
  @Input() maxWidthInPx: number | undefined;
  @Input() widthPercentage: number | undefined;
  private onChange: any;

  constructor(private domSanitizer: DomSanitizer) {
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  writeValue(value: string): void {
    if (value !== undefined) {
      this.internalValue = this.domSanitizer.bypassSecurityTrustHtml(atob(value));
    }
  }


}
