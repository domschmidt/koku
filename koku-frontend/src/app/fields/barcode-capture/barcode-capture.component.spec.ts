import { TestBed } from '@angular/core/testing';
import { BarcodeCaptureComponent } from './barcode-capture.component';

describe('BarcodeCaptureComponent', () => {
  it('clears an initializing scanner without trying to pause it', () => {
    const fixture = TestBed.createComponent(BarcodeCaptureComponent);
    const scanner = {
      clear: vi.fn().mockName('Html5QrcodeScanner.clear'),
    };
    scanner.clear.mockResolvedValue(undefined);
    const componentState = fixture.componentInstance as unknown as {
      html5QrcodeInstance: unknown;
    };
    componentState.html5QrcodeInstance = scanner;

    expect(() => fixture.componentInstance.ngOnDestroy()).not.toThrow();
    expect(scanner.clear).toHaveBeenCalledTimes(1);
    expect(scanner.clear).toHaveBeenCalledWith();
  });
});
