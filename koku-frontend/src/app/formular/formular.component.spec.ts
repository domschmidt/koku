import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { SimpleChange } from '@angular/core';
import { firstValueFrom, of, Subject, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ToastService } from '../toast/toast.service';
import { FORMULAR_CONTENT_REGISTRY } from '../formular-binding/registry';
import { FORMULAR_PLUGIN, FormularComponent, FormularPlugin } from './formular.component';

const EMPTY_FORM = {
  alias: 'customer editor',
  rootId: 'root',
  contents: { root: { id: 'root', '@type': 'grid' } },
  placements: [],
} as unknown as KokuDto.FormViewDto;

async function createComponent(
  http: Partial<HttpClient>,
  plugins?: ((component: FormularComponent) => FormularPlugin) | ((component: FormularComponent) => FormularPlugin)[],
) {
  const toast = { add: vi.fn() };
  await TestBed.configureTestingModule({
    imports: [FormularComponent],
    providers: [
      { provide: HttpClient, useValue: http },
      { provide: ToastService, useValue: toast },
      ...(plugins ? [{ provide: FORMULAR_PLUGIN, useValue: plugins }] : []),
    ],
  })
    .overrideComponent(FormularComponent, { set: { template: '' } })
    .compileComponents();
  const fixture = TestBed.createComponent(FormularComponent);
  fixture.componentRef.setInput('formularUrl', '/forms/customer');
  fixture.componentRef.setInput('contentRegistry', FORMULAR_CONTENT_REGISTRY);
  return { fixture, component: fixture.componentInstance, toast };
}

describe('FormularComponent', () => {
  it('loads its definition and source, applies overrides and submits merged data', async () => {
    const http = {
      get: vi.fn((url: string) => of(url === '/forms/customer' ? EMPTY_FORM : { id: 42, name: 'Before' })),
      request: vi.fn(() => of({ id: 42, name: 'After', saved: true })),
    };
    const plugin: FormularPlugin = {
      onFormularLoaded: vi.fn(),
      onSourceLoaded: vi.fn(),
      destroy: vi.fn(),
    };
    const pluginFactory = vi.fn(() => plugin);
    const { fixture, component } = await createComponent(http as any, [pluginFactory]);
    fixture.componentRef.setInput('sourceUrl', '/customers/42');
    fixture.componentRef.setInput('submitMethod', 'PUT');
    fixture.componentRef.setInput('sourceOverrides', [
      { path: 'name', value: 'Overridden' },
      { path: 'ignored', value: undefined },
    ]);
    fixture.detectChanges();
    component.runtime.resolveContent('root');
    component.runtime.attachInstance('root', {});
    await Promise.resolve();
    await Promise.resolve();

    expect(pluginFactory).toHaveBeenCalledWith(component);
    expect(plugin.onFormularLoaded).toHaveBeenCalledWith(EMPTY_FORM);
    expect(plugin.onSourceLoaded).toHaveBeenCalledWith(expect.objectContaining({ id: 42, name: 'Overridden' }));
    expect(component.formularData()).toBe(EMPTY_FORM);
    expect(component.testId()).toBe('customer-editor-form');
    expect(component.sourceLoading()).toBe(false);

    const saved = vi.fn();
    component.saved.subscribe(saved);
    component.dirty.set(true);
    const response = await firstValueFrom(component.submit({ extra: 'payload' }));
    expect(http.request).toHaveBeenCalledWith('PUT', '/customers/42', {
      body: expect.objectContaining({ id: 42, name: 'Overridden', extra: 'payload' }),
    });
    expect(response).toEqual({ id: 42, name: 'After', saved: true });
    expect(saved).toHaveBeenCalledWith(response);
    expect(component.dirty()).toBe(false);
    expect(component.submitting()).toBe(false);

    component.submitting.set(true);
    expect(() => component.submit()).toThrow('submit is in progress');
    component.submitting.set(false);
    fixture.destroy();
    expect(plugin.destroy).toHaveBeenCalled();
  });

  it('stops invalid submissions and reports unresolved targets and loading failures', async () => {
    const loadError = new Error('offline');
    const http = {
      get: vi.fn(() => throwError(() => loadError)),
      request: vi.fn(),
    };
    const { fixture, component, toast } = await createComponent(http as any);
    fixture.detectChanges();
    await Promise.resolve();
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Fehler beim Laden'), 'error');

    const invalid = { validate: vi.fn(() => false), focus: vi.fn() };
    component.runtime.setFormView({
      rootId: 'field',
      contents: { field: { id: 'field', '@type': 'input' } },
      placements: [],
    } as any);
    component.runtime.resolveContent('field');
    component.runtime.attachInstance('field', invalid);
    await expect(firstValueFrom(component.submit(), { defaultValue: 'completed' })).resolves.toBe('completed');
    expect(invalid.focus).toHaveBeenCalled();
    expect(http.request).not.toHaveBeenCalled();

    component.runtime.attachInstance('field', { validate: () => true });
    await expect(firstValueFrom(component.submit())).rejects.toThrow('target url is unresolvable');
    fixture.destroy();
  });

  it('delegates submit errors to plugins and reacts to source and definition changes', async () => {
    const error = new HttpErrorResponse({ status: 409, error: { message: 'conflict' } });
    const sourceRequests = new Subject<any>();
    const http = {
      get: vi.fn((url: string) => (url === '/forms/customer' ? of(EMPTY_FORM) : sourceRequests)),
      request: vi.fn(() => throwError(() => error)),
    };
    const handledPlugin: FormularPlugin = {
      onSourceLoaded: vi.fn(() => {
        throw new Error('plugin initialization failed');
      }),
      onSubmitError: vi.fn((_error, subscriber) => {
        subscriber.next({ recovered: true });
        subscriber.complete();
        return true;
      }),
    };
    const { fixture, component, toast } = await createComponent(http as any, [() => handledPlugin]);
    fixture.componentRef.setInput('sourceUrl', '/source/one');
    fixture.detectChanges();
    component.runtime.resolveContent('root');
    component.runtime.attachInstance('root', {});
    await Promise.resolve();
    await Promise.resolve();
    sourceRequests.next({ id: 1 });
    sourceRequests.complete();
    expect(toast.add).toHaveBeenCalledWith('Fehler bei der Formularinitialisierung', 'error');
    expect(component.sourceLoading()).toBe(false);

    fixture.componentRef.setInput('submitUrl', '/submit');
    const recovered = await firstValueFrom(component.submit());
    expect(recovered).toEqual({ recovered: true });
    expect(handledPlugin.onSubmitError).toHaveBeenCalledWith(
      error,
      expect.anything(),
      'POST',
      '/submit',
      expect.any(Object),
    );

    const previousSubscription = { closed: false, unsubscribe: vi.fn() };
    (component as any).lastSourceSubscription = previousSubscription;
    fixture.componentRef.setInput('sourceUrl', '/source/two');
    fixture.detectChanges();
    expect(previousSubscription.unsubscribe).toHaveBeenCalled();
    expect(http.get).toHaveBeenCalledWith('/source/two');

    const unhandledHttp = { request: vi.fn(() => throwError(() => error)) };
    (component as any).httpClient = unhandledHttp;
    (component as any).pluginInstances = [];
    await expect(firstValueFrom(component.requestSubmit('PATCH', '/submit', {}))).rejects.toBe(error);
    fixture.destroy();
  });

  it('reports invalid definitions and initialization failures', async () => {
    const invalidForm = {
      ...EMPTY_FORM,
      contents: { root: { id: 'root', '@type': 'not-registered' } },
    } as unknown as KokuDto.FormViewDto;
    const invalid = await createComponent({ get: vi.fn(() => of(invalidForm)) } as any);
    invalid.fixture.detectChanges();
    await Promise.resolve();
    expect(invalid.toast.add).toHaveBeenCalledWith('Fehlerhafte Formularkonfiguration', 'error');
    invalid.fixture.destroy();
    TestBed.resetTestingModule();

    const failing = await createComponent({ get: vi.fn(() => of(EMPTY_FORM)) } as any);
    vi.spyOn(failing.component.runtime, 'whenInitialized').mockRejectedValueOnce(new Error('initialization failed'));
    failing.fixture.detectChanges();
    await Promise.resolve();
    await Promise.resolve();
    expect(failing.toast.add).toHaveBeenCalledWith('Fehler bei der Formularinitialisierung', 'error');
    failing.fixture.destroy();
  });

  it('covers missing and failing sources, request failures and dirty transitions', async () => {
    const sourceError = new Error('source failed');
    const submitError = new Error('submit failed');
    const http = {
      get: vi.fn((url: string) => (url === '/forms/customer' ? of(EMPTY_FORM) : throwError(() => sourceError))),
      request: vi.fn(() => throwError(() => submitError)),
    };
    const { fixture, component } = await createComponent(http as any);
    fixture.componentRef.setInput('sourceUrl', '/source');
    fixture.detectChanges();
    component.runtime.resolveContent('root');
    component.runtime.attachInstance('root', {});
    await Promise.resolve();
    await Promise.resolve();
    expect(component.sourceLoading()).toBe(false);

    fixture.componentRef.setInput('sourceUrl', undefined);
    component.loadSource();
    expect(component.source()).toEqual({});

    fixture.componentRef.setInput('submitUrl', '/submit');
    await expect(firstValueFrom(component.submit())).rejects.toBe(submitError);
    expect(component.submitting()).toBe(false);

    expect(component.dirty()).toBe(false);
    (component as any).setDirty();
    (component as any).setDirty();
    expect(component.dirty()).toBe(true);

    fixture.componentRef.setInput('formularUrl', undefined);
    await expect(firstValueFrom((component as any).loadFormular())).rejects.toBe('missing formularurl');
    fixture.destroy();
  });

  it('accepts a single plugin factory and cancels obsolete definition loads', async () => {
    const pluginFactory = vi.fn(() => ({}));
    const { fixture, component } = await createComponent(
      { get: vi.fn(() => of(EMPTY_FORM)) } as any,
      pluginFactory as any,
    );
    expect(pluginFactory).toHaveBeenCalledWith(component);
    const previous = { closed: false, unsubscribe: vi.fn() };
    (component as any).lastFormularSubscription = previous;
    component.ngOnChanges({ formularUrl: new SimpleChange('/old', '/new', false) });
    expect(previous.unsubscribe).toHaveBeenCalled();
    const subscription = (component as any).loadFormular().subscribe();
    subscription.unsubscribe();
    expect(() => (component as any).completeFormularLoad({ closed: true }, EMPTY_FORM)).not.toThrow();
    await Promise.resolve();
    fixture.destroy();
  });
});
