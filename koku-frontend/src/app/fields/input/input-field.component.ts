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
  type = input<'TEXT' | 'PASSWORD' | 'EMAIL' | 'NUMBER' | 'DATETIME' | 'TEL' | 'URL' | 'SEARCH'>('TEXT');
  min = input<number>();
  max = input<number>();
  inputMode = input<'decimal' | 'email' | 'none' | 'numeric' | 'search' | 'tel' | 'text' | 'url'>();
  placeholder = input<string>();
  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });
  cls = input<string>('');
  changed = output<string>();
  typed = output<string>();
  blurred = output<void>();
  focused = output<void>();
  id = `input-field-${uniqueId++}`;

  typeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).value;
      this.typed.emit(value);
    }
  }

  changeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).value;
      this.changed.emit(value);
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
