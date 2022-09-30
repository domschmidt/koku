import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "./document.service";
import {AlertDialogButtonConfig, AlertDialogComponent, AlertDialogData} from "../alert-dialog/alert-dialog.component";
import {
  DocumentContextSelectionDialogComponent,
  DocumentContextSelectionDialogComponentResponseData
} from "./document-context-selection-dialog.component";
import {SnackBarService} from "../snackbar/snack-bar.service";

export interface DocumentDetailsComponentData {
  documentId?: number;
  documentDescription?: string;
}

export interface DocumentDetailsComponentResponseData {
  document?: KokuDto.FormularDto;
}

@Component({
  selector: 'document-dialog-component',
  templateUrl: './document-dialog.component.html',
  styleUrls: ['./document-dialog.component.scss']
})
export class DocumentDialogComponent {

  document: KokuDto.FormularDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  private nextFieldId: number = 0;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: DocumentDetailsComponentData,
    public dialogRef: MatDialogRef<DocumentDialogComponent>,
    public dialog: MatDialog,
    public snackBarService: SnackBarService,
    public documentService: DocumentService
  ) {
    this.createMode = data.documentId === undefined;
    if (data.documentId) {
      this.documentService.getDocument(data.documentId).subscribe((document) => {
        this.document = document;
        let nextFieldId = 0;
        for (const currentRow of document.rows || []) {
          for (const currentItem of currentRow.items || []) {
            if (currentItem.id > nextFieldId) {
              nextFieldId = currentItem.id;
            }
          }
        }
        this.nextFieldId = nextFieldId;
        this.loading = false;
      });
    } else {
      const contextSelectionDialogRef = this.dialog.open<DocumentContextSelectionDialogComponent, void, DocumentContextSelectionDialogComponentResponseData>(DocumentContextSelectionDialogComponent, {});
      contextSelectionDialogRef.afterClosed().subscribe((result) => {
        if (result && result.context) {
          this.document = {
            description: this.data.documentDescription || '',
            context: result.context
          };
          this.loading = false;
        } else {
          this.snackBarService.openErrorSnack('Die Dokumentenerstellung wurde abgebrochen.');
          this.dialogRef.close();
        }
      });
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

  duplicate(originalDocument: KokuDto.FormularDto) {
    this.saving = true;
    this.documentService.duplicateDocument(originalDocument).subscribe((result) => {
      const dialogResult: DocumentDetailsComponentResponseData = {
        document: result
      };
      this.dialogRef.close(dialogResult);
      this.saving = false;
      this.snackBarService.openCommonSnack(`Dokument wurde kopiert. Neuer Name: ${result.description}`);
    }, () => {
      this.saving = false;
    });
  }

}
