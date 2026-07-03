import {
  booleanAttribute,
  Component,
  computed,
  ElementRef,
  inject,
  input,
  output,
  signal,
  ViewChild,
} from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { ToastService } from '../../toast/toast.service';
import { KeyValuePipe } from '@angular/common';
import { colorTextClass } from '../../utils/color.utils';

let uniqueId = 0;

@Component({
  selector: 'select-field',
  templateUrl: './select-field.component.html',
  styleUrl: './select-field.component.css',
  imports: [KeyValuePipe],
  standalone: true,
})
export class SelectFieldComponent {
  readonly colorTextClass = colorTextClass;
  @ViewChild('searchInput') searchInput!: ElementRef<HTMLInputElement>;
  value = input.required<string, string | number>({
    transform: (v: any) => {
      if (typeof v === 'string') {
        return v;
      } else {
        return String(v);
      }
    },
  });
  defaultValue = input<string>('');
  name = input.required<string>();
  label = input<string>();
  placeholder = input<string>();
  possibleValues = input<KokuDto.SelectFormularFieldPossibleValue[]>([]);
  loading = input(false, { transform: booleanAttribute });
  keepOpenOnSelect = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });
  clearOnSelect = input(false, { transform: booleanAttribute });

  changed = output<string | null>();
  blurred = output<Event>();
  focused = output<Event>();

  selectedIdx = computed(() => this.filteredPossibleValues().findIndex((opt) => opt.id === this.selectedId()));
  selectedId = signal<string | null>(null);
  searchTerm = signal('');
  filteredPossibleValues = computed(() => {
    const terms = this.searchTerm().toLowerCase().split(/\s+/).filter(Boolean);
    return this.possibleValues().filter((option) => {
      if (option.disabled) {
        return false;
      }
      const text = (option.text || '').toLowerCase();
      return terms.every((term) => text.includes(term));
    });
  });
  filteredPossibleValuesGrouped = computed(() =>
    Object.groupBy(this.filteredPossibleValues(), ({ category }) => category || ''),
  );
  showDropdown = signal<{ direction: 'top' | 'bottom' } | null>(null);
  elementRef = inject(ElementRef);
  toastService = inject(ToastService);
  id = `select-field-${uniqueId++}`;
  private isOptionClick = false;

  constructor() {
    toObservable(this.value).subscribe((newValue) => {
      let found = false;
      for (const possibleValue of this.possibleValues() || []) {
        if (possibleValue.id === newValue) {
          this.selectedId.set(possibleValue.id);
          this.searchTerm.set(possibleValue.text || '');
          found = true;
          break;
        }
      }
      if (!found) {
        this.selectedId.set(null);
        this.searchTerm.set('');
      }
    });
  }

  typeRaw($event: Event) {
    if ($event.target) {
      const value = ($event.target as HTMLInputElement).value;
      this.searchTerm.set(value);
      this.selectedId.set(null);
      this.displayDropdown();
    }
  }

  onKeyDownRaw(event: KeyboardEvent) {
    if (!this.showDropdown()) {
      if (event.key === 'ArrowDown') {
        this.displayDropdown();
      } else {
        return;
      }
    }

    const selectedIdxSnapshot = this.selectedIdx();
    const filteredPossibleValuesSnapshot = this.filteredPossibleValues();
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      const next = selectedIdxSnapshot === -1 ? 0 : (selectedIdxSnapshot + 1) % filteredPossibleValuesSnapshot.length;
      this.selectedId.set(filteredPossibleValuesSnapshot[next]?.id || null);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      const prev = selectedIdxSnapshot <= 0 ? filteredPossibleValuesSnapshot.length - 1 : selectedIdxSnapshot - 1;
      this.selectedId.set(filteredPossibleValuesSnapshot[prev]?.id || null);
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const currentSelectedValue = filteredPossibleValuesSnapshot[selectedIdxSnapshot];
      if (currentSelectedValue) this.selectOption(currentSelectedValue);
    } else if (event.key === 'Escape') {
      this.selectedId.set(null);
      this.showDropdown.set(null);
    }
  }

  blurredRaw($event: Event) {
    if (this.isOptionClick) {
      this.isOptionClick = false;
    } else {
      this.showDropdown.set(null);
      if (this.selectedIdx() < 0) {
        this.searchTerm.set('');
        this.changed.emit(null);
      } else {
        const currentValue = this.filteredPossibleValues()[this.selectedIdx()];
        this.searchTerm.set(currentValue.text || '');
      }
    }
    this.blurred.emit($event);
  }

  focusedRaw($event: Event) {
    this.focused.emit($event);
    if (this.filteredPossibleValues().length) {
      this.displayDropdown();
    }
  }

  validate(): boolean {
    const valueSnapshot = this.value();
    return Boolean(valueSnapshot?.length) || this.required() === false;
  }

  selectOption(option: KokuDto.SelectFormularFieldPossibleValue) {
    if (option.disabled !== true) {
      this.changed.emit(option.id || null);
      if (this.clearOnSelect()) {
        this.searchTerm.set('');
      } else {
        this.searchTerm.set(option.text || '');
      }
      if (this.keepOpenOnSelect()) {
        this.searchInput.nativeElement.focus();
      } else {
        this.showDropdown.set(null);
      }
    } else {
      this.toastService.add('Option deaktiviert! Bitte wähle eine andere Option aus. ', 'error');
    }
  }

  onOptionPointerDown() {
    this.isOptionClick = true;
  }

  private displayDropdown() {
    let direction: 'bottom' | 'top' = 'bottom';
    if (globalThis.innerHeight - this.elementRef.nativeElement.getBoundingClientRect().bottom < 400) {
      direction = 'top';
    }

    this.showDropdown.set({
      direction: direction,
    });
  }
}
