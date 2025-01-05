import {Component, Input} from '@angular/core';
import {SortableDialogItem} from './sortable-dialog.component';
import {CdkDragDrop, moveItemInArray} from '@angular/cdk/drag-drop';

@Component({
  selector: 'sortable-dialog-item',
  templateUrl: './sortable-dialog-item.component.html',
  styleUrls: ['./sortable-dialog-item.component.scss']
})
export class SortableDialogItemComponent {

  @Input('item') item!: SortableDialogItem;

  constructor() {
  }

  moveItemInArray(array: any[], event: CdkDragDrop<any[]>): void {
    moveItemInArray(array, event.previousIndex, event.currentIndex);
  }
}
