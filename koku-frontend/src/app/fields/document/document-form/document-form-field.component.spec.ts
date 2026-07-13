import { HttpClient } from '@angular/common/http';
import { SimpleChange } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { FullscreenService } from '../../../fullscreen/fullscreen.service';
import { ToastService } from '../../../toast/toast.service';

const pdfMocks = vi.hoisted(() => {
  const instances: any[] = [];
  const generate = vi.fn(() => Promise.resolve({ buffer: new ArrayBuffer(4) }));
  class Form {
    template: any;
    inputs: any[];
    domContainer: HTMLElement;
    resizeObserver = { disconnect: vi.fn(), observe: vi.fn() };
    updateTemplate = vi.fn((template: any) => (this.template = template));
    setInputs = vi.fn((inputs: any[]) => (this.inputs = inputs));
    getTemplate = vi.fn(() => this.template);
    getInputs = vi.fn(() => this.inputs);
    destroy = vi.fn();
    render = vi.fn();
    constructor(options: any) {
      this.template = options.template;
      this.inputs = options.inputs;
      this.domContainer = options.domContainer;
      instances.push(this);
    }
  }
  return { Form, instances, generate };
});

vi.mock('@pdfme/ui', () => ({ Form: pdfMocks.Form }));
vi.mock('@pdfme/generator', () => ({ generate: pdfMocks.generate }));

import { DocumentFormFieldComponent } from './document-form-field.component';

class ResizeObserverFake {
  disconnect = vi.fn();
  observe = vi.fn();
  unobserve = vi.fn();
  constructor(readonly callback: ResizeObserverCallback) {}
}

vi.stubGlobal('ResizeObserver', ResizeObserverFake);
Object.defineProperty(screen, 'orientation', {
  configurable: true,
  value: Object.assign(new EventTarget(), { type: 'portrait-primary' }),
});

const template = {
  basePdf: { width: 100, height: 200 },
  schemas: [
    [
      { type: 'text', name: 'customer', content: 'Hello { customer.name }' },
      { type: 'qrcode', name: 'qr', content: '{document.id}' },
      { type: 'date', name: '{utils.today}' },
      { type: 'multiVariableText', name: 'variables', variables: ['customer.name', 'missing'] },
    ],
  ],
};

describe('DocumentFormFieldComponent', () => {
  it('builds and updates PDF forms from document templates and context', async () => {
    pdfMocks.generate.mockClear();
    const http = {
      get: vi.fn(() => of({ name: 'Contract', template: JSON.stringify(template) })),
      request: vi.fn(() => of({ id: 'file-1' })),
    };
    const fullscreen = { enter: vi.fn(), exit: vi.fn() };
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [DocumentFormFieldComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: ToastService, useValue: toast },
        { provide: FullscreenService, useValue: fullscreen },
      ],
    })
      .overrideComponent(DocumentFormFieldComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DocumentFormFieldComponent);
    fixture.componentRef.setInput('documentUrl', '/documents/1');
    fixture.componentRef.setInput('submitUrl', '/customers/1/documents');
    fixture.componentRef.setInput('submitMethod', 'PUT');
    fixture.componentRef.setInput('context', { customer: { name: 'Ada' } });
    const root = document.createElement('div');
    Object.defineProperty(root, 'getBoundingClientRect', {
      value: () => ({ width: 400, height: 300 }),
    });
    (fixture.componentInstance.formRoot as any) = () => ({ nativeElement: root });
    fixture.detectChanges();
    const component = fixture.componentInstance;
    const form = pdfMocks.instances.at(-1);
    expect(form.inputs[0].customer).toBe('Hello Ada');
    expect(form.inputs[0].qr).toBeTruthy();
    expect(form.inputs[0].variables).toContain('Ada');
    expect(form.resizeObserver.observe).toHaveBeenCalledWith(root);
    vi.useFakeTimers();
    form.resizeObserver.callback([], form.resizeObserver);
    vi.advanceTimersByTime(50);
    expect(form.render).toHaveBeenCalled();
    expect(form.size).toEqual(expect.objectContaining({ width: 400, height: expect.any(Number) }));
    vi.useRealTimers();

    component.ngOnChanges({ documentUrl: new SimpleChange('/documents/1', '/documents/2', false) });
    expect(form.updateTemplate).toHaveBeenCalled();
    expect(form.setInputs).toHaveBeenCalled();

    const submitted = vi.fn();
    component.submitted.subscribe(submitted);
    component.submit();
    await Promise.resolve();
    await Promise.resolve();
    expect(http.request).toHaveBeenCalledWith('PUT', expect.stringMatching(/^\/customers\/1\/documents\?id=/), {
      body: expect.any(FormData),
    });
    expect(submitted).toHaveBeenCalledWith({ id: 'file-1' });
    expect(toast.add).toHaveBeenCalledWith('Dokument erstellt');

    component.enterFullscreen();
    component.exitFullscreen();
    (screen.orientation as any).type = 'landscape-primary';
    screen.orientation.dispatchEvent(new Event('change'));
    (screen.orientation as any).type = 'portrait-primary';
    screen.orientation.dispatchEvent(new Event('change'));
    expect(fullscreen.enter).toHaveBeenCalledWith(root);
    fixture.destroy();
    expect(form.destroy).toHaveBeenCalled();
    expect(fullscreen.exit).toHaveBeenCalled();
  });

  it('blocks missing required values and reports generation and upload errors', async () => {
    pdfMocks.generate.mockClear();
    const http = {
      get: vi.fn(() => of({ name: 'Required', template: JSON.stringify(template) })),
      request: vi.fn(() => throwError(() => new Error('upload failed'))),
    };
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [DocumentFormFieldComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: ToastService, useValue: toast },
        { provide: FullscreenService, useValue: { enter: vi.fn(), exit: vi.fn() } },
      ],
    })
      .overrideComponent(DocumentFormFieldComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DocumentFormFieldComponent);
    fixture.componentRef.setInput('documentUrl', '/documents/required');
    fixture.componentRef.setInput('submitUrl', '/upload?type=pdf');
    const component = fixture.componentInstance;
    (component.formRoot as any) = () => ({ nativeElement: document.createElement('div') });
    fixture.detectChanges();
    const form = pdfMocks.instances.at(-1);
    form.template.schemas[0][0].required = true;
    form.inputs = [{}];
    component.submit();
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Eingaben'), 'warning');
    expect(pdfMocks.generate).not.toHaveBeenCalled();

    form.inputs = [{ customer: 'present' }];
    component.submit();
    await Promise.resolve();
    await Promise.resolve();
    expect(http.request).toHaveBeenCalledWith('POST', expect.stringContaining('&id='), expect.any(Object));
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Speichern', 'error');

    pdfMocks.generate.mockRejectedValueOnce(new Error('pdf failed'));
    component.submit();
    await Promise.resolve();
    await Promise.resolve();
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Erstellen der PDF', 'error');
    expect(component.submitting()).toBe(false);

    pdfMocks.generate.mockResolvedValueOnce({ buffer: new ArrayBuffer(4) });
    (component as any).document = { template: JSON.stringify(template) };
    component.submit();
    await Promise.resolve();
    await Promise.resolve();
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Erstellen der PDF', 'error');
  });

  it('rejects documents without templates', async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentFormFieldComponent],
      providers: [
        { provide: HttpClient, useValue: { get: vi.fn(() => of({ name: 'Broken' })) } },
        { provide: ToastService, useValue: { add: vi.fn() } },
        { provide: FullscreenService, useValue: { exit: vi.fn() } },
      ],
    })
      .overrideComponent(DocumentFormFieldComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DocumentFormFieldComponent);
    const component = fixture.componentInstance as any;
    component.document = { name: 'Broken' };
    expect(() => component.initOrUpdateForm()).toThrow('Missing template in document');
  });
});
