import {
  ElementRef,
  LOCALE_ID,
  booleanAttribute,
  Component,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { InputFieldComponent } from './input-field.component';
import { IconComponent } from '../../icon/icon.component';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import isoWeek from 'dayjs/plugin/isoWeek';
import {
  formatIsoWeekDisplay,
  formatIsoWeekValue,
  normalizeShortYear,
  parseIsoWeekValue,
  supportsNativeTemporalInput,
} from './temporal-input.utils';

dayjs.extend(customParseFormat);
dayjs.extend(isoWeek);

@Component({
  selector: 'week-input-field',
  templateUrl: './week-input-field.component.html',
  imports: [InputFieldComponent, IconComponent],
  standalone: true,
})
export class WeekInputFieldComponent {
  value = input.required<string | null | undefined>();
  defaultValue = input<string | null | undefined>('');
  name = input<string>();
  label = input<string>();
  min = input<number>();
  max = input<number>();
  placeholder = input<string>();
  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });
  cls = input<string>('');
  changed = output<string | null>();
  typed = output<string>();
  blurred = output<void>();
  focused = output<void>();
  readonly locale = inject(LOCALE_ID);
  readonly nativePickerSupported = supportsNativeTemporalInput('week');
  private readonly displayOverride = signal<string | null>(null);
  private readonly nativePicker = viewChild<ElementRef<HTMLInputElement>>('nativePicker');

  displayValue(): string {
    const rawValue = this.rawValue();
    const displayOverride = this.displayOverride();
    if (displayOverride !== null && this.toOutputValue(displayOverride) === rawValue) {
      return displayOverride;
    }
    return this.formatValue(rawValue);
  }

  defaultDisplayValue(): string {
    return this.formatValue(this.defaultValue() ?? '');
  }

  placeholderValue(): string {
    return this.placeholder() || 'KW WW / JJJJ';
  }

  nativePickerValue(): string {
    const parsedValue = this.parseValue(this.rawValue());
    return parsedValue.isValid() ? this.formatWeek(parsedValue) : '';
  }

  typeWeekInput(value: string): void {
    const displayValue = this.formatValue(value);
    this.displayOverride.set(displayValue);
    this.typed.emit(this.toOutputValue(displayValue));
  }

  changeWeekInput(value: string): void {
    const displayValue = this.formatValue(value);
    this.displayOverride.set(displayValue);
    this.changed.emit(this.toChangeValue(displayValue));
  }

  openNativePicker(event: Event): void {
    event.stopPropagation();
    if (this.disabled() || this.readonly()) {
      return;
    }
    const nativePicker = this.nativePicker()?.nativeElement;
    if (!nativePicker) {
      return;
    }
    nativePicker.focus();
    if (nativePicker.showPicker) {
      nativePicker.showPicker();
    } else {
      nativePicker.click();
    }
  }

  selectFromNativePicker(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    if (!value) {
      return;
    }
    const parsedValue = this.parseValue(value);
    if (!parsedValue.isValid()) {
      return;
    }
    const outputValue = this.formatWeek(parsedValue);
    this.displayOverride.set(formatIsoWeekDisplay(parsedValue));
    this.typed.emit(outputValue);
    this.changed.emit(outputValue);
  }

  validate(): boolean {
    const valueSnapshot = this.value() ?? '';
    if ((!valueSnapshot || !valueSnapshot.length) && this.required()) {
      return false;
    }
    return !valueSnapshot || this.parseValue(valueSnapshot).isValid();
  }

  private rawValue(): string {
    return (!this.loading() ? this.value() : this.defaultValue()) ?? '';
  }

  private formatValue(value: string): string {
    if (!value) {
      return '';
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? formatIsoWeekDisplay(parsedValue) : this.maskValue(value);
  }

  private toOutputValue(value: string): string {
    if (!value) {
      return value;
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? this.formatWeek(parsedValue) : value;
  }

  private toChangeValue(value: string): string | null {
    if (!value) {
      return value;
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? this.formatWeek(parsedValue) : null;
  }

  private parseValue(value: string): dayjs.Dayjs {
    return parseIsoWeekValue(this.normalizeValue(value));
  }

  private normalizeValue(value: string): string {
    const normalizedValue = value.toUpperCase();
    if (/^\d{4}-W\d{1,2}$/.test(normalizedValue)) {
      const [year, week] = normalizedValue.split('-W');
      return `${year}-W${week.padStart(2, '0')}`;
    }
    const digits = value.replace(/\D/g, '');
    if (digits.length === 3) {
      return `${this.normalizeYear(digits.slice(1, 3))}-W0${digits.slice(0, 1)}`;
    }
    if (digits.length === 4) {
      const shortWeek = `${this.normalizeYear(digits.slice(2, 4))}-W${digits.slice(0, 2)}`;
      if (parseIsoWeekValue(shortWeek).isValid()) {
        return shortWeek;
      }
      return `${this.normalizeYear(digits.slice(1, 3))}-W0${digits.slice(0, 1)}`;
    }
    if (digits.length === 6) {
      return `${digits.slice(2, 6)}-W${digits.slice(0, 2)}`;
    }
    return normalizedValue;
  }

  private maskValue(value: string): string {
    return value
      .toUpperCase()
      .replace(/[^\dW-]/g, '')
      .slice(0, 8);
  }

  private formatWeek(value: dayjs.Dayjs): string {
    return formatIsoWeekValue(value);
  }

  private normalizeYear(value: string): string {
    if (value.length === 4) {
      return value;
    }
    return normalizeShortYear(value);
  }
}
