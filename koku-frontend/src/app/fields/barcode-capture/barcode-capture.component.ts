import {AfterViewInit, Component, OnDestroy, output} from '@angular/core';
import {Html5QrcodeScanner, Html5QrcodeScanType} from 'html5-qrcode';

@Component({
  selector: 'barcode-capture',
  templateUrl: './barcode-capture.component.html',
  styleUrl: './barcode-capture.component.css',
  imports: [],
  standalone: true
})
export class BarcodeCaptureComponent implements AfterViewInit, OnDestroy {

  private html5QrcodeInstance: Html5QrcodeScanner | undefined;

  afterCapture = output<string>();

  ngAfterViewInit(): void {
    this.startCapture();
  }

  private startCapture() {
    this.html5QrcodeInstance = new Html5QrcodeScanner(
      "html5qrcode", {
        fps: 10,
        showTorchButtonIfSupported: true,
        rememberLastUsedCamera: true,
        // Only support camera scan type.
        supportedScanTypes: [Html5QrcodeScanType.SCAN_TYPE_CAMERA]
      }, false);
    this.html5QrcodeInstance.render((capturedValue) => {
      this.afterCapture.emit(capturedValue);
    }, () => {});
  }

  ngOnDestroy(): void {
    this.html5QrcodeInstance?.pause(true);
    this.html5QrcodeInstance?.clear();
  }

}
