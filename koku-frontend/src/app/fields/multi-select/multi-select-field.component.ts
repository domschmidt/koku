import {
  booleanAttribute,
  Component,
  computed,
  ElementRef,
  input,
  output,
  QueryList,
  signal,
  TemplateRef,
  ViewChildren,
} from '@angular/core';
import { SelectFieldComponent } from '../select/select-field.component';
import { toObservable } from '@angular/core/rxjs-interop';
import { CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray } from '@angular/cdk/drag-drop';
import { NgTemplateOutlet } from '@angular/common';
import { isMatch } from '../../utils/ismatch';
import { get } from '../../utils/get';
import { set } from '../../utils/set';

@Component({
  selector: 'multi-select-field',
  templateUrl: './multi-select-field.component.html',
  styleUrl: './multi-select-field.component.css',
  imports: [SelectFieldComponent, CdkDropList, CdkDrag, NgTemplateOutlet],
  standalone: true,
})
export class MultiSelectFieldComponent {
  value = input.required<any[]>();
  defaultValue = input<string[]>([]);
  name = input.required<string>();
  label = input<string>();
  placeholder = input<string>();
  possibleValues = input<KokuDto.MultiSelectFormularFieldPossibleValue[]>([]);
  loading = input(false, { transform: booleanAttribute });
  readonly = input(false, { transform: booleanAttribute });
  required = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  valueOnly = input(false, { transform: booleanAttribute });

  selectionIds = signal<string[]>([]);
  possibleValuesIdx = computed(() => {
    const result: Record<string, KokuDto.MultiSelectFormularFieldPossibleValue> = {};
    for (const currentValue of this.possibleValues() || []) {
      if (currentValue.id !== undefined) {
        let key = currentValue.id;
        if (typeof key !== 'string') {
          key = String(key);
        }
        result[key] = currentValue;
      }
    }
    return result;
  });
  filteredPossibleValues = signal<KokuDto.MultiSelectFormularFieldPossibleValue[]>([]);
  badgeContentTemplate = input<TemplateRef<any>>();
  idPathMapping = input<string>();
  uniqueValues = input(false, { transform: booleanAttribute });

  @ViewChildren('badgeEl') badgeEls: QueryList<ElementRef<HTMLButtonElement>> | undefined;

  onSelectItemClick = output<{
    event: Event;
    id: string;
    pos: number;
  }>();
  onChange = output<any[]>();
  onBlur = output<Event>();
  onFocus = output<Event>();
  onSelect = output<string>();
  onMove = output<{
    oldPosition: number;
    newPosition: number;
    id: string;
  }>();
  onDelete = output<{
    position: number;
    id: string;
  }>();

  constructor() {
    toObservable(this.value).subscribe((newValues) => {
      const idPathMappingSnapshot = this.idPathMapping();
      const selectionIds: string[] = [];
      for (const currentValue of newValues) {
        const matchingValue = (this.possibleValues() || []).find((value) => {
          if (idPathMappingSnapshot) {
            return String(get(currentValue, idPathMappingSnapshot)) === value.id;
          }
          return isMatch(value.valueMapping, currentValue);
        });
        if (matchingValue === undefined || matchingValue === null) {
          throw new Error(`Unknown value to match`);
        }
        let key = matchingValue.id;
        if (typeof key !== 'string') {
          key = String(key);
        }
        selectionIds.push(key);
      }
      this.selectionIds.set(selectionIds);
      this.filteredPossibleValues.set(this.filterPossibleValues(selectionIds));
    });
    toObservable(this.possibleValues).subscribe(() => {
      this.filteredPossibleValues.set(this.filterPossibleValues(this.selectionIds()));
    });
  }

  selectById($event: string | null) {
    if ($event) {
      const newSelectionIds = [...this.selectionIds(), $event];
      this.selectionIds.set(newSelectionIds);
      this.filteredPossibleValues.set(this.filterPossibleValues(newSelectionIds));
      this.onSelect.emit($event);
      this.emitOnChange(newSelectionIds);
    }
  }

  onSelectionDelete(event: Event, currentSelectionIdx: number) {
    event.preventDefault();
    event.stopPropagation();
    const selectionIdsSnapshot = this.selectionIds();
    const deletedId = selectionIdsSnapshot[currentSelectionIdx];
    selectionIdsSnapshot.splice(currentSelectionIdx, 1);
    this.selectionIds.set(selectionIdsSnapshot);
    this.filteredPossibleValues.set(this.filterPossibleValues(selectionIdsSnapshot));
    this.onDelete.emit({
      position: currentSelectionIdx,
      id: deletedId,
    });
    this.emitOnChange(selectionIdsSnapshot);
  }

  drop(event: CdkDragDrop<string[]>) {
    const selectionIdsSnapshot = this.selectionIds();
    const movedId = selectionIdsSnapshot[event.currentIndex];
    moveItemInArray(selectionIdsSnapshot, event.previousIndex, event.currentIndex);
    this.selectionIds.set(selectionIdsSnapshot);
    this.onMove.emit({
      oldPosition: event.previousIndex,
      newPosition: event.currentIndex,
      id: movedId,
    });
    this.emitOnChange(selectionIdsSnapshot);
  }

  private filterPossibleValues(selectionIds: string[]) {
    const currentPossibleValuesSnapshot = this.possibleValues() || [];
    if (this.uniqueValues()) {
      return currentPossibleValuesSnapshot.filter((currentPossibleValue) => {
        return currentPossibleValue.id === undefined || selectionIds.indexOf(currentPossibleValue.id) < 0;
      });
    } else {
      return currentPossibleValuesSnapshot;
    }
  }

  onSelectItemKeyDownRaw(event: KeyboardEvent, currentSelectionId: string) {
    const selectionIdsSnapshot = this.selectionIds();
    const oldIdx = selectionIdsSnapshot.indexOf(currentSelectionId);
    if (['ArrowDown', 'ArrowRight'].indexOf(event.key) >= 0) {
      event.preventDefault();
      moveItemInArray(selectionIdsSnapshot, oldIdx, Math.min(selectionIdsSnapshot.length, oldIdx + 1));
      this.selectionIds.set(selectionIdsSnapshot);
    } else if (['ArrowUp', 'ArrowLeft'].indexOf(event.key) >= 0) {
      event.preventDefault();
      const newIdx = Math.max(0, oldIdx - 1);
      moveItemInArray(selectionIdsSnapshot, oldIdx, Math.max(0, newIdx));
      this.selectionIds.set(selectionIdsSnapshot);
      setTimeout(() => {
        if (this.badgeEls) {
          const firstBadge = this.badgeEls.get(newIdx);
          if (firstBadge) {
            firstBadge.nativeElement.focus();
          }
        }
      });
    } else if (['Backspace', 'Delete'].indexOf(event.key) >= 0) {
      event.preventDefault();
      const newIdx = Math.max(0, oldIdx - 1);
      this.onSelectionDelete(event, oldIdx);
      setTimeout(() => {
        if (this.badgeEls) {
          const firstBadge = this.badgeEls.get(newIdx);
          if (firstBadge) {
            firstBadge.nativeElement.focus();
          }
        }
      });
    }
  }

  private emitOnChange(newSelectionIds: string[]) {
    const idPathMappingSnapshot = this.idPathMapping();
    const resultToBePublished: any[] = [];
    const possibleValueIdxSnapshot = this.possibleValuesIdx();
    for (const currentSelectionId of newSelectionIds) {
      const currentValue = possibleValueIdxSnapshot[currentSelectionId];
      if (!currentValue) {
        throw new Error('selectionId not found: ' + currentSelectionId);
      }
      if (currentValue.valueMapping) {
        resultToBePublished.push(currentValue.valueMapping);
      } else if (idPathMappingSnapshot) {
        const value = {};
        set(value, idPathMappingSnapshot, currentValue.id);
        resultToBePublished.push(value);
      } else {
        resultToBePublished.push(currentValue.id);
      }
    }
    this.onChange.emit(resultToBePublished);
  }
}
