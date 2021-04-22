import {Component, forwardRef, Input} from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormControl,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator,
  Validators
} from '@angular/forms';
import {MAT_DATE_FORMATS} from '@angular/material/core';
import {MatDatepicker} from '@angular/material/datepicker';

// Depending on whether rollup is used, moment needs to be imported differently.
// Since Moment.js doesn't have a default export, we normally need to import using the `* as`
// syntax. However, rollup creates a synthetic default module and we thus need to import it using
// the `default as` syntax.
// @ts-ignore
import * as moment from 'moment';

// See the Moment.js docs for the meaning of these formats:
// https://momentjs.com/docs/#/displaying/format/
export const MY_FORMATS = {
  parse: {
    dateInput: 'MM.YYYY',
  },
  display: {
    dateInput: 'MM.YYYY',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY',
  },
};

/** @title Datepicker emulating a Year and month picker */
@Component({
  selector: 'chart-year-month-field',
  templateUrl: './chart-year-month-field.component.html',
  providers: [
    {provide: MAT_DATE_FORMATS, useValue: MY_FORMATS},
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => ChartYearMonthFieldComponent),
      multi: true
    },
    {provide: NG_VALIDATORS, useExisting: forwardRef(() => ChartYearMonthFieldComponent), multi: true}
  ],
})
export class ChartYearMonthFieldComponent implements ControlValueAccessor, Validator {
  @Input() label: string | undefined;
  value: string | undefined;
  formattedValue: string | undefined;
  date = new FormControl(moment(), [Validators.required]);

  onChangeCallback: (_: string) => void = () => {
  };
  onTouchedCallback: () => void = () => {
  };
  onValidatorChangeCallback: () => void = () => {
  };

  chosenYearHandler(normalizedYear: moment.Moment) {
    let ctrlValue = this.date.value;
    if (!ctrlValue) {
      ctrlValue = moment();
    }
    ctrlValue.year(normalizedYear.year());
    this.date.setValue(ctrlValue);
  }

  chosenMonthHandler(normalizedMonth: moment.Moment, datepicker: MatDatepicker<any>) {
    let ctrlValue = this.date.value;
    if (!ctrlValue) {
      ctrlValue = moment();
    }
    ctrlValue.month(normalizedMonth.month());
    this.date.setValue(ctrlValue);
    datepicker.close();
  }

  public writeValue(value: string) {
    if (value && this.value !== value) {
      const newValueMoment = moment(value);
      this.value = value;
      this.formattedValue = newValueMoment.format('MM.YYYY');
      this.date.setValue(newValueMoment);
    }
  }

  public registerOnChange(callback: (_: string) => void) {
    this.onChangeCallback = callback;
  }

  public registerOnTouched(callback: () => void) {
    this.onTouchedCallback = callback;
  }

  registerOnValidatorChange(fn: () => void): void {
    this.onValidatorChangeCallback = fn;
  }

  validate(control: AbstractControl): ValidationErrors | null {
    const errors: ValidationErrors = {};
    if (this.date.invalid) {
      errors['invalid'] = this.value;
    }
    return errors;
  }

  userTypedValue() {
    if (this.date.value) {
      const newDate = this.date.value.format('YYYY-MM');
      this.formattedValue = this.date.value.format('MM.YYYY');
      this.value = newDate;
      this.onChangeCallback(newDate);
    }
  }

}
