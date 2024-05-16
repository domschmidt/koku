import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from '../../../document/document.service';
import {DocumentFieldMeta} from '../../../document-designer-module/document-field-config';

@Component({
  selector: 'document-qr-field',
  templateUrl: './document-qrcode-config-field.component.html',
  styleUrls: ['./document-qrcode-config-field.component.scss']
})
export class DocumentQrcodeConfigFieldComponent {

  qrCodeField: KokuDto.QrCodeFormularItemDto | undefined;
  saving = false;
  loading = true;
  createMode: boolean;
  replacementTokens: KokuDto.FormularReplacementTokenDto[] = [];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {
      field?: KokuDto.QrCodeFormularItemDto
      meta: DocumentFieldMeta
    },
    public dialogRef: MatDialogRef<DocumentQrcodeConfigFieldComponent>,
    public dialog: MatDialog,
    public documentService: DocumentService
  ) {
    this.createMode = data.field === undefined;
    if (data.field === undefined) {
      this.qrCodeField = {
        id: 0,
        ['@type']: 'QrCodeFormularItemDto'
      };
    } else {
      this.qrCodeField = {...data.field};
    }
    this.replacementTokens = data.meta.replacementTokens;
  }

  save(form: NgForm): void {
    if (form.valid) {
      this.dialogRef.close(this.qrCodeField);
    }
  }

  addReplacementToken(
    textArea: HTMLTextAreaElement,
    replacementToken: KokuDto.FormularReplacementTokenDto,
    formField: KokuDto.QrCodeFormularItemDto
  ): void {
    const oldText = formField.value || '';
    formField.value = oldText.substring(0, textArea.selectionStart)
      + replacementToken.replacementToken
      + oldText.substring(textArea.selectionEnd, oldText.length);
  }
}
