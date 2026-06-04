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
import { normalizeShortYear, supportsNativeTemporalInput } from './temporal-input.utils';

dayjs.extend(customParseFormat);

const MONTH_VALUE_FORMAT = 'YYYY-MM';

@Component({
  selector: 'month-input-field',
  templateUrl: './month-input-field.component.html',
  imports: [InputFieldComponent, IconComponent],
  standalone: true,
})
export class MonthInputFieldComponent {
  value = input.required<string>();
  defaultValue = input<string>('');
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
  readonly nativePickerSupported = supportsNativeTemporalInput('month');
  private readonly displayOverride = signal<string | null>(null);
  private readonly nativePicker = viewChild<ElementRef<HTMLInputElement>>('nativePicker');
  private readonly monthNameLookup = this.createMonthNameLookup();

  displayValue(): string {
    const rawValue = this.rawValue();
    const displayOverride = this.displayOverride();
    if (displayOverride !== null && this.toOutputValue(displayOverride) === rawValue) {
      return displayOverride;
    }
    return this.formatValue(rawValue);
  }

  defaultDisplayValue(): string {
    return this.formatValue(this.defaultValue());
  }

  placeholderValue(): string {
    return this.placeholder() || this.monthPlaceholder();
  }

  nativePickerValue(): string {
    const parsedValue = this.parseValue(this.rawValue());
    return parsedValue.isValid() ? parsedValue.format(MONTH_VALUE_FORMAT) : '';
  }

  typeMonthInput(value: string): void {
    const displayValue = this.formatValue(value);
    this.displayOverride.set(displayValue);
    this.typed.emit(this.toOutputValue(displayValue));
  }

  changeMonthInput(value: string): void {
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
    const parsedValue = dayjs(value, MONTH_VALUE_FORMAT, true);
    if (!parsedValue.isValid()) {
      return;
    }
    const outputValue = parsedValue.format(MONTH_VALUE_FORMAT);
    this.displayOverride.set(this.formatValue(outputValue));
    this.typed.emit(outputValue);
    this.changed.emit(outputValue);
  }

  validate(): boolean {
    const valueSnapshot = this.value();
    if ((!valueSnapshot || !valueSnapshot.length) && this.required()) {
      return false;
    }
    return !valueSnapshot || this.parseValue(valueSnapshot).isValid();
  }

  private rawValue(): string {
    return !this.loading() ? this.value() : this.defaultValue();
  }

  private formatValue(value: string): string {
    if (!value) {
      return '';
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? this.formatMonth(parsedValue) : this.maskValue(value);
  }

  private toOutputValue(value: string): string {
    if (!value) {
      return value;
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? parsedValue.format(MONTH_VALUE_FORMAT) : value;
  }

  private toChangeValue(value: string): string | null {
    if (!value) {
      return value;
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? parsedValue.format(MONTH_VALUE_FORMAT) : null;
  }

  private parseValue(value: string): dayjs.Dayjs {
    return dayjs(this.normalizeValue(value), MONTH_VALUE_FORMAT, true);
  }

  private normalizeValue(value: string): string {
    if (/^\d{4}-\d{1,2}$/.test(value)) {
      const [year, month] = value.split('-');
      return `${year}-${month.padStart(2, '0')}`;
    }
    const namedMonth = this.readNamedMonthValue(value);
    if (namedMonth) {
      return namedMonth;
    }
    if (/^\d{1,2}\D\d{2,4}$/.test(value)) {
      const [month, year] = value.split(/\D/);
      return `${this.normalizeYear(year)}-${month.padStart(2, '0')}`;
    }
    const digits = value.replace(/\D/g, '');
    if (digits.length === 3) {
      return `${this.normalizeYear(digits.slice(1, 3))}-0${digits.slice(0, 1)}`;
    }
    if (digits.length === 4) {
      const shortMonth = `${this.normalizeYear(digits.slice(2, 4))}-${digits.slice(0, 2)}`;
      if (dayjs(shortMonth, MONTH_VALUE_FORMAT, true).isValid()) {
        return shortMonth;
      }
      return `${this.normalizeYear(digits.slice(1, 3))}-0${digits.slice(0, 1)}`;
    }
    if (digits.length === 6) {
      return `${digits.slice(2, 6)}-${digits.slice(0, 2)}`;
    }
    return value;
  }

  private maskValue(value: string): string {
    if (/[A-Za-zÄÖÜäöüß]/.test(value)) {
      return value.slice(0, 20);
    }
    if (/\D/.test(value)) {
      return value.slice(0, 10);
    }
    const digits = value.replace(/\D/g, '').slice(0, 6);
    if (digits.length <= 2) {
      return digits;
    }
    return `${digits.slice(0, 2)}.${digits.slice(2, 6)}`;
  }

  private formatMonth(value: dayjs.Dayjs): string {
    return new Intl.DateTimeFormat(this.locale, { month: 'long', year: 'numeric' }).format(value.toDate());
  }

  private monthPlaceholder(): string {
    return this.formatMonth(dayjs('2006-06', MONTH_VALUE_FORMAT, true)).replace('2006', 'JJJJ');
  }

  private normalizeYear(value: string): string {
    if (value.length === 4) {
      return value;
    }
    return normalizeShortYear(value);
  }

  private readNamedMonthValue(value: string): string | null {
    const normalizedValue = this.normalizeText(value);
    const match = /^([^\d]+?)\s+(\d{2}|\d{4})$/.exec(normalizedValue);
    if (!match) {
      return null;
    }
    const month = this.monthNameLookup[match[1].trim()];
    if (!month) {
      return null;
    }
    return `${this.normalizeYear(match[2])}-${month}`;
  }

  private createMonthNameLookup(): Record<string, string> {
    const result: Record<string, string> = {};
    for (let month = 0; month < 12; month++) {
      const date = new Date(2006, month, 1);
      const monthNumber = String(month + 1).padStart(2, '0');
      for (const monthFormat of ['long', 'short'] as const) {
        const monthName = new Intl.DateTimeFormat(this.locale, { month: monthFormat }).format(date);
        result[this.normalizeText(monthName)] = monthNumber;
        result[this.normalizeText(monthName.replace('.', ''))] = monthNumber;
      }
    }
    return result;
  }

  private normalizeText(value: string): string {
    return value
      .trim()
      .toLocaleLowerCase(this.locale)
      .normalize('NFD')
      .replace(/\p{Diacritic}/gu, '')
      .replace(/\s+/g, ' ');
  }
}
