import { Component, ElementRef, input, output, signal, viewChild } from '@angular/core';
import { UNIQUE_REF_GENERATOR } from '../../../utils/uniqueRef';

export type ToggleFilterTriState = 'checked' | 'unchecked' | 'indeterminate';

@Component({
  selector: '[toggle-filter],toggle-filter',
  templateUrl: './toggle-filter.component.html',
  styleUrl: './toggle-filter.component.css',
})
export class ToggleFilterComponent {
  readonly id = UNIQUE_REF_GENERATOR.generate();

  label = input.required<string>();
  inputElement = viewChild<ElementRef<HTMLInputElement>>('inputElement');
  state = signal<ToggleFilterTriState>('unchecked');
  filterChanged = output<ToggleFilterTriState>();

  onToggle() {
    let newState: ToggleFilterTriState;
    switch (this.state()) {
      case 'unchecked':
        newState = 'indeterminate';
        break;

      case 'indeterminate':
        newState = 'checked';
        break;

      case 'checked':
        newState = 'unchecked';
        break;
    }
    this.state.set(newState);

    const inputElSnapshot = this.inputElement();
    if (inputElSnapshot) {
      inputElSnapshot.nativeElement.indeterminate = newState === 'indeterminate';
      inputElSnapshot.nativeElement.checked = newState === 'checked';
    }

    this.filterChanged.emit(newState);
  }
}
