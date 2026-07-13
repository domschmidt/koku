import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ToastService } from '../../toast/toast.service';

const compressorMock = vi.hoisted(() => {
  const instances: any[] = [];
  class Compressor {
    abort = vi.fn();
    constructor(
      readonly file: File,
      readonly options: any,
    ) {
      instances.push(this);
    }
  }
  return { Compressor, instances };
});

vi.mock('compressorjs', () => ({ default: compressorMock.Compressor }));

import { PictureUploadComponent } from './picture-upload.component';

class FileReaderFake {
  onload: ((event: any) => void) | null = null;
  readAsDataURL(blob: Blob) {
    this.onload?.({ target: { result: `data:${blob.type};base64,encoded` } });
  }
}

describe('PictureUploadComponent', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('compresses supported images, emits data URLs and aborts active work', async () => {
    vi.stubGlobal('FileReader', FileReaderFake);
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [PictureUploadComponent],
      providers: [{ provide: ToastService, useValue: toast }],
    })
      .overrideComponent(PictureUploadComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(PictureUploadComponent);
    fixture.componentRef.setInput('name', 'picture');
    fixture.componentRef.setInput('value', '');
    fixture.componentRef.setInput('compressMaxResolution', [640, 480]);
    fixture.detectChanges();
    const changed = vi.fn();
    fixture.componentInstance.changed.subscribe(changed);
    const file = new File(['image'], 'photo.png', { type: 'image/png' });
    fixture.componentInstance.onFileSelected({ target: { files: [file] } } as unknown as Event);
    const compressor = compressorMock.instances.at(-1);
    expect(compressor.options).toEqual(expect.objectContaining({ quality: 0.6, maxWidth: 640, maxHeight: 480 }));
    compressor.options.success(new Blob(['compressed'], { type: 'image/png' }));
    expect(changed).toHaveBeenCalledWith('data:image/png;base64,encoded');
    fixture.componentInstance.onFileSelected({ target: { files: [file] } } as unknown as Event);
    const active = compressorMock.instances.at(-1);
    fixture.componentInstance.ngOnDestroy();
    expect(active.abort).toHaveBeenCalled();
    fixture.componentInstance.clearImage();
    expect(changed).toHaveBeenCalledWith('');
  });

  it('rejects unsupported files and reports compression errors', async () => {
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [PictureUploadComponent],
      providers: [{ provide: ToastService, useValue: toast }],
    })
      .overrideComponent(PictureUploadComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(PictureUploadComponent);
    fixture.componentRef.setInput('name', 'picture');
    fixture.componentRef.setInput('value', '');
    fixture.detectChanges();
    fixture.componentInstance.onFileSelected({ target: { files: [] } } as unknown as Event);
    fixture.componentInstance.onFileSelected({
      target: { files: [new File(['text'], 'notes.txt', { type: 'text/plain' })] },
    } as unknown as Event);
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Bildformat'), 'error');
    fixture.componentInstance.onFileSelected({
      target: { files: [new File(['image'], 'photo.jpg', { type: 'image/jpeg' })] },
    } as unknown as Event);
    compressorMock.instances.at(-1).options.error();
    expect(toast.add).toHaveBeenCalledWith('Fehler bei der Bildbearbeitung', 'error');
  });
});
