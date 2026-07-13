import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { FileViewerComponent } from './file-viewer.component';

describe('FileViewerComponent', () => {
  it('loads metadata and blobs, updates PDF data and revokes object URLs', async () => {
    const createObjectURL = vi
      .spyOn(URL, 'createObjectURL')
      .mockReturnValueOnce('blob:first')
      .mockReturnValueOnce('blob:second');
    const revokeObjectURL = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => undefined);
    const http = {
      get: vi.fn((url: string) =>
        url.startsWith('/files')
          ? of(new Blob(['pdf'], { type: 'application/pdf' }))
          : of({ file: { mime: 'application/pdf' } }),
      ),
    };
    await TestBed.configureTestingModule({
      imports: [FileViewerComponent],
      providers: [{ provide: HttpClient, useValue: http }],
    }).compileComponents();
    const fixture = TestBed.createComponent(FileViewerComponent);
    fixture.componentRef.setInput('sourceUrl', '/documents/1');
    fixture.componentRef.setInput('fileUrl', '/files/1');
    fixture.componentRef.setInput('mimeTypeSourcePath', 'file.mime');
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.source()).toEqual({ file: { mime: 'application/pdf' } });
    expect(component.mimeType()).toBe('application/pdf');
    expect(component.fileBlobUrl()).toBe('blob:first#toolbar=0&view=FitH');
    expect(createObjectURL).toHaveBeenCalled();
    fixture.detectChanges();
    const object = fixture.nativeElement.querySelector('object');
    expect(object.getAttribute('data')).toBe('blob:first#toolbar=0&view=FitH');
    fixture.componentRef.setInput('fileUrl', '/files/2');
    fixture.detectChanges();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:first');
    expect(component.fileBlobUrl()).toBe('blob:second#toolbar=0&view=FitH');
    (component as any).clearBlobUrl();
    fixture.detectChanges();
    expect(object.getAttribute('data')).toBeNull();
    fixture.destroy();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:second');
    vi.restoreAllMocks();
  });
});
