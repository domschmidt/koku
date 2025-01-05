import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';

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

  moveItemInArray(array: any[], event: CdkDragDrop<any[]>): void {
    moveItemInArray(array, event.previousIndex, event.currentIndex);
  }


}
