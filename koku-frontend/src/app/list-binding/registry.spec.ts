import { signal } from '@angular/core';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { executeHttpActionEvent, LIST_CONTENT_SETUP } from './registry';

describe('list content recipes', () => {
  it('resolves every field and preview recipe with its event contract', async () => {
    for (const [type, factory] of Object.entries(LIST_CONTENT_SETUP.fieldRegistry)) {
      const emit = vi.fn();
      const fieldState = { value: signal('value'), config: { '@type': type } as any };
      const recipe = factory!({
        id: type,
        register: signal({} as any),
        fieldState,
        config: signal({
          '@type': type,
          type: 'TEXT',
          rounded: 'MD',
          backgroundColor: 'SUCCESS',
        } as any),
        submitting: signal(false),
        emit,
      });

      expect(await recipe.loadComponent!()).toBeDefined();
      expect(recipe.inputs?.()).toEqual(expect.objectContaining({ name: type, value: 'value' }));
      for (const output of Object.values(recipe.outputs ?? {})) {
        output('payload');
      }
      expect(emit).toHaveBeenCalled();
    }

    for (const [type, factory] of Object.entries(LIST_CONTENT_SETUP.previewRegistry)) {
      const recipe = factory!({
        register: signal({} as any),
        preview: signal({ '@type': type } as any),
        value: signal('preview'),
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      expect(recipe.inputs?.()).toEqual({ value: 'preview' });
    }
  }, 15_000);

  it('resolves every inline and modal recipe including close behavior', async () => {
    const closed = vi.fn();
    const opened = vi.fn();
    const baseContent = {
      formularUrl: '/forms/:id',
      sourceUrl: '/source/:id',
      submitUrl: '/submit/:id',
      documentUrl: '/documents/:id',
      fileUrl: '/files/:id',
      chartUrl: '/charts/:id',
      listUrl: '/lists/:id',
      maxWidthInPx: 480,
      content: [],
      context: { customer: 'source.customer' },
      onCaptureEvents: [],
      onSubmitEvents: [],
    };
    for (const [type, factory] of Object.entries(LIST_CONTENT_SETUP.inlineContentRegistry)) {
      const recipe = factory!({
        content: signal({ '@type': type, ...baseContent } as any),
        loading: signal(false),
        urlSegments: signal({ ':id': '42' }),
        parentRoutePath: signal('/parent'),
        buttonDockOutlet: signal(undefined),
        context: signal({ origin: 'test' }),
        close: closed,
        openRoutedContent: opened,
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      for (const [name, output] of Object.entries(recipe.outputs ?? {})) {
        output(name.includes('Routed') ? ['child'] : 'captured');
      }
    }
    expect(closed).toHaveBeenCalled();
    expect(opened).toHaveBeenCalled();

    for (const [type, factory] of Object.entries(LIST_CONTENT_SETUP.modalRegistry)) {
      const modalClose = vi.fn();
      const onCloseRequested = type === 'dock' ? vi.fn() : undefined;
      const recipe = factory({
        instance: {} as any,
        content: signal({ '@type': type, ...baseContent } as any),
        modal: {
          uid: 1,
          urlSegments: { ':id': '42' },
          parentRoutePath: '/parent',
          close: modalClose,
          update: vi.fn(),
          onCloseRequested,
        },
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      const closeOutput = recipe.outputs?.['closeRequested'];
      if (closeOutput) {
        closeOutput(undefined);
        expect(onCloseRequested ?? modalClose).toHaveBeenCalled();
      }
    }
  }, 15_000);

  it('executes all action recipes and applies successful HTTP event effects', async () => {
    const fieldValue = signal('Ada');
    const source = signal<Record<string, any>>({ nested: { status: 'old' } });
    const register = {
      id: 'item-1',
      source,
      fieldSelection: [],
      fields: { name: { value: fieldValue, config: {} as any } },
      actions: [],
    };
    const reload = vi.fn();
    const openInline = vi.fn();
    const openRouted = vi.fn();
    const toast = vi.fn();
    const parent = {
      httpClient: {
        request: vi.fn(() => of({ id: 'item-1', name: 'Grace', status: 'updated' })),
      },
      modalService: { add: vi.fn() },
      toastService: { add: toast },
      register: signal(register),
      listRegister: signal([register]),
      reloadRequested: { emit: reload },
      openInlineContentRequested: { emit: openInline },
      openRoutedContentRequested: { emit: openRouted },
    } as any;
    const actionByType: Record<string, any> = {
      'http-call': {
        '@type': 'http-call',
        method: 'GET',
        url: '/customers/:name',
        params: [
          {
            param: ':name',
            valueReference: { '@type': 'field-reference', fieldId: 'name' },
          },
        ],
        successEvents: [
          { '@type': 'reload' },
          {
            '@type': 'notification',
            text: 'Saved :name',
            serenity: 'SUCCESS',
            params: [{ param: ':name', valueReference: { '@type': 'field-reference', fieldId: 'name' } }],
          },
          {
            '@type': 'event-payload-update',
            idPath: 'id',
            valueMapping: {
              name: { '@type': 'field-reference', fieldId: 'name' },
              status: { '@type': 'source-path-reference', valuePath: 'nested.status' },
            },
          },
        ],
      },
      condition: { '@type': 'condition' },
      'open-inline-content': { '@type': 'open-inline-content', inlineContent: { '@type': 'formular' } },
      'open-routed-content': {
        '@type': 'open-routed-content',
        route: 'customers/:name',
        params: [{ param: ':name', valueReference: { '@type': 'source-path-reference', valuePath: 'nested.status' } }],
      },
    };

    for (const [type, factory] of Object.entries(LIST_CONTENT_SETUP.actionRegistry)) {
      const recipe = factory!({
        action: signal(actionByType[type]),
        register: signal(register),
        listRegister: signal([register]),
        contentSetup: signal(LIST_CONTENT_SETUP),
        urlSegments: signal(null),
        parent,
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      recipe.outputs?.['clicked']?.({ stopPropagation: vi.fn() } as any);
    }

    expect(parent.httpClient.request).toHaveBeenCalledWith('GET', '/customers/Ada');
    expect(reload).toHaveBeenCalled();
    expect(toast).toHaveBeenCalledWith('Saved Ada', 'success');
    expect(fieldValue()).toBe('Grace');
    expect(source()['nested'].status).toBe('updated');
    expect(openInline).toHaveBeenCalled();
    expect(openRouted).toHaveBeenCalledWith(['customers', 'updated']);
  });

  it('executes confirmed HTTP actions and resets modal buttons after success and failure', () => {
    const register = {
      id: 'item-1',
      source: signal({ name: 'Ada' }),
      fieldSelection: [],
      fields: { name: { value: signal('Ada'), config: {} } },
      actions: [],
    } as any;
    let modalSetup: any;
    const modal = { close: vi.fn(), update: vi.fn() };
    const request = vi.fn(() => of({ id: 'item-1' }));
    const propagated = vi.fn();
    const dispose = GLOBAL_EVENT_BUS.addGlobalEventListener('registry-confirm', 'confirmed', propagated);
    const parent = {
      httpClient: { request },
      modalService: {
        add: vi.fn((setup: any) => {
          modalSetup = setup;
          return modal;
        }),
      },
      toastService: { add: vi.fn() },
      register: signal(register),
      listRegister: signal([register]),
      reloadRequested: { emit: vi.fn() },
      openInlineContentRequested: { emit: vi.fn() },
      openRoutedContentRequested: { emit: vi.fn() },
    } as any;
    const action = signal<any>({
      '@type': 'http-call',
      method: 'DELETE',
      url: '/customers/:name',
      params: [{ param: ':name', valueReference: { '@type': 'source-path-reference', valuePath: 'name' } }],
      userConfirmation: {
        headline: 'Delete :name?',
        content: 'Really delete :name?',
        params: [{ param: ':name', valueReference: { '@type': 'field-reference', fieldId: 'name' } }],
      },
      successEvents: [{ '@type': 'propagate-global-event', eventName: 'confirmed' }],
      failEvents: [{ '@type': 'notification', text: 'Failed', serenity: 'ERROR' }],
    });
    const recipe = LIST_CONTENT_SETUP.actionRegistry['http-call']!({
      action,
      register: signal(register),
      listRegister: signal([register]),
      contentSetup: signal(LIST_CONTENT_SETUP),
      urlSegments: signal(null),
      parent,
    });
    recipe.outputs?.['clicked']?.({ stopPropagation: vi.fn() } as any);
    expect(modalSetup).toEqual(expect.objectContaining({ headline: 'Delete Ada?', content: 'Really delete Ada?' }));
    modalSetup.buttons[0].onClick();
    expect(modal.close).toHaveBeenCalled();
    const confirmButton = modalSetup.buttons[1];
    confirmButton.onClick(new Event('click'), {}, confirmButton);
    expect(request).toHaveBeenCalledWith('DELETE', '/customers/Ada');
    expect(confirmButton.loading).toBe(false);
    expect(confirmButton.disabled).toBe(false);
    expect(propagated).toHaveBeenCalledWith({ id: 'item-1' });

    request.mockImplementationOnce(() => throwError(() => new Error('failed')));
    confirmButton.onClick(new Event('click'), { uid: 1 }, confirmButton);
    expect(modal.update).toHaveBeenCalledWith({ uid: 1 });
    expect(parent.toastService.add).toHaveBeenCalledWith('Failed', 'error');
    modalSetup.clickOutside();
    dispose();

    action.set({ '@type': 'http-call', method: 'GET', url: '/x', userConfirmation: { params: [{}] } });
    expect(() => recipe.outputs?.['clicked']?.({ stopPropagation: vi.fn() } as any)).toThrow('Missing param');
    action.set({ '@type': 'http-call' });
    expect(() => recipe.outputs?.['clicked']?.({ stopPropagation: vi.fn() } as any)).toThrow(
      'configuration is missing',
    );

    const malformedReferences = [
      { param: ':x' },
      { param: ':x', valueReference: { '@type': 'field-reference' } },
      { param: ':x', valueReference: { '@type': 'field-reference', fieldId: 'missing' } },
      { param: ':x', valueReference: { '@type': 'source-path-reference' } },
      { param: ':x', valueReference: { '@type': 'unknown' } },
    ];
    const expectedErrors = [
      'Missing valueReference',
      'Missing fieldId',
      'not resolvable',
      'Missing valuePath',
      'Unknown value reference type',
    ];
    malformedReferences.forEach((param, index) => {
      action.set({ '@type': 'http-call', method: 'GET', url: '/:x', params: [param] });
      expect(() => recipe.outputs?.['clicked']?.({ stopPropagation: vi.fn() } as any)).toThrow(expectedErrors[index]!);
    });

    request.mockImplementationOnce(() => throwError(() => new Error('direct failure')));
    action.set({
      '@type': 'http-call',
      method: 'GET',
      url: '/direct',
      failEvents: [{ '@type': 'notification', text: 'Direct failed', serenity: 'ERROR' }],
    });
    recipe.outputs?.['clicked']?.({ stopPropagation: vi.fn() } as any);
    expect(parent.toastService.add).toHaveBeenCalledWith('Direct failed', 'error');
  });

  it('maps toggle filters and conditional item styling', async () => {
    const toggle = LIST_CONTENT_SETUP.filterRegistry['toggle']!;
    const predicates = {
      DISABLED: [{ field: 'active', value: false }],
      NEUTRAL: [{ field: 'active', value: null }],
      ENABLED: [{ field: 'active', value: true }],
    } as any;
    expect(
      toggle.initialPredicates({ defaultState: 'DISABLED', disabledPredicates: predicates.DISABLED } as any),
    ).toEqual(predicates.DISABLED);
    expect(toggle.initialPredicates({ defaultState: 'NEUTRAL', neutralPredicates: predicates.NEUTRAL } as any)).toEqual(
      predicates.NEUTRAL,
    );
    expect(toggle.initialPredicates({ defaultState: 'ENABLED', enabledPredicates: predicates.ENABLED } as any)).toEqual(
      predicates.ENABLED,
    );
    expect(toggle.initialPredicates({} as any)).toEqual([]);

    const emit = vi.fn();
    const recipe = toggle.createRecipe({
      filter: signal({} as any),
      filterDefinition: signal({
        '@type': 'toggle',
        label: 'Active',
        disabledPredicates: predicates.DISABLED,
        neutralPredicates: predicates.NEUTRAL,
        enabledPredicates: predicates.ENABLED,
      } as any),
      emit,
    });
    expect(await recipe.loadComponent!()).toBeDefined();
    expect(recipe.inputs?.()).toEqual({ label: 'Active' });
    recipe.outputs?.['filterChanged']?.('checked');
    recipe.outputs?.['filterChanged']?.('unchecked');
    recipe.outputs?.['filterChanged']?.('indeterminate');
    expect(emit.mock.calls.map(([value]) => value)).toEqual([
      predicates.ENABLED,
      predicates.DISABLED,
      predicates.NEUTRAL,
    ]);

    const styling = LIST_CONTENT_SETUP.itemStylingRegistry['condition']!;
    const definition = {
      compareValuePath: 'state',
      expectedValues: ['deleted'],
      positiveStyling: { opacity: 50, lineThrough: true },
      negativeStyling: { opacity: 100 },
    } as any;
    expect(styling.itemClasses!(definition, { state: 'deleted' })).toEqual(['opacity-50', 'line-through']);
    expect(styling.itemClasses!(definition, { state: 'active' })).toEqual(['opacity-100']);
  });

  it('does not pass container inputs to the barcode capture component', () => {
    const factory = LIST_CONTENT_SETUP.inlineContentRegistry['barcode']!;
    const captured = vi.fn();
    const dispose = GLOBAL_EVENT_BUS.addGlobalEventListener('barcode-registry-spec', 'barcode-captured', captured);
    const recipe = factory({
      content: signal({
        '@type': 'barcode',
        onCaptureEvents: [{ '@type': 'propagate-global-event', eventName: 'barcode-captured' }],
      } as unknown as KokuDto.ListViewBarcodeContentDto),
      loading: signal(false),
      urlSegments: signal(null),
      parentRoutePath: signal(''),
      buttonDockOutlet: signal(undefined),
      context: signal(undefined),
      close: () => undefined,
      openRoutedContent: () => undefined,
    });

    expect(recipe.inputs).toBeUndefined();
    recipe.outputs?.['afterCapture']('4711');
    expect(captured).toHaveBeenCalledWith('4711');
    dispose();
  });

  it('keeps optional header sources undefined and rejects malformed barcode events', () => {
    const base = {
      loading: signal(false),
      urlSegments: signal<Record<string, string> | null>(null),
      parentRoutePath: signal(''),
      buttonDockOutlet: signal(undefined),
      context: signal(undefined),
      close: vi.fn(),
      openRoutedContent: vi.fn(),
    };
    const header = LIST_CONTENT_SETUP.inlineContentRegistry['header']!({
      ...base,
      content: signal({ '@type': 'header', title: 'Title' } as any),
    });
    expect(header.inputs?.()['sourceUrl']).toBeUndefined();

    const barcode = LIST_CONTENT_SETUP.inlineContentRegistry['barcode']!({
      ...base,
      content: signal({ '@type': 'barcode', onCaptureEvents: [{ '@type': 'propagate-global-event' }] } as any),
    });
    expect(() => barcode.outputs?.['afterCapture']('value')).toThrow('Missing eventName');
  });

  it('resolves formular routes, width and route-based overrides from inline context', () => {
    const factory = LIST_CONTENT_SETUP.inlineContentRegistry['formular']!;
    const closed: string[] = [];
    const openedRoutes: string[][] = [];
    const recipe = factory({
      content: signal({
        '@type': 'formular',
        formularUrl: '/customers/:customerId/form',
        sourceUrl: '/customers/:customerId',
        maxWidthInPx: 0,
        contentOverrides: [
          {
            '@type': 'route-based-override',
            alias: 'customerId',
            routeParam: ':customerId',
            disabled: true,
          },
        ],
      } as unknown as KokuDto.ListViewFormularContentDto),
      loading: signal(false),
      urlSegments: signal({ ':customerId': '42' }),
      parentRoutePath: signal('/customers'),
      buttonDockOutlet: signal(undefined),
      context: signal({ source: 'list' }),
      close: () => closed.push('closed'),
      openRoutedContent: (routes) => openedRoutes.push(routes),
    });

    expect(recipe.inputs?.()).toEqual(
      expect.objectContaining({
        formularUrl: '/customers/42/form',
        sourceUrl: '/customers/42',
        submitUrl: '/customers/42',
        maxWidth: '0px',
        contentOverrides: [{ alias: 'customerId', disabled: true, value: '42' }],
        context: { source: 'list' },
      }),
    );

    recipe.outputs?.['closeRequested'](undefined);
    recipe.outputs?.['openRoutedContentRequested'](['42', 'appointments']);

    expect(closed).toEqual(['closed']);
    expect(openedRoutes).toEqual([['42', 'appointments']]);
  });

  it('propagates document form submit events through the global event bus', () => {
    const factory = LIST_CONTENT_SETUP.inlineContentRegistry['document-form']!;
    const payloads: any[] = [];
    GLOBAL_EVENT_BUS.addGlobalEventListener('list-registry-spec', 'document-created', (payload) =>
      payloads.push(payload),
    );
    const recipe = factory({
      content: signal({
        '@type': 'document-form',
        documentUrl: '/documents/:documentId',
        submitUrl: '/documents/:documentId/render',
        onSubmitEvents: [
          {
            '@type': 'propagate-global-event',
            eventName: 'document-created',
          },
        ],
      } as unknown as KokuDto.ListViewDocumentFormContentDto),
      loading: signal(false),
      urlSegments: signal({ ':documentId': 'abc' }),
      parentRoutePath: signal('/documents'),
      buttonDockOutlet: signal(undefined),
      context: signal(undefined),
      close: () => undefined,
      openRoutedContent: () => undefined,
    });

    expect(recipe.inputs?.()).toEqual(
      expect.objectContaining({
        documentUrl: '/documents/abc',
        submitUrl: '/documents/abc/render',
      }),
    );

    recipe.outputs?.['submitted']({ id: 'rendered-file' });

    expect(payloads).toEqual([{ id: 'rendered-file' }]);
    GLOBAL_EVENT_BUS.removeGlobalEventListener('list-registry-spec');
  });

  it('validates document submit and HTTP action event contracts', () => {
    const base = {
      loading: signal(false),
      urlSegments: signal<Record<string, string> | null>(null),
      parentRoutePath: signal(''),
      buttonDockOutlet: signal(undefined),
      context: signal(undefined),
      close: vi.fn(),
      openRoutedContent: vi.fn(),
    };
    const documentContent = signal<any>({
      '@type': 'document-form',
      onSubmitEvents: [{ '@type': 'unknown' }],
    });
    const documentRecipe = LIST_CONTENT_SETUP.inlineContentRegistry['document-form']!({
      ...base,
      content: documentContent,
    });
    expect(() => documentRecipe.outputs?.['submitted']({})).toThrow('Unknown onSubmitEvent type');
    documentContent.set({ '@type': 'document-form', onSubmitEvents: [{ '@type': 'propagate-global-event' }] });
    expect(() => documentRecipe.outputs?.['submitted']({})).toThrow('Missing eventName');

    const item = {
      id: '1',
      fields: { name: { value: signal('Ada'), config: {} } },
      source: signal<any>({ nested: { status: 'old' } }),
    } as any;
    const parent = {
      listRegister: signal([item]),
      reloadRequested: { emit: vi.fn() },
      toastService: { add: vi.fn() },
    } as any;
    expect(() => executeHttpActionEvent(parent, { '@type': 'unknown' } as any, {})).toThrow('Unknown event type');
    expect(() => executeHttpActionEvent(parent, { '@type': 'event-payload-update' } as any, {})).toThrow('Missing id');
    expect(() =>
      executeHttpActionEvent(parent, { '@type': 'event-payload-update', idPath: 'id' } as any, { id: 'missing' }),
    ).not.toThrow();
    const update = (reference: any) =>
      executeHttpActionEvent(
        parent,
        { '@type': 'event-payload-update', idPath: 'id', valueMapping: { value: reference } } as any,
        { id: '1', value: 'new' },
      );
    expect(() => update({ '@type': 'field-reference' })).toThrow('Missing fieldId');
    expect(() => update({ '@type': 'field-reference', fieldId: 'missing' })).toThrow('not resolvable');
    expect(() => update({ '@type': 'source-path-reference' })).toThrow('Missing valuePath');
    expect(() => update({ '@type': 'unknown' })).toThrow('Unknown Reference');
    expect(() => executeHttpActionEvent(parent, { '@type': 'notification' } as any, {})).toThrow('Missing text');
    expect(() =>
      executeHttpActionEvent(parent, { '@type': 'notification', text: 'Hi', params: [{}] } as any, {}),
    ).toThrow('Missing param');
    executeHttpActionEvent(parent, { '@type': 'notification', text: 'Hi' } as any, {});
    expect(parent.toastService.add).toHaveBeenCalledWith('Hi', 'info');
    expect(() =>
      executeHttpActionEvent(parent, { '@type': 'notification', text: 'Hi', serenity: 'OTHER' } as any, {}),
    ).toThrow('Unknown Notification serenity');
    expect(() => executeHttpActionEvent(parent, { '@type': 'propagate-global-event' } as any, {})).toThrow(
      'Missing eventName',
    );
  });
});
