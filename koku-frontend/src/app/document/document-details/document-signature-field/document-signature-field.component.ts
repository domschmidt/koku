import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {DocumentService} from "../../document.service";

@Component({
  selector: 'document-signature-field',
  templateUrl: './document-signature-field.component.html',
  styleUrls: ['./document-signature-field.component.scss']
})
export class DocumentSignatureFieldComponent {

  signatureField: KokuDto.SignatureFormularItemDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;

  constructor(@Inject(MAT_DIALOG_DATA) public data: KokuDto.SignatureFormularItemDto,
              public dialogRef: MatDialogRef<DocumentSignatureFieldComponent>,
              public dialog: MatDialog,
              public documentService: DocumentService) {
    this.createMode = data === null;
    if (this.createMode) {
      this.signatureField = {
        ['@type']: 'SignatureFormularItemDto'
      };
    } else {
      this.signatureField = {...data};
    }
  }

  save(form: NgForm) {
    if (form.valid) {
      this.dialogRef.close(this.signatureField);
    }
  }
}
