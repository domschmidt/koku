import {booleanAttribute, Component, computed, ElementRef, inject, input, output, signal} from '@angular/core';
import {toObservable} from '@angular/core/rxjs-interop';
import {ToastService} from '../../toast/toast.service';
import {KeyValuePipe} from '@angular/common';

let uniqueId = 0;

@Component({
  selector: 'select-field',
  templateUrl: './select-field.component.html',
  styleUrl: './select-field.component.css',
  imports: [
    KeyValuePipe
  ],
  standalone: true
})
export class SelectFieldComponent {

  value = input.required<string, string | number>({
    transform: (v: any) => {
      if (typeof v === 'string') {
        return v;
      } else {
        return String(v);
      }
    }
  });
  defaultValue = input<string>("");
  name = input.required<string>();
  label = input<string>();
  placeholder = input<string>();
  possibleValues = input<KokuDto.SelectFormularFieldPossibleValue[]>([]);
  loading = input(false, {transform: booleanAttribute});
  readonly = input(false, {transform: booleanAttribute});
  required = input(false, {transform: booleanAttribute});
  disabled = input(false, {transform: booleanAttribute});
  valueOnly = input(false, {transform: booleanAttribute});
  clearOnSelect = input(false, {transform: booleanAttribute});

  onChange = output<string | null>();
  onBlur = output<Event>();
  onFocus = output<Event>();

  selectedIdx = computed(() =>
    this.filteredPossibleValues().findIndex(opt => opt.id === this.selectedId())
  );
  selectedId = signal<string | null>(null);
  searchTerm = signal("");
  filteredPossibleValues = computed(() =>
    this.possibleValues().filter(option =>
      (option.text || "").toLowerCase().includes(this.searchTerm().toLowerCase())
    )
  );
  filteredPossibleValuesGrouped = computed(() =>
    Object.groupBy(this.filteredPossibleValues(), ({category}) => category || '')
  );
  showDropdown = signal<{direction: 'top' | 'bottom'} | null>(null);
  elementRef = inject(ElementRef);
  toastService = inject(ToastService);
  id = `select-field-${uniqueId++}`;
  private isOptionClick = false;

  constructor() {
    toObservable(this.value).subscribe((newValue) => {
      let found = false;
      for (const possibleValue of (this.possibleValues() || [])) {
        if (possibleValue.id === newValue) {
          this.selectedId.set(possibleValue.id);
          this.searchTerm.set(possibleValue.text || "");
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

  onInputRaw($event: Event) {
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
      this.selectedId.set((filteredPossibleValuesSnapshot[next] || {}).id || null);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      const prev = selectedIdxSnapshot <= 0 ? filteredPossibleValuesSnapshot.length - 1 : selectedIdxSnapshot - 1;
      this.selectedId.set((filteredPossibleValuesSnapshot[prev] || {}).id || null);
    } else if (event.key === 'Enter') {
      event.preventDefault();
      const currentSelectedValue = filteredPossibleValuesSnapshot[selectedIdxSnapshot];
      if (currentSelectedValue) this.selectOption(currentSelectedValue);
    } else if (event.key === 'Escape') {
      this.selectedId.set(null);
      this.showDropdown.set(null);
    }
  }

  onBlurRaw($event: Event) {
    if (this.isOptionClick) {
      this.isOptionClick = false;
    } else {
      this.showDropdown.set(null);
      if (this.selectedIdx() < 0) {
        this.searchTerm.set("");
        this.onChange.emit(null);
      } else {
        const currentValue = this.filteredPossibleValues()[this.selectedIdx()];
        this.searchTerm.set(currentValue.text || "");
      }
    }
    this.onBlur.emit($event);
  }

  onFocusRaw($event: Event) {
    this.onFocus.emit($event);
    if (this.filteredPossibleValues().length) {
      this.displayDropdown();
    }
  }

  validate(): boolean {
    const valueSnapshot = this.value();
    if ((!valueSnapshot || !valueSnapshot.length) && this.required()) {
      return false;
    }
    return true;
  }

  selectOption(option: KokuDto.SelectFormularFieldPossibleValue) {
    if (option.disabled !== true) {
      this.onChange.emit(option.id || null);
      if (this.clearOnSelect()) {
        this.searchTerm.set("");
      } else {
        this.searchTerm.set(option.text || "");
      }
      this.showDropdown.set(null);
    } else {
      this.toastService.add("Option deaktiviert! Bitte wähle eine andere Option aus. ", 'error')
    }
  }

  onOptionPointerDown() {
    this.isOptionClick = true;
  }

  private displayDropdown() {
    let direction: 'bottom' | 'top' = 'bottom';
    if (window.innerHeight - this.elementRef.nativeElement.getBoundingClientRect().bottom < 400) {
      direction = 'top';
    }

    this.showDropdown.set({
      direction: direction
    });
  }
}
