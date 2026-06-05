import { TestBed } from '@angular/core/testing';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { BarcodeCaptureComponent } from './barcode-capture.component';

describe('BarcodeCaptureComponent', () => {
  it('clears an initializing scanner without trying to pause it', () => {
    const fixture = TestBed.createComponent(BarcodeCaptureComponent);
    const scanner = jasmine.createSpyObj<Html5QrcodeScanner>('Html5QrcodeScanner', ['clear']);
    scanner.clear.and.resolveTo();
    const componentState = fixture.componentInstance as unknown as {
      html5QrcodeInstance: Html5QrcodeScanner;
    };
    componentState.html5QrcodeInstance = scanner;

    expect(() => fixture.componentInstance.ngOnDestroy()).not.toThrow();
    expect(scanner.clear).toHaveBeenCalledOnceWith();
  });
});
