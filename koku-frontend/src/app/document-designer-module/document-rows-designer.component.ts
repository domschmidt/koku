import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {SortableEvent} from 'sortablejs';
import {AlertDialogButtonConfig, AlertDialogComponent, AlertDialogData} from '../alert-dialog/alert-dialog.component';
import {DocumentFieldHostDirective} from './document-field-host.directive';
import {DocumentBaseRow, DocumentBaseRowItem} from './document.interface';
import {DocumentFieldConfigurationTypes} from './document-field-config';

@Component({
  selector: 'document-rows-designer',
  templateUrl: './document-rows-designer.component.html',
  styleUrls: ['./document-rows-designer.component.scss']
})
export class DocumentRowsDesignerComponent implements OnInit {

  @Input() rows!: DocumentBaseRow[];
  @Input() fieldConfig!: DocumentFieldConfigurationTypes;
  @Input() gridSize!: number;
  @ViewChild(DocumentFieldHostDirective, {static: true}) fieldHost!: DocumentFieldHostDirective;

  private nextFieldId = 0;

  constructor(
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    let nextFieldId = 0;
    for (const currentRow of this.rows || []) {
      for (const currentItem of currentRow.items || []) {
        if (currentItem.id && currentItem.id > nextFieldId) {
          nextFieldId = currentItem.id;
        }
      }
    }
    this.nextFieldId = nextFieldId;
  }

  deleteItem(formField: DocumentBaseRowItem, row: DocumentBaseRow): void {
    const dialogData: AlertDialogData = {
      headline: 'Feld Löschen',
      message: `Wollen Sie das Feld wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          if (row && row.items) {
            row.items.splice(row.items.indexOf(formField) || 0, 1);
          }
          dialogRef.close();
        }
      }]
    };

    this.dialog.open(AlertDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  getFxFlex(size: number): string {
    return Math.round((size / this.gridSize) * 100) + '%';
  }

  startDragging(event: SortableEvent): void {
    event.item.classList.add('document__row--handle-grabbed');
  }

  endDragging(event: SortableEvent): void {
    event.item.classList.remove('document__row--handle-grabbed');
  }

  startDraggingItem(event: SortableEvent): void {
    event.item.classList.add('document__row__contents__item__menu__handle-btn--handle-grabbed');
  }

  endDraggingItem(event: SortableEvent): void {
    event.item.classList.remove('document__row__contents__item__menu__handle-btn--handle-grabbed');
  }

  editFormField(formField: DocumentBaseRowItem, row: DocumentBaseRow): void {
    const componentType = this.fieldConfig[formField['@type']].component;
    const meta = this.fieldConfig[formField['@type']].meta;
    if (componentType !== undefined) {
      const creationDialog = this.dialog.open<typeof componentType>(componentType, {
        data: {
          field: formField,
          meta
        },
        closeOnNavigation: false,
        width: '100%',
        maxWidth: 500,
        position: {
          top: '20px'
        }
      });
      creationDialog.afterClosed().subscribe((changedField) => {
        if (changedField && row && row.items) {
          row.items[row.items.indexOf(formField)] = changedField;
        }
      });
    }
  }

  createField(typeName: string, row: DocumentBaseRow, targetPosition?: number): void {
    const componentType = this.fieldConfig[typeName].component;
    const meta = this.fieldConfig[typeName].meta;
    if (componentType !== undefined) {
      const fieldCreationDialog = this.dialog.open<typeof componentType>(componentType, {
        closeOnNavigation: false,
        data: {
          meta
        },
        width: '100%',
        maxWidth: 500,
        position: {
          top: '20px'
        }
      });
      fieldCreationDialog.afterClosed().subscribe((createdField) => {
        if (createdField && row && row.items) {
          if (!row.items) {
            row.items = [];
          }
          if (targetPosition === undefined) {
            row.items.push({
              ...createdField,
              id: ++this.nextFieldId
            });
          } else {
            row.items.splice(targetPosition, 0, {
              ...createdField,
              id: ++this.nextFieldId
            });
          }
        }
      });
    }
  }

  addRow(targetPosition?: number): void {
    const newRowStub = {items: []};
    if (targetPosition === undefined) {
      this.rows.push(newRowStub);
    } else {
      this.rows.splice(targetPosition, 0, newRowStub);
    }
  }

  deleteRow(row: DocumentBaseRow): void {
    if (row && row.items && row.items.length > 0) {
      const dialogData: AlertDialogData = {
        headline: 'Feld Löschen',
        message: `Wollen Sie diese Zeile wirklich löschen?`,
        buttons: [{
          text: 'Abbrechen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            dialogRef.close();
          }
        }, {
          text: 'Bestätigen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            if (row && row.items) {
              this.rows.splice(this.rows.indexOf(row) || 0, 1);
            }
            dialogRef.close();
          }
        }]
      };

      this.dialog.open(AlertDialogComponent, {
        data: dialogData,
        width: '100%',
        maxWidth: 700,
        closeOnNavigation: false,
        position: {
          top: '20px'
        }
      });
    } else {
      this.rows.splice(this.rows.indexOf(row) || 0, 1);
    }
  }

}
