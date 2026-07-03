import { booleanAttribute, Component, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { InputFieldComponent } from './input-field.component';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import { IconComponent } from '../../icon/icon.component';
import { supportsNativeTemporalInput } from './temporal-input.utils';

dayjs.extend(customParseFormat);

const TIME_VALUE_FORMAT = 'HH:mm';

@Component({
  selector: 'time-input-field',
  templateUrl: './time-input-field.component.html',
  imports: [InputFieldComponent, IconComponent],
  standalone: true,
})
export class TimeInputFieldComponent {
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
  readonly nativePickerSupported = supportsNativeTemporalInput('time');
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
    return this.placeholder() || 'HH:MM';
  }

  typeTimeInput(value: string): void {
    const displayValue = this.formatValue(value);
    this.displayOverride.set(displayValue);
    this.typed.emit(this.toOutputValue(displayValue));
  }

  changeTimeInput(value: string): void {
    const displayValue = this.formatValue(value);
    this.displayOverride.set(displayValue);
    this.changed.emit(this.toChangeValue(displayValue));
  }

  nativePickerValue(): string {
    const parsedValue = this.parseValue(this.rawValue());
    return parsedValue.isValid() ? parsedValue.format(TIME_VALUE_FORMAT) : '';
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
    const outputValue = parsedValue.format(TIME_VALUE_FORMAT);
    this.displayOverride.set(outputValue);
    this.typed.emit(outputValue);
    this.changed.emit(outputValue);
  }

  validate(): boolean {
    const valueSnapshot = this.value() ?? '';
    if (valueSnapshot.length === 0) {
      return this.required() === false;
    }
    return this.parseValue(valueSnapshot).isValid();
  }

  private rawValue(): string {
    if (this.loading()) {
      return this.defaultValue() ?? '';
    }
    return this.value() ?? '';
  }

  private formatValue(value: string): string {
    if (!value) {
      return '';
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? parsedValue.format(TIME_VALUE_FORMAT) : this.maskValue(value);
  }

  private toOutputValue(value: string): string {
    if (!value) {
      return value;
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? parsedValue.format(TIME_VALUE_FORMAT) : value;
  }

  private toChangeValue(value: string): string | null {
    if (!value) {
      return value;
    }
    const parsedValue = this.parseValue(value);
    return parsedValue.isValid() ? parsedValue.format(TIME_VALUE_FORMAT) : null;
  }

  private parseValue(value: string): dayjs.Dayjs {
    return dayjs(this.normalizeValue(value), TIME_VALUE_FORMAT, true);
  }

  private normalizeValue(value: string): string {
    if (/^\d{1,2}:\d{1,2}:\d{1,2}(?:\.\d+)?$/.test(value)) {
      const [hours, minutes] = value.split(':');
      return `${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`;
    }
    if (/^\d{1,2}:\d{1,2}$/.test(value)) {
      const [hours, minutes] = value.split(':');
      return `${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`;
    }
    const digits = value.replace(/\D/g, '');
    if (digits.length === 2) {
      return `${digits}:00`;
    }
    if (digits.length === 3) {
      const twoDigitHourValue = `${digits.slice(0, 2)}:${digits.slice(2, 3)}0`;
      if (dayjs(twoDigitHourValue, TIME_VALUE_FORMAT, true).isValid()) {
        return twoDigitHourValue;
      }
      return `${digits.slice(0, 1).padStart(2, '0')}:${digits.slice(1, 3)}`;
    }
    if (digits.length === 4) {
      return `${digits.slice(0, 2)}:${digits.slice(2, 4)}`;
    }
    return value;
  }

  private maskValue(value: string): string {
    const digits = value.replace(/\D/g, '').slice(0, 4);
    if (digits.length <= 2) {
      return digits;
    }
    return `${digits.slice(0, 2)}:${digits.slice(2, 4)}`;
  }
}
