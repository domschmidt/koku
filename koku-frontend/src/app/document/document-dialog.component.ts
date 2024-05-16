import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {DocumentService} from './document.service';
import {AlertDialogButtonConfig, AlertDialogComponent, AlertDialogData} from '../alert-dialog/alert-dialog.component';
import {
  DocumentContextSelectionDialogComponent,
  DocumentContextSelectionDialogComponentResponseData
} from './document-context-selection-dialog.component';
import {SnackBarService} from '../snackbar/snack-bar.service';
import {DocumentFieldConfigurationTypes} from '../document-designer-module/document-field-config';
import {
  DocumentCheckboxConfigFieldComponent
} from '../document-fields/document-checkbox-field/config/document-checkbox-config-field.component';
import {
  DocumentDateConfigFieldComponent
} from '../document-fields/document-date-field/config/document-date-config-field.component';
import {
  DocumentQrcodeConfigFieldComponent
} from '../document-fields/document-qrcode-field/config/document-qrcode-config-field.component';
import {
  DocumentSignatureConfigFieldComponent
} from '../document-fields/document-signature-field/config/document-signature-config-field.component';
import {
  DocumentSvgConfigFieldComponent
} from '../document-fields/document-svg-field/config/document-svg-config-field.component';
import {
  DocumentTextConfigFieldComponent
} from '../document-fields/document-text-field/config/document-text-config-field.component';
import {
  DocumentActivityPriceListConfigFieldComponent
} from '../document-fields/document-activity-price-list-field/config/document-activity-price-list-config-field.component';
import {forkJoin, Observable} from 'rxjs';
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
import {map} from 'rxjs/operators';

export interface DocumentDetailsComponentData {
  documentId?: number;
  documentDescription?: string;
}

export interface DocumentDetailsComponentResponseData {
  document?: KokuDto.FormularDto;
}

@Component({
  selector: 'document-dialog-component',
  templateUrl: './document-dialog.component.html',
  styleUrls: ['./document-dialog.component.scss']
})
export class DocumentDialogComponent {

  document: KokuDto.FormularDto | undefined;
  saving = false;
  loading = true;
  createMode: boolean;
  documentFieldConfig: DocumentFieldConfigurationTypes = {};
  private nextFieldId = 0;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: DocumentDetailsComponentData,
    public dialogRef: MatDialogRef<DocumentDialogComponent>,
    public dialog: MatDialog,
    public snackBarService: SnackBarService,
    public documentService: DocumentService
  ) {
    this.createMode = data.documentId === undefined;
    if (data.documentId) {
      this.documentService.getDocument(data.documentId).subscribe((document) => {
        this.generateFieldConfig(document.context.value).subscribe((config) => {
          this.documentFieldConfig = config;
          this.document = document;
          let nextFieldId = 0;
          for (const currentRow of document.rows || []) {
            for (const currentItem of currentRow.items || []) {
              if (currentItem.id > nextFieldId) {
                nextFieldId = currentItem.id;
              }
            }
          }
          this.nextFieldId = nextFieldId;
          this.loading = false;
        });
      });
    } else {
      const contextSelectionDialogRef = this.dialog.open<
        DocumentContextSelectionDialogComponent, void, DocumentContextSelectionDialogComponentResponseData
      >(DocumentContextSelectionDialogComponent, {});
      contextSelectionDialogRef.afterClosed().subscribe((result) => {
        if (result && result.context) {
          this.generateFieldConfig(result.context.value).subscribe((config) => {
            this.documentFieldConfig = config;
            this.document = {
              description: this.data.documentDescription || '',
              context: result.context,
              rows: []
            };

            this.loading = false;
          });
        } else {
          this.snackBarService.openErrorSnack('Die Dokumentenerstellung wurde abgebrochen.');
          this.dialogRef.close();
        }
      });
    }
  }

  save(document: KokuDto.FormularDto, form: NgForm): void {
    if (form.valid) {
      this.saving = true;
      if (!document.id) {
        this.documentService.createDocument(document).subscribe((result) => {
          const dialogResult: DocumentDetailsComponentResponseData = {
            document: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.documentService.updateDocument(document).subscribe(() => {
          const dialogResult: DocumentDetailsComponentResponseData = {
            document
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(user: KokuDto.FormularDto): void {
    const dialogData: AlertDialogData = {
      headline: 'Feld Löschen',
      message: `Wollen Sie das Dokument wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          this.saving = true;
          this.documentService.deleteDocument(user).subscribe(() => {
            this.dialogRef.close();
            this.saving = false;
            dialogRef.close();
          });
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

  duplicate(originalDocument: KokuDto.FormularDto): void {
    this.saving = true;
    this.documentService.duplicateDocument(originalDocument).subscribe((result) => {
      const dialogResult: DocumentDetailsComponentResponseData = {
        document: result
      };
      this.dialogRef.close(dialogResult);
      this.saving = false;
      this.snackBarService.openCommonSnack(`Dokument wurde kopiert. Neuer Name: ${result.description}`);
    }, () => {
      this.saving = false;
    });
  }

  private generateFieldConfig(context: KokuDto.DocumentContextEnumDto): Observable<DocumentFieldConfigurationTypes> {
    return forkJoin(
      [
        this.documentService.getDocumentTextReplacementToken(context),
        this.documentService.getDocumentActivityPriceListItemTextReplacementToken(context),
        this.documentService.getDocumentQrcodeReplacementToken(context),
        this.documentService.getDocumentDateReplacementToken(context),
        this.documentService.getDocumentCheckboxReplacementToken(context)
      ]
    ).pipe(map(([
                  textReplacementToken,
                  textActivityPriceListItemReplacementToken,
                  qrReplacementToken,
                  dateReplacementToken,
                  checkboxReplacementToken
                ]) => {
      return {
        CheckboxFormularItemDto: {
          component: DocumentCheckboxConfigFieldComponent,
          meta: {
            replacementTokens: checkboxReplacementToken
          },
          renderComponent: DocumentCheckboxFieldComponent,
          name: 'Checkbox'
        },
        DateFormularItemDto: {
          component: DocumentDateConfigFieldComponent,
          meta: {
            replacementTokens: dateReplacementToken
          },
          renderComponent: DocumentDateFieldComponent,
          name: 'Datum'
        },
        QrCodeFormularItemDto: {
          component: DocumentQrcodeConfigFieldComponent,
          meta: {
            replacementTokens: qrReplacementToken
          },
          renderComponent: DocumentQrcodeFieldComponent,
          name: 'QRCode'
        },
        SignatureFormularItemDto: {
          component: DocumentSignatureConfigFieldComponent,
          meta: {},
          renderComponent: DocumentSignatureFieldComponent,
          name: 'Unterschrift'
        },
        SVGFormularItemDto: {
          component: DocumentSvgConfigFieldComponent,
          meta: {},
          renderComponent: DocumentSvgFieldComponent,
          name: 'Vektorgrafik'
        },
        TextFormularItemDto: {
          component: DocumentTextConfigFieldComponent,
          meta: {
            replacementTokens: textReplacementToken
          },
          renderComponent: DocumentTextFieldComponent,
          name: 'Text'
        },
        ActivityPriceListFormularItemDto: {
          component: DocumentActivityPriceListConfigFieldComponent,
          meta: {
            fieldConfig: {
              TextFormularItemDto: {
                component: DocumentTextConfigFieldComponent,
                meta: {
                  replacementTokens: textActivityPriceListItemReplacementToken
                },
                renderComponent: DocumentTextFieldComponent,
                name: 'Text'
              }
            }
          },
          renderComponent: DocumentActivityPriceListFieldComponent,
          name: 'Aktivitätspreisliste'
        }
      };
    }));
  }
}
