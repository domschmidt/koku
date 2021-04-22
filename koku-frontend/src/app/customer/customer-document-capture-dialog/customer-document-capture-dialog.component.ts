import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DocumentService} from "../../document/document.service";
import {NgForm} from "@angular/forms";
import {CustomerService} from "../customer.service";

export interface CustomerDocumentCaptureDialogData {
  documentId?: number;
  customerId?: number;
}

export interface CustomerDocumentCaptureDialogResponseData {
  document: KokuDto.FormularDto | undefined;
  upload: KokuDto.UploadDto | undefined;
}

@Component({
  selector: 'customer-document-capture-dialog',
  templateUrl: './customer-document-capture-dialog.component.html',
  styleUrls: ['./customer-document-capture-dialog.component.scss']
})
export class CustomerDocumentCaptureDialogComponent implements OnInit {
  private static GRID_SIZE: number = 12;
  saving: boolean = false;
  customerId: number | undefined;
  documentId: number | undefined;
  document: KokuDto.FormularDto | undefined;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerDocumentCaptureDialogData,
              public dialogRef: MatDialogRef<CustomerDocumentCaptureDialogComponent>,
              public documentService: DocumentService,
              public customerService: CustomerService) {
    this.customerId = data.customerId;
    this.documentId = data.documentId;
  }

  ngOnInit(): void {
    if (this.customerId && this.documentId) {
      this.customerService.getDocument(this.customerId, this.documentId).subscribe((document) => {
        this.document = document;
      });
    }
  }

  getFxFlex(size: number) {
    return Math.round((size / CustomerDocumentCaptureDialogComponent.GRID_SIZE) * 100) + '%';
  }

  getAlignStyle(align?: "LEFT" | "CENTER" | "RIGHT") {
    let result = 'left'
    switch (align) {
      case 'CENTER':
        result = 'center';
        break;
      case 'RIGHT':
        result = 'flex-end';
        break;
      case 'LEFT':
        result = 'flex-start';
        break;
      default:
        break;
    }
    return result;
  }

  save(document: KokuDto.FormularDto, form: NgForm) {
    if (this.customerId && this.documentId && form.valid) {
      this.saving = true;
      this.customerService.createCustomerDocument(this.customerId, document).subscribe((upload) => {
        const dialogResult: CustomerDocumentCaptureDialogResponseData = {
          document,
          upload
        };
        this.dialogRef.close(dialogResult);
      }, () => {
        this.saving = false;
      });
    }
  }

}
