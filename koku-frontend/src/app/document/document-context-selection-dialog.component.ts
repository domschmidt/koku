import {Component} from '@angular/core';
import {NgForm} from "@angular/forms";
import {DocumentService} from "./document.service";
import {SnackBarService} from "../snackbar/snack-bar.service";
import {MatDialogRef} from "@angular/material/dialog";

export interface DocumentContextSelectionDialogComponentResponseData {
  context: KokuDto.DocumentContextDto;
}

@Component({
  selector: 'document-context-selection-dialog-component',
  template: `
    <h2 mat-dialog-title>Kontext auswählen</h2>
    <form #form="ngForm"
          (keydown.enter)="apply(form)"
          (submit)="apply(form)"
          *ngIf="availableContexts">
      <mat-dialog-content>
        <mat-form-field class="document-context-field">
          <mat-label>Kontext</mat-label>
          <mat-select [(value)]="context"
                      required>
            <mat-option [value]="currentContext"
                        *ngFor="let currentContext of availableContexts">
              {{currentContext.description}}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close>Abbrechen</button>
        <button [disabled]="form.invalid"
                mat-button
                type="submit">
          Übernehmen
        </button>
      </mat-dialog-actions>
    </form>
  `
})
export class DocumentContextSelectionDialogComponent {

  availableContexts: KokuDto.DocumentContextDto[] | undefined;
  context: KokuDto.DocumentContextDto | undefined;

  constructor(
    private readonly documentService: DocumentService,
    private readonly snackBarService: SnackBarService,
    public dialogRef: MatDialogRef<DocumentContextSelectionDialogComponent, DocumentContextSelectionDialogComponentResponseData>,
  ) {
    this.documentService.getDocumentContexts().subscribe((contexts) => {
      this.availableContexts = contexts;
    }, (error) => {
      this.snackBarService.openErrorSnack(error.error.message)
    });
  }

  apply(form: NgForm) {
    if (form.valid && this.context) {
      this.dialogRef.close({
        context: this.context
      });
    }
  }
}
