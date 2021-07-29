import {Component, forwardRef, Input} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";


@Component({
  selector: 'checkbox-field',
  templateUrl: './checkbox-field.component.html',
  styleUrls: ['./checkbox-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckboxFieldComponent),
      multi: true
    }
  ]
})
export class CheckboxFieldComponent implements ControlValueAccessor {

  internalValue: string = '';
  @Input() label: string | undefined;
  @Input() fontSize: KokuDto.FontSizeDto | undefined;
  @Input() readOnly: boolean | undefined;
  onChange: any;

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


}
