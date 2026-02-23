import { booleanAttribute, Component, input, output } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';

@Component({
  selector: 'year-month-field',
  templateUrl: './year-month-field.component.html',
  styleUrl: './year-month-field.component.css',
  imports: [],
  standalone: true,
})
export class YearMonthFieldComponent {
  value = input.required<string>();
  name = input<string>();
  placeholder = input<string>();
  label = input<string>();
  required = input(false, { transform: booleanAttribute });
  loading = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  onChange = output<string>();
  onInput = output<string>();
  onBlur = output<void>();
  onFocus = output<void>();
  tmpValue = '';

  constructor() {
    toObservable(this.value).subscribe((value) => {
      this.tmpValue = value;
    });
  }

  onInputRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).value;
      this.tmpValue = value;
      this.onInput.emit(value);
    }
  }

  onBlurRaw() {
    this.onBlur.emit();
    if (this.tmpValue !== this.value()) {
      this.onChange.emit(this.tmpValue);
    }
  }
}
