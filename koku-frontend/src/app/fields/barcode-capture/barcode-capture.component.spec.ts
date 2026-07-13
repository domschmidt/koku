import { TestBed } from '@angular/core/testing';

const scannerMocks = vi.hoisted(() => {
  const instances: any[] = [];
  class Scanner {
    render = vi.fn();
    clear = vi.fn(() => Promise.resolve());
    constructor() {
      instances.push(this);
    }
  }
  return { Scanner, instances };
});

vi.mock('html5-qrcode', () => ({
  Html5QrcodeScanner: scannerMocks.Scanner,
  Html5QrcodeScanType: { SCAN_TYPE_CAMERA: 0 },
}));

import { BarcodeCaptureComponent } from './barcode-capture.component';

describe('BarcodeCaptureComponent', () => {
  it('starts scanning and emits successful captures', () => {
    const fixture = TestBed.createComponent(BarcodeCaptureComponent);
    const captured = vi.fn();
    fixture.componentInstance.afterCapture.subscribe(captured);
    fixture.detectChanges();
    const scanner = scannerMocks.instances.at(-1);
    const [success, failure] = scanner.render.mock.calls[0];
    success('4711');
    failure('ignored');
    expect(captured).toHaveBeenCalledWith('4711');
    scanner.clear.mockRejectedValueOnce(new Error('already stopped'));
    fixture.destroy();
    expect(() => fixture.componentInstance.ngOnDestroy()).not.toThrow();
  });

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
