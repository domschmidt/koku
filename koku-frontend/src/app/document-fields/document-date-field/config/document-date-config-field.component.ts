import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "../../../document/document.service";

@Component({
  selector: 'document-date-field',
  templateUrl: './document-date-config-field.component.html',
  styleUrls: ['./document-date-config-field.component.scss']
})
export class DocumentDateConfigFieldComponent {

  dateField: KokuDto.DateFormularItemDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  replacementTokens: KokuDto.FormularReplacementTokenDto[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: {
                field?: KokuDto.DateFormularItemDto
                document: KokuDto.FormularDto
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
    this.documentService.getDocumentDateReplacementToken(data.document.context.value).subscribe((tokens) => {
      this.replacementTokens = tokens;
    });
  }

  save(form: NgForm) {
    if (form.valid) {
      this.dialogRef.close(this.dateField);
    }
  }
}
