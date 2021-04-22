import {Component, forwardRef} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import * as moment from "moment";


@Component({
  selector: 'date-field',
  templateUrl: './date-field.component.html',
  styleUrls: ['./date-field.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DateFieldComponent),
      multi: true
    }
  ]
})
export class DateFieldComponent implements ControlValueAccessor {

  internalValue: moment.MomentInput = moment();
  private onChange: any;
  private readonly API_FORMAT = 'yyyy-mm-dd';

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  writeValue(value: string): void {
    if (value !== undefined) {
      const newValue = moment(value, this.API_FORMAT, true);
      if (newValue.isValid()) {
        this.internalValue = newValue;
      }
    }
  }


}
