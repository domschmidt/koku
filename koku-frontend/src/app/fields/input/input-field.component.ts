import { booleanAttribute, Component, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';

let uniqueId = 0;

@Component({
  selector: 'input-field',
  templateUrl: './input-field.component.html',
  styleUrl: './input-field.component.css',
  imports: [DatePipe],
  standalone: true,
})
export class InputFieldComponent {
  value = input.required<string>();
  defaultValue = input<string>('');
  name = input<string>();
  label = input<string>();
  type = input<
    | 'TEXT'
    | 'PASSWORD'
    | 'EMAIL'
    | 'NUMBER'
    | 'DATE'
    | 'DATETIME'
    | 'WEEK'
    | 'MONTH'
    | 'TEL'
    | 'URL'
    | 'SEARCH'
    | 'TIME'
  >('TEXT');
  min = input<number>();
  max = input<number>();
  placeholder = input<string>();
  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });
  cls = input<string>('');
  onChange = output<string>();
  onInput = output<string>();
  onBlur = output<void>();
  onFocus = output<void>();
  id = `input-field-${uniqueId++}`;

  onInputRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).value;
      this.onInput.emit(value);
    }
  }

  onChangeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).value;
      this.onChange.emit(value);
    }
  }

  validate(): boolean {
    const valueSnapshot = this.value();
    if ((!valueSnapshot || !valueSnapshot.length) && this.required()) {
      return false;
    }
    return true;
  }
}
