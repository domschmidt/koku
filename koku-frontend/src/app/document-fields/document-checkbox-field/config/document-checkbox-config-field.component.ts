import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "../../../document/document.service";

@Component({
  selector: 'document-checkbox-field',
  templateUrl: './document-checkbox-config-field.component.html',
  styleUrls: ['./document-checkbox-config-field.component.scss']
})
export class DocumentCheckboxConfigFieldComponent {

  checkboxField: KokuDto.CheckboxFormularItemDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  replacementTokens: KokuDto.FormularReplacementTokenDto[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: KokuDto.CheckboxFormularItemDto,
              public dialogRef: MatDialogRef<DocumentCheckboxConfigFieldComponent>,
              public dialog: MatDialog,
              public documentService: DocumentService) {
    this.createMode = data === null;
    if (this.createMode) {
      this.checkboxField = {
        id: 0,
        ['@type']: 'CheckboxFormularItemDto'
      };
    } else {
      this.checkboxField = {...data};
    }
    this.documentService.getDocumentCheckboxReplacementToken().subscribe((tokens) => {
      this.replacementTokens = tokens;
    });
  }

  save(form: NgForm) {
    if (form.valid) {
      this.dialogRef.close(this.checkboxField);
    }
  }

}
