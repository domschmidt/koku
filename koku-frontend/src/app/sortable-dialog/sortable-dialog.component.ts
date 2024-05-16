import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {SortableEvent} from 'sortablejs';

export interface SortableDialogButtonConfig {
  text: string;
  loading?: boolean;
  onClick: (mouseEvent: Event, button: SortableDialogButtonConfig, dialogRef: MatDialogRef<SortableDialogComponent>) => void;
}

export interface SortableDialogItem {
  items?: SortableDialogItem[];
  description: string;
  id: number;
  [key: string]: any;
}

export interface SortableDialogData {
  headline?: string;
  items: SortableDialogItem[];
  buttons?: SortableDialogButtonConfig[];
}

@Component({
  selector: 'sort-dialog',
  templateUrl: './sortable-dialog.component.html'
})
export class SortableDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: SortableDialogData,
              public dialogRef: MatDialogRef<SortableDialogComponent>) {
  }

  startDraggingItem(event: SortableEvent): void {
    event.item.classList.add('sortable__item__handle-btn--handle-grabbed');
  }

  endDraggingItem(event: SortableEvent): void {
    event.item.classList.remove('sortable__item__handle-btn--handle-grabbed');
  }
}
