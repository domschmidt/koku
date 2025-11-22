import {booleanAttribute, Component, input, output, signal} from '@angular/core';
import {toObservable} from '@angular/core/rxjs-interop';
import {get} from '../../utils/get';
import {MultiSelectFieldComponent} from '../multi-select/multi-select-field.component';
import {InputFieldComponent} from '../input/input-field.component';
import {IconComponent} from '../../icon/icon.component';
import {moveItemInArray} from '@angular/cdk/drag-drop';

@Component({
  selector: 'multi-select-with-prices-field',
  templateUrl: './multi-select-with-prices-field.component.html',
  styleUrl: './multi-select-with-prices-field.component.css',
  imports: [
    MultiSelectFieldComponent,
    InputFieldComponent,
    IconComponent
  ],
  standalone: true
})
export class MultiSelectWithPricesFieldComponent {

  value = input.required<{ [key: string]: any }[]>();
  defaultValue = input<any[]>([]);
  name = input.required<string>();
  label = input<string>();
  placeholder = input<string>();
  possibleValues = input<KokuDto.SelectFormularFieldPossibleValue[]>([]);
  loading = input(false, {transform: booleanAttribute});
  readonly = input(false, {transform: booleanAttribute});
  required = input(false, {transform: booleanAttribute});
  disabled = input(false, {transform: booleanAttribute});
  valueOnly = input(false, {transform: booleanAttribute});
  idPathMapping = input.required<string>();
  pricePathMapping = input.required<string>();
  uniqueValues = input(false, {transform: booleanAttribute});

  priceModal = signal<{
    item: { [key: string]: any };
    useDefaultPrice: boolean;
    price: number;
  } | null>(null);
  selectionIds = signal<string[]>([]);
  selectionValues = signal<any[]>([]);
  // selectionIdx = signal<{ [key: string]: { [key: string]: any } }>({});

  onChange = output<{ [key: string]: any }[] | null>();
  onBlur = output<Event>();
  onFocus = output<Event>();

  constructor() {
    toObservable(this.value).subscribe((newValue) => {
      const newSelectionIds: string[] = [];
      const newSelectionValues: any[] = [];
      // const newSelectionIdx: { [key: string]: object } = {};
      for (const currentNewValue of newValue) {
        let id = get(currentNewValue, this.idPathMapping());
        if (typeof id !== 'string') {
          id = String(id);
        }

        newSelectionIds.push(id);
        // newSelectionIdx[id] = currentNewValue
        newSelectionValues.push(currentNewValue);
      }

      this.selectionIds.set(newSelectionIds);
      this.selectionValues.set(newSelectionValues);
      // this.selectionIdx.set(newSelectionIdx);
    });
  }

  onSelectItemClick(payload: { event: Event, id: string, pos: number }) {
    const selectedItem = this.selectionValues()[payload.pos];
    if (!selectedItem) {
      throw new Error(`Selected item with pos ${payload.pos} not found`);
    }
    const price = get(selectedItem, this.pricePathMapping());
    this.priceModal.set({
      useDefaultPrice: price === null || price === undefined,
      price: price,
      item: selectedItem
    });
  }

  protected readonly String = String;
  protected readonly Number = Number;

  closePriceModal() {
    this.priceModal.set(null);
  }

  applyPrice() {
    const priceModalSnapshot = this.priceModal();
    if (priceModalSnapshot && priceModalSnapshot.item) {
      const selectionValuesSnapshot = this.selectionValues();
      const itemIdx = selectionValuesSnapshot.indexOf(priceModalSnapshot.item);
      if (itemIdx >= 0) {
        selectionValuesSnapshot[itemIdx][this.pricePathMapping()] = priceModalSnapshot.useDefaultPrice ? undefined : priceModalSnapshot.price
      }
      this.selectionValues.set(selectionValuesSnapshot);
      this.emitChange();
      this.closePriceModal();
    }
  }

  private emitChange() {
    this.onChange.emit(this.selectionValues());
  }

  onSelectionDeleted($event: { position: number; id: string }) {
    const selectionIdsSnapshot = this.selectionIds();
    const selectionValuesSnapshot = this.selectionValues();
    selectionIdsSnapshot.splice($event.position, 1)
    selectionValuesSnapshot.splice($event.position, 1)
    this.selectionIds.set(selectionIdsSnapshot);
    this.selectionValues.set(selectionValuesSnapshot);
    this.emitChange();
  }

  onSelectionMoved($event: { oldPosition: number; newPosition: number; id: string }) {
    const selectionIdsSnapshot = this.selectionIds();
    const selectionValuesSnapshot = this.selectionValues();
    moveItemInArray(selectionIdsSnapshot, $event.oldPosition, $event.newPosition);
    moveItemInArray(selectionValuesSnapshot, $event.oldPosition, $event.newPosition);
    this.selectionIds.set(selectionIdsSnapshot);
    this.selectionValues.set(selectionValuesSnapshot);
    this.emitChange();
  }

  onSelectionAdded(id: string) {
    this.selectionIds().push(id);
    this.selectionValues().push({
      [this.idPathMapping()]: id,
      [this.pricePathMapping()]: undefined,
    });
    this.emitChange();
  }
}
