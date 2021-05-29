import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "../document.service";
import {SortableEvent} from "sortablejs";
import {DocumentSignatureFieldComponent} from "./document-signature-field/document-signature-field.component";
import {DocumentTextFieldComponent} from "./document-text-field/document-text-field.component";
import {DocumentSvgFieldComponent} from "./document-svg-field/document-svg-field.component";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";
import {MatSnackBar} from "@angular/material/snack-bar";
import {DocumentCheckboxFieldComponent} from "./document-checkbox-field/document-checkbox-field.component";

export interface DocumentDetailsComponentData {
  documentId?: number;
  documentDescription?: string;
}

export interface DocumentDetailsComponentResponseData {
  document?: KokuDto.FormularDto;
}

@Component({
  selector: 'document-details',
  templateUrl: './document-details.component.html',
  styleUrls: ['./document-details.component.scss']
})
export class DocumentDetailsComponent {
  private static GRID_SIZE: number = 12;

  document: KokuDto.FormularDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;

  constructor(@Inject(MAT_DIALOG_DATA) public data: DocumentDetailsComponentData,
              public dialogRef: MatDialogRef<DocumentDetailsComponent>,
              public dialog: MatDialog,
              public matSnack: MatSnackBar,
              public documentService: DocumentService) {
    this.createMode = data.documentId === undefined;
    if (data.documentId) {
      this.documentService.getDocument(data.documentId).subscribe((document) => {
        this.document = document;
        this.loading = false;
      });
    } else {
      this.document = {
        description: this.data.documentDescription
      };
      this.loading = false;
    }
  }

  save(document: KokuDto.FormularDto, form: NgForm) {
    if (form.valid) {
      this.saving = true;
      if (!document.id) {
        this.documentService.createDocument(document).subscribe((result) => {
          const dialogResult: DocumentDetailsComponentResponseData = {
            document: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.documentService.updateDocument(document).subscribe(() => {
          const dialogResult: DocumentDetailsComponentResponseData = {
            document
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(user: KokuDto.FormularDto) {
    const dialogData: AlertDialogData = {
      headline: 'Feld Löschen',
      message: `Wollen Sie das Dokument wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          this.saving = true;
          this.documentService.deleteDocument(user).subscribe(() => {
            this.dialogRef.close();
            this.saving = false;
            dialogRef.close();
          });
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

  deleteItem(formField: KokuDto.FormularItemDtoUnion, row: KokuDto.FormularRowDto) {
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

  getAlignStyle(align?: "LEFT" | "CENTER" | "RIGHT") {
    let result = 'left'
    switch (align) {
      case 'CENTER':
        result = 'center';
        break;
      case 'RIGHT':
        result = 'flex-end';
        break;
      case 'LEFT':
        result = 'flex-start';
        break;
      default:
        break;
    }
    return result;
  }

  getFxFlex(size: number) {
    return Math.round((size / DocumentDetailsComponent.GRID_SIZE) * 100) + '%';
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

  editFormField(formField: KokuDto.FormularItemDtoUnion, row: KokuDto.FormularRowDto) {
    switch (formField['@type']) {
      case 'SignatureFormularItemDto':
        const signatureCreationDialog = this.dialog.open(DocumentSignatureFieldComponent, {
          data: formField,
          closeOnNavigation: false,
          width: '100%',
          maxWidth: 500,
          position: {
            top: '20px'
          }
        });
        signatureCreationDialog.afterClosed().subscribe((changedField) => {
          if (changedField && row && row.items) {
            row.items[row.items.indexOf(formField)] = changedField
          }
        });
        break;
      case 'SVGFormularItemDto':
        const svgCreationDialog = this.dialog.open(DocumentSvgFieldComponent, {
          data: formField,
          closeOnNavigation: false,
          width: '100%',
          maxWidth: 500,
          position: {
            top: '20px'
          }
        });
        svgCreationDialog.afterClosed().subscribe((changedField) => {
          if (changedField && row && row.items) {
            row.items[row.items.indexOf(formField)] = changedField
          }
        });
        break;
      case 'TextFormularItemDto':
        const textCreationDialog = this.dialog.open(DocumentTextFieldComponent, {
          data: formField,
          closeOnNavigation: false,
          width: '100%',
          maxWidth: 500,
          position: {
            top: '20px'
          }
        });
        textCreationDialog.afterClosed().subscribe((changedField) => {
          if (changedField && row && row.items) {
            row.items[row.items.indexOf(formField)] = changedField
          }
        });
        break;
      case 'CheckboxFormularItemDto':
        const checkboxCreationDialog = this.dialog.open(DocumentCheckboxFieldComponent, {
          data: formField,
          closeOnNavigation: false,
          width: '100%',
          maxWidth: 500,
          position: {
            top: '20px'
          }
        });
        checkboxCreationDialog.afterClosed().subscribe((changedField) => {
          if (changedField && row && row.items) {
            row.items[row.items.indexOf(formField)] = changedField
          }
        });
        break;
    }
  }

  createSVGField(row: KokuDto.FormularRowDto, targetPosition?: number) {
    const fieldCreationDialog = this.dialog.open(DocumentSvgFieldComponent, {
      closeOnNavigation: false,
      width: '100%',
      maxWidth: 500,
      position: {
        top: '20px'
      }
    });
    fieldCreationDialog.afterClosed().subscribe((createdField) => {
      if (createdField && row && row.items) {
        if (targetPosition === undefined) {
          row.items.push(createdField);
        } else {
          const itemsClone = [...row.items];
          itemsClone.splice(targetPosition, 0, createdField);
          row.items = itemsClone;
        }
      }
    });
  }

  createTextField(row: KokuDto.FormularRowDto, targetPosition?: number) {
    const fieldCreationDialog = this.dialog.open(DocumentTextFieldComponent, {
      closeOnNavigation: false,
      width: '100%',
      maxWidth: 500,
      position: {
        top: '20px'
      }
    });
    fieldCreationDialog.afterClosed().subscribe((createdField) => {
      if (createdField && row && row.items) {
        if (targetPosition === undefined) {
          row.items.push(createdField);
        } else {
          const itemsClone = [...row.items];
          itemsClone.splice(targetPosition, 0, createdField);
          row.items = itemsClone;
        }
      }
    });
  }

  createCheckboxField(row: KokuDto.FormularRowDto, targetPosition?: number) {
    const fieldCreationDialog = this.dialog.open(DocumentCheckboxFieldComponent, {
      closeOnNavigation: false,
      width: '100%',
      maxWidth: 500,
      position: {
        top: '20px'
      }
    });
    fieldCreationDialog.afterClosed().subscribe((createdField) => {
      if (createdField && row && row.items) {
        if (targetPosition === undefined) {
          row.items.push(createdField);
        } else {
          const itemsClone = [...row.items];
          itemsClone.splice(targetPosition, 0, createdField);
          row.items = itemsClone;
        }
      }
    });
  }

  createSignatureField(row: KokuDto.FormularRowDto, targetPosition?: number) {
    const fieldCreationDialog = this.dialog.open(DocumentSignatureFieldComponent, {
      closeOnNavigation: false,
      width: '100%',
      maxWidth: 500,
      position: {
        top: '20px'
      }
    });
    fieldCreationDialog.afterClosed().subscribe((createdField) => {
      if (createdField && row && row.items) {
        if (targetPosition === undefined) {
          row.items.push(createdField);
        } else {
          const itemsClone = [...row.items];
          itemsClone.splice(targetPosition, 0, createdField);
          row.items = itemsClone;
        }
      }
    });
  }

  addRow(document: KokuDto.FormularDto, targetPosition?: number) {
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

  deleteRow(row: KokuDto.FormularRowDto, rows: KokuDto.FormularRowDto[]) {
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
  }

  duplicate(originalDocument: KokuDto.FormularDto) {
    this.saving = true;
    this.documentService.duplicateDocument(originalDocument).subscribe((result) => {
      const dialogResult: DocumentDetailsComponentResponseData = {
        document: result
      };
      this.dialogRef.close(dialogResult);
      this.saving = false;
      this.matSnack.open(`Dokument wurde kopiert. Neuer Name: ${result.description}`, 'ok', {
        duration: 5000
      });
    }, () => {
      this.saving = false;
    });
  }

}
