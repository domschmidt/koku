import {Component, Input} from '@angular/core';
import {SortableDialogItem} from './sortable-dialog.component';
import {SortableEvent} from 'sortablejs';

@Component({
  selector: 'sortable-dialog-item',
  templateUrl: './sortable-dialog-item.component.html',
  styleUrls: ['./sortable-dialog-item.component.scss']
})
export class SortableDialogItemComponent {

  @Input('item') item!: SortableDialogItem;

  constructor() {
  }
  startDraggingItem(event: SortableEvent): void {
    event.item.classList.add('sortable__item__handle-btn--handle-grabbed');
  }

  endDraggingItem(event: SortableEvent): void {
    event.item.classList.remove('sortable__item__handle-btn--handle-grabbed');
  }
}
