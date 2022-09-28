import {AfterViewInit, Component, OnDestroy} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import {Html5Qrcode} from "html5-qrcode";
import {Html5QrcodeError} from "html5-qrcode/esm/core";

export interface QRCodeCaptureDialogData {
}

export interface QRCodeCaptureDialogResponse {
  decodedText: string;
}

@Component({
  selector: 'qr-code-capture-dialog',
  template: `
    <h1 mat-dialog-title class="dialog-header">
      QR Code Scannen
    </h1>
    <mat-dialog-content>
      <div id="scanner"></div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Abbrechen</button>
    </mat-dialog-actions>
  `
})
export class QRCodeCaptureDialogComponent implements AfterViewInit, OnDestroy {
  private scanner: Html5Qrcode | undefined;

  constructor(
    public dialogRef: MatDialogRef<QRCodeCaptureDialogComponent, QRCodeCaptureDialogResponse>
  ) {}

  ngAfterViewInit(): void {
    this.startCapture();
  }

  private startCapture() {
    this.scanner = new Html5Qrcode(
      "scanner"
    );
    this.scanner.start({
        facingMode: "environment"
      },
      {
        fps: 10
      },
      (decodedText: string) => {
        this.dialogRef.close({
          decodedText
        });
      }, (errorMessage: string, error: Html5QrcodeError) => {
        // ignore
      });
  }

  ngOnDestroy(): void {
    if (this.scanner && this.scanner.isScanning) {
      this.scanner.stop();
    }
  }


}
