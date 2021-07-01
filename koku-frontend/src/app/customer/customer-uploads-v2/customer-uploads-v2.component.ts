import {Component, Input, OnInit} from '@angular/core';
import {CustomerService} from "../customer.service";
import {FileSystemFileEntry, NgxFileDropEntry} from "ngx-file-drop";
import {UploadWithProgress} from "../customer-uploads/extended-upload.interface";
import {DocumentService} from "../../document/document.service";
import {
  CustomerDocumentCaptureDialogComponent,
  CustomerDocumentCaptureDialogData,
  CustomerDocumentCaptureDialogResponseData
} from "../customer-document-capture-dialog/customer-document-capture-dialog.component";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";
import {HttpClient} from "@angular/common/http";
import * as FileSaver from "file-saver";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'customer-uploads',
  templateUrl: './customer-uploads-v2.component.html',
  styleUrls: ['./customer-uploads-v2.component.scss']
})
export class CustomerUploadsV2Component implements OnInit {
  customerId: number | undefined;
  uploads: UploadWithProgress[] | undefined;
  possibleFormulars: KokuDto.FormularDto[] | undefined;

  constructor(public customerService: CustomerService,
              public documentService: DocumentService,
              public httpClient: HttpClient,
              private readonly route: ActivatedRoute,
              public dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.documentService.getDocuments().subscribe((formulars: KokuDto.FormularDto[]) => {
      this.possibleFormulars = formulars;
    });
    this.route.paramMap.subscribe((params) => {
      this.customerId = Number(params.get('customerId'));
      if (this.customerId) {
        this.customerService.getCustomerUploads(this.customerId).subscribe((value) => {
          this.uploads = value;
        });
      }
    });
  }

  trackByFn(index: number, item: KokuDto.UploadDto) {
    return item.uuid;
  }

  dropped(droppedFiles: NgxFileDropEntry[]) {
    if (this.customerId) {
      for (const currentUpload of droppedFiles) {
        if (!this.uploads) {
          this.uploads = [];
        }
        this.uploads.push(this.customerService.addCustomerUpload(this.customerId, <FileSystemFileEntry>currentUpload.fileEntry));
      }
    }
  }

  delete(item: KokuDto.UploadDto) {
    const dialogData: AlertDialogData = {
      headline: 'Anhang Löschen',
      message: `Wollen Sie den Anhang mit dem Namen ${item.fileName} wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          if (this.customerId) {
            this.customerService.deleteCustomerUpload(this.customerId, item).subscribe(() => {
              if (!this.uploads) {
                this.uploads = [];
              }
              this.uploads.splice(this.uploads.indexOf(item), 1);
              dialogRef.close();
            }, () => {
              button.loading = false;
            });
          }
        }
      }]
    };
    this.dialog.open(AlertDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  download(upload: KokuDto.UploadDto) {
    this.httpClient.get('/api/customers/' + this.customerId + '/uploads/' + upload.uuid, {responseType: 'blob'}).subscribe((result) => {
      FileSaver.saveAs(result, upload.fileName);
    });
  }

  openFormularCapture(possibleFormular: KokuDto.FormularDto) {
    const dialogData: CustomerDocumentCaptureDialogData = {
      customerId: this.customerId || 0,
      documentId: possibleFormular.id || 0
    };
    const documentCaptureDialog = this.dialog.open(CustomerDocumentCaptureDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: '90%',
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
    documentCaptureDialog.afterClosed().subscribe((dialogResult: CustomerDocumentCaptureDialogResponseData) => {
      if (dialogResult && dialogResult.upload) {
        if (!this.uploads) {
          this.uploads = [];
        }
        this.uploads.push(dialogResult.upload);
      }
    });
  }
}