import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from '../../../document/document.service';
import {DocumentFieldMeta} from '../../../document-designer-module/document-field-config';

@Component({
  selector: 'document-date-field',
  templateUrl: './document-date-config-field.component.html',
  styleUrls: ['./document-date-config-field.component.scss']
})
export class DocumentDateConfigFieldComponent {

  dateField: KokuDto.DateFormularItemDto | undefined;
  saving = false;
  loading = true;
  createMode: boolean;
  replacementTokens: KokuDto.FormularReplacementTokenDto[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: {
                field?: KokuDto.DateFormularItemDto
                meta: DocumentFieldMeta
              },
              public dialogRef: MatDialogRef<DocumentDateConfigFieldComponent>,
              public dialog: MatDialog,
              public documentService: DocumentService) {
    this.createMode = data.field === undefined;
    if (data.field === undefined) {
      this.dateField = {
        id: 0,
        ['@type']: 'DateFormularItemDto'
      };
    } else {
      this.dateField = {...data.field};
    }
    this.replacementTokens = data.meta.replacementTokens;
  }

  save(form: NgForm): void {
    if (form.valid) {
      this.dialogRef.close(this.dateField);
    }
  }
}
