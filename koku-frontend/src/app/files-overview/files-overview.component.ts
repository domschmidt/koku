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
import {
  FileUploadDialogComponent,
  FileUploadDialogComponentData,
  FileUploadDialogComponentResponseData
} from "./file-upload-dialog.component";
import {FileService} from "./file.service";

@Component({
  selector: 'files-overview',
  templateUrl: './files-overview.component.html',
  styleUrls: ['./files-overview.component.scss']
})
export class FilesOverviewComponent {
  reloadTableSubject: Subject<DataTableDto.DataQuerySpecDto | void> = new Subject<DataTableDto.DataQuerySpecDto | void>();
  prepareDeleteForElements: { [key: string]: boolean } = {};

  constructor(
    private readonly dialog: MatDialog,
    private readonly httpClient: HttpClient,
    private readonly fileService: FileService,
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

  createFile() {
    const createFileDialogRef = this.dialog.open<FileUploadDialogComponent, FileUploadDialogComponentData, FileUploadDialogComponentResponseData>(FileUploadDialogComponent, {
      data: {
        dynamicDocumentContext: 'NONE'
      },
      closeOnNavigation: false,
      minHeight: '500px',
      position: {
        top: '20px'
      }
    });
    createFileDialogRef.afterClosed().subscribe(() => {
      this.reloadTableSubject.next();
    });
  }

  openDocument(element: { [key: string]: any }) {
    this.fileService.downloadFile(element.uuid).subscribe((result) => {
      if (result.size > 0) {
        FileSaver.saveAs(result, element.fileName);
      } else {
        this.snackBarService.openErrorSnack('Das Dokument konnte nicht abgerufen werden.')
      }
    });
  }

  deleteDocument(element: { [key: string]: any }) {
    this.prepareDeleteForElements[element.uuid] = true;
    const deleteDialogRef = this.dialog.open<AlertDialogComponent, AlertDialogData>(AlertDialogComponent, {
      data: {
        headline: 'Datei Löschen',
        message: `Wollen Sie die Datei '${element.fileName}' wirklich löschen?`,
        buttons: [{
          text: 'Abbrechen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            dialogRef.close();
          }
        }, {
          text: 'Bestätigen',
          onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
            button.loading = true;
            this.fileService.deleteFile(element.uuid).subscribe(() => {
              this.reloadTableSubject.next();
              this.snackBarService.openCommonSnack(`Die Datei '${element.fileName}' wurde erfolgreich gelöscht`);
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
