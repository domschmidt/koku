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

  constructor(@Inject(MAT_DIALOG_DATA) public data: {
                field?: KokuDto.CheckboxFormularItemDto
                document: KokuDto.FormularDto
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
    this.documentService.getDocumentCheckboxReplacementToken(data.document.context.value).subscribe((tokens) => {
      this.replacementTokens = tokens;
    });
  }

  save(form: NgForm) {
    if (form.valid) {
      this.dialogRef.close(this.checkboxField);
    }
  }

}
