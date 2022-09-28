import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "../../../document/document.service";

@Component({
  selector: 'document-text-field',
  templateUrl: './document-text-config-field.component.html',
  styleUrls: ['./document-text-config-field.component.scss']
})
export class DocumentTextConfigFieldComponent {

  textField: KokuDto.TextFormularItemDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  replacementTokens: KokuDto.FormularReplacementTokenDto[] = [];

  constructor(@Inject(MAT_DIALOG_DATA) public data: KokuDto.TextFormularItemDto,
              public dialogRef: MatDialogRef<DocumentTextConfigFieldComponent>,
              public dialog: MatDialog,
              public documentService: DocumentService) {
    this.createMode = data === null;
    if (this.createMode) {
      this.textField = {
        id: 0,
        ['@type']: 'TextFormularItemDto'
      };
    } else {
      this.textField = {...data};
    }
    this.documentService.getDocumentTextReplacementToken().subscribe((tokens) => {
      this.replacementTokens = tokens;
    });
  }

  save(form: NgForm) {
    if (form.valid) {
      this.dialogRef.close(this.textField);
    }
  }

  addReplacementToken(textArea: HTMLTextAreaElement, replacementToken: KokuDto.FormularReplacementTokenDto, formField: KokuDto.TextFormularItemDto) {
    const oldText = formField.text || '';
    formField.text = oldText.substring(0, textArea.selectionStart)
      + replacementToken.replacementToken
      + oldText.substring(textArea.selectionEnd, oldText.length);
  }
}
