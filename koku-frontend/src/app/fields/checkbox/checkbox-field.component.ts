import { booleanAttribute, Component, input, output } from '@angular/core';

@Component({
  selector: 'checkbox-field',
  templateUrl: './checkbox-field.component.html',
  styleUrl: './checkbox-field.component.css',
  imports: [],
  standalone: true,
})
export class CheckboxFieldComponent {
  value = input.required<boolean>();
  defaultValue = input<boolean>();
  name = input.required<string>();
  label = input<string>();
  placeholder = input<string>();
  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });
  changed = output<boolean>();
  typed = output<boolean>();
  blurred = output<void>();
  focused = output<void>();

  typeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).checked;
      this.typed.emit(value);
    }
  }

  changeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).checked;
      this.changed.emit(value);
    }
  }

  validate(): boolean {
    return true;
  }
}
