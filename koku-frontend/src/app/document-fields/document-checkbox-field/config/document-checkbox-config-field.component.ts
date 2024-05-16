import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from '../../../document/document.service';
import {DocumentFieldMeta} from '../../../document-designer-module/document-field-config';

@Component({
  selector: 'document-checkbox-field',
  templateUrl: './document-checkbox-config-field.component.html',
  styleUrls: ['./document-checkbox-config-field.component.scss']
})
export class DocumentCheckboxConfigFieldComponent {

  checkboxField: KokuDto.CheckboxFormularItemDto | undefined;
  saving = false;
  loading = true;
  createMode: boolean;
  replacementTokens: KokuDto.FormularReplacementTokenDto[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: {
                field?: KokuDto.CheckboxFormularItemDto
                meta: DocumentFieldMeta
              },
              public dialogRef: MatDialogRef<DocumentCheckboxConfigFieldComponent>,
              public dialog: MatDialog,
              public documentService: DocumentService) {
    this.createMode = data.field === undefined;
    if (data.field === undefined) {
      this.checkboxField = {
        id: 0,
        ['@type']: 'CheckboxFormularItemDto'
      };
    } else {
      this.checkboxField = {...data.field};
    }
    this.replacementTokens = data.meta.replacementTokens;
  }

  save(form: NgForm): void {
    if (form.valid) {
      this.dialogRef.close(this.checkboxField);
    }
  }

}
