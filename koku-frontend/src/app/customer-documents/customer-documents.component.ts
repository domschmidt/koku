import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {
  QRCodeCaptureDialogComponent,
  QRCodeCaptureDialogData,
  QRCodeCaptureDialogResponse
} from "../qr-code-capture/qr-code-capture-dialog.component";
import * as FileSaver from "file-saver";
import {HttpClient} from "@angular/common/http";
import {SnackBarService} from "../snackbar/snack-bar.service";
import {AlertDialogButtonConfig, AlertDialogComponent, AlertDialogData} from "../alert-dialog/alert-dialog.component";
import {Subject} from "rxjs";

@Component({
  selector: 'table',
  templateUrl: './customer-documents.component.html',
  styleUrls: ['./customer-documents.component.scss']
})
export class CustomerDocumentsComponent {
  reloadTableSubject: Subject<DataTableDto.DataQuerySpecDto | void> = new Subject<DataTableDto.DataQuerySpecDto | void>();
  prepareDeleteForElements: { [key: string]: boolean } = {};

  constructor(
    private readonly dialog: MatDialog,
    private readonly httpClient: HttpClient,
    private readonly snackBarService: SnackBarService
  ) {
  }

  captureQRCodeAndTriggerSearch() {
    const qrCodeDialogRef = this.dialog.open<QRCodeCaptureDialogComponent, QRCodeCaptureDialogData, QRCodeCaptureDialogResponse>(
      QRCodeCaptureDialogComponent,
      {
        maxHeight: '100vh',
        maxWidth: '100vw',
        height: '100vh',
        width: '100vw'
      }
    );
    qrCodeDialogRef.afterClosed().subscribe((result) => {
      if (result && result.decodedText) {
        this.reloadTableSubject.next({
          globalSearch: result.decodedText
        });
      }
    });
  }

  openDocument(element: { [key: string]: any }) {
    this.httpClient.get(`/api/customers/${element['customer.id']}/uploads/${element.uuid}`, {responseType: 'blob'}).subscribe((result) => {
      if (result.size > 0) {
        FileSaver.saveAs(result, element.fileName);
      } else {
        this.snackBarService.openErrorSnack('Das Dokument konnte nicht abgerufen werden.')
      }
    });
  }

  deleteDocument(element: any) {
    this.prepareDeleteForElements[element.uuid] = true;
    const deleteDialogRef = this.dialog.open<AlertDialogComponent, AlertDialogData>(AlertDialogComponent, {
      data: {
        headline: 'Dokument Löschen',
        message: `Wollen Sie das Dokument '${element.fileName}' wirklich löschen?`,
        buttons: [{
          text: 'Abbrechen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            dialogRef.close();
          }
        }, {
          text: 'Bestätigen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            button.loading = true;
            this.httpClient.delete(`/api/customers/${element['customer.id']}/uploads/${element.uuid}`).subscribe(() => {
              this.reloadTableSubject.next();
              this.snackBarService.openCommonSnack(`Das Dokument '${element.fileName}' wurde erfolgreich gelöscht`);
              dialogRef.close();
            }, (error) => {
              this.snackBarService.openErrorSnack(error.error.message);
              button.loading = false;
            });
          }
        }]
      },
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
    deleteDialogRef.afterClosed().subscribe(() => {
      delete this.prepareDeleteForElements[element.uuid];
    });
  }
}
