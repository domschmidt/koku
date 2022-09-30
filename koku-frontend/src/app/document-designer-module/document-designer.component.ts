import {Component, Inject, Input, OnInit, ViewChild} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {SortableEvent} from "sortablejs";
import {AlertDialogButtonConfig, AlertDialogComponent, AlertDialogData} from "../alert-dialog/alert-dialog.component";
import {DocumentFieldHostDirective} from "./document-field-host.directive";
import {DOCUMENT_CONFIG, DocumentConfig} from "./document-field-config.injector";
import {DocumentBase, DocumentBaseRow, DocumentBaseRowItem} from "./document.interface";

@Component({
  selector: 'document-designer',
  templateUrl: './document-designer.component.html',
  styleUrls: ['./document-designer.component.scss']
})
export class DocumentDesignerComponent implements OnInit {

  @Input() document!: DocumentBase;
  @ViewChild(DocumentFieldHostDirective, {static: true}) fieldHost!: DocumentFieldHostDirective;

  private nextFieldId: number = 0;

  constructor(
    @Inject(DOCUMENT_CONFIG) public readonly documentConfig: DocumentConfig,
    private readonly dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    let nextFieldId = 0;
    if (this.document !== undefined) {
      for (const currentRow of this.document.rows || []) {
        for (const currentItem of currentRow.items || []) {
          if (currentItem.id && currentItem.id > nextFieldId) {
            nextFieldId = currentItem.id;
          }
        }
      }
    }
    this.nextFieldId = nextFieldId;
  }

  deleteItem(formField: DocumentBaseRowItem, row: DocumentBaseRow) {
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

  getFxFlex(size: number) {
    return Math.round((size / this.documentConfig.gridSize) * 100) + '%';
  }

  startDragging(event: SortableEvent) {
    event.item.classList.add('document__row--handle-grabbed');
  }

  endDragging(event: SortableEvent) {
    event.item.classList.remove('document__row--handle-grabbed');
  }

  startDraggingItem(event: SortableEvent) {
    event.item.classList.add('document__row__contents__item__menu__handle-btn--handle-grabbed');
  }

  endDraggingItem(event: SortableEvent) {
    event.item.classList.remove('document__row__contents__item__menu__handle-btn--handle-grabbed');
  }

  editFormField(formField: DocumentBaseRowItem, row: DocumentBaseRow) {
    const componentType = this.documentConfig.fields[formField['@type']].configComponent;
    if (componentType !== undefined) {
      const creationDialog = this.dialog.open<typeof componentType>(componentType, {
        data: {
          field: formField,
          document: {...this.document}
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
          row.items[row.items.indexOf(formField)] = changedField
        }
      });
    }
  }

  createField(typeName: string, row: DocumentBaseRow, targetPosition?: number) {
    const componentType = this.documentConfig.fields[typeName].configComponent;
    if (componentType !== undefined) {
      const fieldCreationDialog = this.dialog.open<typeof componentType>(componentType, {
        closeOnNavigation: false,
        data: {
          document: {...this.document}
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
            row.items = []
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

  addRow(document: DocumentBase, targetPosition?: number) {
    const newRowStub = {items: []};
    if (!document.rows) {
      document.rows = [];
    }
    if (targetPosition === undefined) {
      document.rows.push(newRowStub);
    } else {
      document.rows.splice(targetPosition, 0, newRowStub);
    }
  }

  deleteRow(row: DocumentBaseRow, rows: DocumentBaseRow[]) {
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
              rows.splice(rows.indexOf(row) || 0, 1);
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
      rows.splice(rows.indexOf(row) || 0, 1);
    }
  }

}
