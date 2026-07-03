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

const DATE_VALUE_FORMAT = 'YYYY-MM-DD';
type DateFormatToken = 'D' | 'M' | 'Y';
interface DateFormatDefinition {
  dayjsFormat: string;
  placeholder: string;
  separator: string;
  tokens: DateFormatToken[];
}
interface DateInputParts {
  day?: string;
  month?: string;
  year?: string;
}

@Component({
  selector: 'date-input-field',
  templateUrl: './date-input-field.component.html',
  imports: [InputFieldComponent, IconComponent],
  standalone: true,
})
export class DateInputFieldComponent {
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
  readonly nativePickerSupported = supportsNativeTemporalInput('date');
  private readonly formatDefinition = this.createFormatDefinition(this.locale);
  private readonly displayOverride = signal<string | null>(null);
  private readonly nativePicker = viewChild<ElementRef<HTMLInputElement>>('nativePicker');

  displayValue(): string {
    const rawValue = this.rawValue();
    const displayOverride = this.displayOverride();
    if (displayOverride !== null && this.toDateInputOutputValue(displayOverride) === rawValue) {
      return displayOverride;
    }
    return this.formatDateInputValue(rawValue);
  }

  defaultDisplayValue(): string {
    return this.formatDateInputValue(this.defaultValue() ?? '');
  }

  placeholderValue(): string {
    return this.placeholder() || this.formatDefinition.placeholder;
  }

  nativePickerValue(): string {
    const parsedDate = this.parseDateInputValue(this.rawValue());
    return parsedDate.isValid() ? parsedDate.format(DATE_VALUE_FORMAT) : '';
  }

  typeDateInput(value: string): void {
    const displayValue = this.formatDateInputValue(value);
    this.displayOverride.set(displayValue);
    this.typed.emit(this.toDateInputOutputValue(displayValue));
  }

  changeDateInput(value: string): void {
    const displayValue = this.formatDateInputValue(value);
    this.displayOverride.set(displayValue);
    this.changed.emit(this.toDateInputChangeValue(displayValue));
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
    const parsedDate = dayjs(value, DATE_VALUE_FORMAT, true);
    if (!parsedDate.isValid()) {
      return;
    }
    const outputValue = parsedDate.format(DATE_VALUE_FORMAT);
    this.displayOverride.set(parsedDate.format(this.formatDefinition.dayjsFormat));
    this.typed.emit(outputValue);
    this.changed.emit(outputValue);
  }

  validate(): boolean {
    const valueSnapshot = this.value() ?? '';
    if (valueSnapshot.length === 0) {
      return this.required() === false;
    }
    return this.isDateInputValueValid(valueSnapshot);
  }

  private rawValue(): string {
    if (this.loading()) {
      return this.defaultValue() ?? '';
    }
    return this.value() ?? '';
  }

  private maskDateInput(value: string): string {
    const digits = value.replace(/\D/g, '').slice(0, 8);
    if (/^\d+$/.test(value) && digits.length <= 5) {
      return digits;
    }
    let offset = 0;
    const parts = this.formatDefinition.tokens
      .map((token) => {
        const length = token === 'Y' ? 4 : 2;
        const part = digits.slice(offset, offset + length);
        offset += length;
        return part;
      })
      .filter(Boolean);
    return parts.join(this.formatDefinition.separator);
  }

  private formatDateInputValue(value: string): string {
    if (!value) {
      return '';
    }
    const parsedDate = this.parseDateInputValue(value);
    if (parsedDate.isValid()) {
      return parsedDate.format(this.formatDefinition.dayjsFormat);
    }
    return this.maskDateInput(value);
  }

  private toDateInputOutputValue(value: string): string {
    if (!value) {
      return value;
    }
    const parsedDate = this.parseDateInputValue(value);
    if (parsedDate.isValid()) {
      return parsedDate.format(DATE_VALUE_FORMAT);
    }
    return value;
  }

  private toDateInputChangeValue(value: string): string | null {
    if (!value) {
      return value;
    }
    const parsedDate = this.parseDateInputValue(value);
    if (parsedDate.isValid()) {
      return parsedDate.format(DATE_VALUE_FORMAT);
    }
    return null;
  }

  private isDateInputValueValid(value: string): boolean {
    return this.parseDateInputValue(value).isValid();
  }

  private parseDateInputValue(value: string): dayjs.Dayjs {
    const parsedLocalizedDate = dayjs(value, this.formatDefinition.dayjsFormat, true);
    if (parsedLocalizedDate.isValid()) {
      return parsedLocalizedDate;
    }
    const parsedIsoDate = dayjs(value, DATE_VALUE_FORMAT, true);
    if (parsedIsoDate.isValid()) {
      return parsedIsoDate;
    }
    return dayjs(this.normalizeDateInputValue(value), this.formatDefinition.dayjsFormat, true);
  }

  private normalizeDateInputValue(value: string): string {
    const parts = /\D/.test(value) ? this.readSeparatedDateParts(value) : this.readNumericDateParts(value);
    if (!parts) {
      return value;
    }

    const day = this.padDatePart(parts.day, 2);
    const month = this.padDatePart(parts.month, 2);
    const year = this.normalizeYear(parts.year);
    if (!day || !month || !year) {
      return value;
    }

    const mappedParts: Record<DateFormatToken, string> = {
      D: day,
      M: month,
      Y: year,
    };
    return this.formatDefinition.tokens.map((token) => mappedParts[token]).join(this.formatDefinition.separator);
  }

  private readSeparatedDateParts(value: string): DateInputParts | null {
    const rawParts = value.split(/\D+/).filter(Boolean);
    if (rawParts.length !== 3 || rawParts.some((part) => !/^\d{1,4}$/.test(part))) {
      return null;
    }
    return this.mapDateParts(rawParts);
  }

  private readNumericDateParts(value: string): DateInputParts | null {
    const digits = value.replace(/\D/g, '');
    if (digits.length === 8) {
      return this.readNumericDatePartsByYearLength(digits, 4);
    }
    if (digits.length === 6) {
      return this.readNumericDatePartsByYearLength(digits, 2);
    }
    if (digits.length === 4) {
      return this.readNumericDatePartsWithShortTokens(digits, ['D', 'M']);
    }
    if (digits.length !== 5) {
      return null;
    }

    const candidates = this.formatDefinition.tokens
      .filter((token) => token !== 'Y')
      .map((shortToken) => this.readNumericDatePartsWithShortToken(digits, shortToken))
      .filter((candidate): candidate is DateInputParts => candidate !== null);
    return candidates.find((candidate) => this.partsCreateValidDate(candidate)) || null;
  }

  private readNumericDatePartsByYearLength(digits: string, yearLength: 2 | 4): DateInputParts | null {
    let offset = 0;
    const rawParts = this.formatDefinition.tokens.map((token) => {
      const length = token === 'Y' ? yearLength : 2;
      const part = digits.slice(offset, offset + length);
      offset += length;
      return part;
    });
    return offset === digits.length ? this.mapDateParts(rawParts) : null;
  }

  private readNumericDatePartsWithShortToken(digits: string, shortToken: DateFormatToken): DateInputParts | null {
    return this.readNumericDatePartsWithShortTokens(digits, [shortToken]);
  }

  private readNumericDatePartsWithShortTokens(digits: string, shortTokens: DateFormatToken[]): DateInputParts | null {
    let offset = 0;
    const rawParts = this.formatDefinition.tokens.map((token) => {
      const length = this.numericDatePartLength(token, shortTokens);
      const part = digits.slice(offset, offset + length);
      offset += length;
      return part;
    });
    return offset === digits.length ? this.mapDateParts(rawParts) : null;
  }

  private mapDateParts(rawParts: string[]): DateInputParts {
    return this.formatDefinition.tokens.reduce<DateInputParts>((result, token, index) => {
      if (token === 'D') {
        result.day = rawParts[index];
      } else if (token === 'M') {
        result.month = rawParts[index];
      } else {
        result.year = rawParts[index];
      }
      return result;
    }, {});
  }

  private numericDatePartLength(token: DateFormatToken, shortTokens: DateFormatToken[]): number {
    if (token === 'Y') {
      return 2;
    }
    return shortTokens.includes(token) ? 1 : 2;
  }

  private partsCreateValidDate(parts: DateInputParts): boolean {
    const day = this.padDatePart(parts.day, 2);
    const month = this.padDatePart(parts.month, 2);
    const year = this.normalizeYear(parts.year);
    if (!day || !month || !year) {
      return false;
    }
    const mappedParts: Record<DateFormatToken, string> = {
      D: day,
      M: month,
      Y: year,
    };
    return dayjs(
      this.formatDefinition.tokens.map((token) => mappedParts[token]).join(this.formatDefinition.separator),
      this.formatDefinition.dayjsFormat,
      true,
    ).isValid();
  }

  private padDatePart(value: string | undefined, length: number): string | null {
    if (!value || value.length > length) {
      return null;
    }
    return value.padStart(length, '0');
  }

  private normalizeYear(value: string | undefined): string | null {
    if (!value || (value.length !== 2 && value.length !== 4)) {
      return null;
    }
    if (value.length === 4) {
      return value;
    }
    return normalizeShortYear(value);
  }

  private createFormatDefinition(locale: string): DateFormatDefinition {
    const parts = new Intl.DateTimeFormat(locale).formatToParts(new Date(2006, 10, 22));
    const tokens = parts
      .map((part) => {
        if (part.type === 'day') {
          return 'D';
        }
        if (part.type === 'month') {
          return 'M';
        }
        if (part.type === 'year') {
          return 'Y';
        }
        return null;
      })
      .filter((part): part is DateFormatToken => part !== null);

    if (!tokens.includes('D') || !tokens.includes('M') || !tokens.includes('Y')) {
      return {
        dayjsFormat: 'DD.MM.YYYY',
        placeholder: 'TT.MM.JJJJ',
        separator: '.',
        tokens: ['D', 'M', 'Y'],
      };
    }

    const separator = parts.find((part) => part.type === 'literal')?.value || '.';
    return {
      dayjsFormat: tokens.map((token) => (token === 'Y' ? 'YYYY' : token.repeat(2))).join(separator),
      placeholder: tokens.map((token) => this.placeholderForToken(token)).join(separator),
      separator,
      tokens,
    };
  }

  private placeholderForToken(token: DateFormatToken): string {
    if (token === 'D') {
      return 'TT';
    }
    if (token === 'M') {
      return 'MM';
    }
    return 'JJJJ';
  }
}
