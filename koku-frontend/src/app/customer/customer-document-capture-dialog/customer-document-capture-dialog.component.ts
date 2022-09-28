import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {CustomerService} from "../customer.service";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";

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

  saving: boolean = false;
  customerId: number | undefined;
  documentId: number | undefined;
  document: KokuDto.FormularDto | undefined;
  private dirty: boolean = false;
  @ViewChild('form') ngForm: NgForm | undefined;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerDocumentCaptureDialogData,
              public dialogRef: MatDialogRef<CustomerDocumentCaptureDialogComponent>,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public customerService: CustomerService) {
    this.customerId = data.customerId;
    this.documentId = data.documentId;

    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => {
      this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
        this.dialogRef.close();
      });
    });
    this.dialogRef.keydownEvents().subscribe((event) => {
      if (event.key === 'Escape') {
        this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
          this.dialogRef.close();
        });
      }
    });
  }

  ngOnInit(): void {
    if (this.customerId && this.documentId) {
      this.customerService.getDocument(this.customerId, this.documentId).subscribe((document) => {
        this.document = document;
      });
    }
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

  save(document: KokuDto.FormularDto | undefined, form: NgForm) {
    if (document && this.customerId && this.documentId && form.valid) {
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
