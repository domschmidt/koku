import {AfterViewInit, Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from './document.service';
import {PreventLosingChangesService} from '../prevent-losing-changes/prevent-losing-changes.service';
import {DocumentRenderFieldTypes} from '../document-designer-module/document-field-config';
import {
  DocumentCheckboxFieldComponent
} from '../document-fields/document-checkbox-field/document-checkbox-field.component';
import {DocumentDateFieldComponent} from '../document-fields/document-date-field/document-date-field.component';
import {DocumentQrcodeFieldComponent} from '../document-fields/document-qrcode-field/document-qrcode-field.component';
import {
  DocumentSignatureFieldComponent
} from '../document-fields/document-signature-field/document-signature-field.component';
import {DocumentSvgFieldComponent} from '../document-fields/document-svg-field/document-svg-field.component';
import {DocumentTextFieldComponent} from '../document-fields/document-text-field/document-text-field.component';
import {
  DocumentActivityPriceListFieldComponent
} from '../document-fields/document-activity-price-list-field/document-activity-price-list-field.component';

export interface DocumentCaptureDialogComponentData {
  documentId?: number;
}

export interface DocumentCaptureDialogComponentResponseData {
  document: KokuDto.FormularDto | undefined;
  upload: KokuDto.UploadDto | undefined;
}

@Component({
  selector: 'customer-document-capture-dialog',
  templateUrl: './document-capture-dialog.component.html'
})
export class DocumentCaptureDialogComponent implements OnInit, AfterViewInit {

  saving = false;
  documentId: number | undefined;
  document: KokuDto.FormularDto | undefined;
  documentFieldConfig: DocumentRenderFieldTypes = {
    CheckboxFormularItemDto: {
      component: DocumentCheckboxFieldComponent
    },
    DateFormularItemDto: {
      component: DocumentDateFieldComponent
    },
    QrCodeFormularItemDto: {
      component: DocumentQrcodeFieldComponent
    },
    SignatureFormularItemDto: {
      component: DocumentSignatureFieldComponent,
    },
    SVGFormularItemDto: {
      component: DocumentSvgFieldComponent
    },
    TextFormularItemDto: {
      component: DocumentTextFieldComponent
    },
    ActivityPriceListFormularItemDto: {
      component: DocumentActivityPriceListFieldComponent,
      meta: {
        fieldConfig: {
          TextFormularItemDto: {
            component: DocumentTextFieldComponent
          }
        }
      },
    }
  };
  private dirty = false;
  @ViewChild('form') ngForm: NgForm | undefined;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: DocumentCaptureDialogComponentData,
    public dialogRef: MatDialogRef<DocumentCaptureDialogComponent, DocumentCaptureDialogComponentResponseData>,
    private readonly preventLosingChangesService: PreventLosingChangesService,
    public documentService: DocumentService
  ) {
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
    if (this.documentId) {
      this.documentService.getDocumentCapture(this.documentId).subscribe((document) => {
        this.document = document;
      });
    }
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

  save(document: KokuDto.FormularDto | undefined, form: NgForm): void {
    if (document && this.documentId && form.valid) {
      this.saving = true;
      this.documentService.saveCapturedDocument(document).subscribe((upload) => {
        this.dialogRef.close({
          document,
          upload
        });
      }, () => {
        this.saving = false;
      });
    }
  }

}
