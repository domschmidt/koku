import { booleanAttribute, Component, input, output } from '@angular/core';

let uniqueId = 0;

@Component({
  selector: 'textarea-field',
  templateUrl: './textarea-field.component.html',
  styleUrl: './textarea-field.component.css',
  imports: [],
  standalone: true,
})
export class TextareaFieldComponent {
  value = input.required<string>();
  defaultValue = input<string>('');
  name = input.required<string>();
  label = input<string>();
  placeholder = input<string>();
  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });
  onChange = output<string>();
  onInput = output<string>();
  onBlur = output<void>();
  onFocus = output<void>();
  private supportsFieldSizingNatively: boolean;
  id = `textarea-field-${uniqueId++}`;

  constructor() {
    this.supportsFieldSizingNatively = CSS.supports('field-sizing', 'content');
  }

  onInputRaw($event: Event) {
    if ($event.target) {
      const targetEl = $event.target as HTMLTextAreaElement;
      const value = targetEl.value;
      this.onInput.emit(value);
      if (!this.supportsFieldSizingNatively) {
        setTimeout(() => {
          this.applyAutoHeight(targetEl);
        });
      }
    }
  }

  private applyAutoHeight(targetEl: HTMLTextAreaElement) {
    targetEl.style.height = 'auto';
    const computedStyle = getComputedStyle(targetEl);
    targetEl.style.height =
      'calc(' +
      targetEl.scrollHeight +
      'px' +
      ' + ' +
      computedStyle.borderTopWidth +
      ' + ' +
      computedStyle.borderBottomWidth +
      ')';
  }

  onChangeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLTextAreaElement).value;
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
