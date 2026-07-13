import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Observable, of, Subscriber, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import {
  BusinessExceptionPlugin,
  BusinessRulePlugin,
  ButtonListenerPlugin,
  GlobalEventListenerPlugin,
  UnsavedChangesPreventionGuardPlugin,
  FORMULAR_PLUGIN_PROVIDERS,
} from './formular.plugins';
import { GLOBAL_EVENT_BUS } from './events/global-events';
import { FORMULAR_PLUGIN, FormularRuntime } from './formular/formular.component';
import { ModalService } from './modal/modal.service';
import { UnsavedChangesPreventionGuard } from './navi/UnsavedChangesPreventionGuard';
import { ToastService } from './toast/toast.service';

const renderedModal = () => ({
  uid: 1,
  close: vi.fn(),
  update: vi.fn(),
});

const runtimeWithContent = (content: Record<string, any>, initialValue: any) => {
  const runtime = new FormularRuntime(() => undefined);
  const id = String(content['id']);
  runtime.setFormView({
    rootId: id,
    contents: { [id]: content },
    placements: [],
  } as unknown as KokuDto.FormViewDto);
  runtime.resolveContent(id, {
    createValue: () => signal(initialValue),
    writeSource: (source, value) => source.set(content['valuePath'] ?? content['id'], value),
  });
  runtime.initializeSource({ id: 1 }, []);
  return runtime;
};

describe('GlobalEventListenerPlugin', () => {
  it('updates matching sources and appends mapped field configuration and values', () => {
    const runtime = runtimeWithContent(
      {
        id: 'products',
        '@type': 'multi-select',
        valuePath: 'products',
        possibleValues: [],
      },
      [],
    );
    const formular = { runtime, source: runtime.source } as any;
    const plugin = new GlobalEventListenerPlugin(formular);
    plugin.onFormularLoaded({
      globalEventListeners: [
        { '@type': 'source-update-via-payload', eventName: 'source-updated', idPath: 'id' },
        {
          '@type': 'field-update-via-payload',
          eventName: 'product-created',
          configMapping: {
            products: {
              targetConfigPath: 'possibleValues',
              valueMapping: {
                '@type': 'append-list',
                valueMapping: [
                  { '@type': 'source-path', sourcePath: 'product.id', targetPath: 'id' },
                  {
                    '@type': 'string-transformation',
                    targetPath: 'label',
                    transformPattern: ':name (:id)',
                    transformPatternParameters: {
                      ':name': { '@type': 'source-path', sourcePath: 'product.name' },
                      ':id': { '@type': 'source-path', sourcePath: 'product.id' },
                    },
                  },
                  { '@type': 'string-conversion', sourcePath: 'product.id', targetPath: 'code' },
                  { '@type': 'static-value', targetPath: 'color', value: 'SUCCESS' },
                ],
              },
            },
          },
          fieldValueMapping: {
            products: {
              '@type': 'append-list',
              targetPathMapping: {
                productId: { '@type': 'source-path', sourcePath: 'product.id' },
                active: { '@type': 'static-value', value: true },
              },
            },
          },
        },
      ],
    } as any);

    GLOBAL_EVENT_BUS.propagateGlobalEvent('source-updated', { id: 1, name: 'Updated' });
    expect(runtime.source()['name']).toBe('Updated');
    GLOBAL_EVENT_BUS.propagateGlobalEvent('source-updated', { id: 2, name: 'Ignored' });
    expect(runtime.source()['name']).toBe('Updated');

    GLOBAL_EVENT_BUS.propagateGlobalEvent('product-created', {
      product: { id: 42, name: 'Treatment' },
    });
    expect((runtime.content('products') as any).possibleValues).toEqual([
      { id: 42, label: 'Treatment (42)', code: '42', color: 'SUCCESS' },
    ]);
    expect(runtime.contentHandle('products')?.value?.()).toEqual([{ productId: 42, active: true }]);

    plugin.destroy();
    GLOBAL_EVENT_BUS.propagateGlobalEvent('product-created', { product: { id: 43, name: 'Removed' } });
    expect(runtime.contentHandle('products')?.value?.()).toHaveLength(1);
  });

  it('rejects malformed and unknown listener configurations', () => {
    const runtime = runtimeWithContent({ id: 'field', '@type': 'input', valuePath: 'field' }, '');
    const plugin = new GlobalEventListenerPlugin({ runtime, source: runtime.source } as any);
    expect(() => plugin.onFormularLoaded({ globalEventListeners: [{ '@type': 'x' }] } as any)).toThrow(
      'Missing eventName',
    );
    plugin.onFormularLoaded({ globalEventListeners: [{ '@type': 'x', eventName: 'bad' }] } as any);
    expect(() => GLOBAL_EVENT_BUS.propagateGlobalEvent('bad', {})).toThrow('Global event listener failed');
    plugin.destroy();
  });

  it('validates configuration and value mapping contracts', () => {
    const runtime = runtimeWithContent({ id: 'field', '@type': 'multi-select', possibleValues: [] }, []);
    const plugin = new GlobalEventListenerPlugin({ runtime, source: runtime.source } as any) as any;
    expect(() => plugin.updateSourceViaPayload({}, {})).toThrow('Missing idPath');
    expect(() => plugin.updateContentConfigViaPayload({ configMapping: { missing: {} } }, {})).toThrow(
      'Content state not found',
    );
    expect(() =>
      plugin.appendContentConfigListItem('field', {}, { valueMapping: { '@type': 'append-list' } }, {}),
    ).toThrow('Missing targetConfigPath');
    expect(() => plugin.applyConfigListItemValue({}, { '@type': 'unknown' }, {})).toThrow('Unknown value Mapping');
    expect(() => plugin.applySourcePathConfigListItemValue({}, { targetPath: 'x' }, {})).toThrow('Missing sourcePath');
    expect(() => plugin.applySourcePathConfigListItemValue({}, { sourcePath: 'x' }, {})).toThrow('Missing targetPath');
    expect(() => plugin.applyStringTransformationConfigListItemValue({}, {}, {})).toThrow('Missing targetPath');
    expect(() =>
      plugin.transformStringValue({ transformPatternParameters: { ':x': { '@type': 'unknown' } } }, {}),
    ).toThrow('Unknown Param type');
    expect(() =>
      plugin.transformStringValue({ transformPatternParameters: { ':x': { '@type': 'source-path' } } }, {}),
    ).toThrow('Missing sourcePath');
    expect(() => plugin.applyStringConversionConfigListItemValue({}, { targetPath: 'x' }, {})).toThrow(
      'Missing sourcePath',
    );
    expect(() => plugin.applyStringConversionConfigListItemValue({}, { sourcePath: 'x' }, {})).toThrow(
      'Missing targetPath',
    );
    expect(() => plugin.applyStaticConfigListItemValue({}, {})).toThrow('Missing targetPath');
    expect(() => plugin.updateContentValuesViaPayload({ fieldValueMapping: { field: null } }, {})).toThrow(
      'Unexpected reference',
    );
    expect(() => plugin.resolveUpdatedContentValue('field', { '@type': 'unknown' }, {})).toThrow(
      'Unexpected reference',
    );
    expect(() => plugin.resolveUpdatedContentValue('missing', { '@type': 'field-reference' }, {})).toThrow(
      'Content value state not found',
    );
    expect(
      plugin.resolveUpdatedContentValue(
        'field',
        { '@type': 'field-reference', source: { '@type': 'static-value', value: 'resolved' } },
        {},
      ),
    ).toBe('resolved');
    expect(plugin.resolveAppendListValue({ targetPathMapping: { skipped: { '@type': 'static-value' } } }, {})).toEqual(
      {},
    );
    expect(() => plugin.resolveFieldReferenceValue({}, {})).toThrow('Unexpected reference source');
    expect(() => plugin.resolvePayloadValueSource({ '@type': 'source-path' }, {})).toThrow(
      'Unexpected reference sourcePath',
    );
    expect(() => plugin.resolvePayloadValueSource({ '@type': 'unknown' }, {})).toThrow('Unknown mapping source type');
    plugin.destroy();
  });
});

describe('ButtonListenerPlugin', () => {
  it('submits, post-processes and dispatches success events', () => {
    const runtime = runtimeWithContent(
      {
        id: 'save',
        '@type': 'button',
        buttonType: 'SUBMIT',
        postProcessingActions: [{ '@type': 'reload' }],
        successEvents: [
          {
            '@type': 'notification',
            text: 'Saved :name',
            serenity: 'SUCCESS',
            params: [{ '@type': 'value', param: ':name', sourcePath: 'name' }],
          },
          { '@type': 'propagate-global-event', eventName: 'saved' },
        ],
      },
      undefined,
    );
    runtime.replaceSource({ name: 'Ada' });
    const toast = { add: vi.fn() };
    const loadSource = vi.fn();
    const listener = vi.fn();
    const dispose = GLOBAL_EVENT_BUS.addGlobalEventListener('button-plugin-spec', 'saved', listener);
    const formular = {
      runtime,
      source: runtime.source,
      submit: vi.fn(() => of({ id: 7 })),
      loadSource,
    } as any;
    const plugin = new ButtonListenerPlugin({ add: vi.fn() } as any, toast as any, formular);
    plugin.onFormularLoaded({
      contents: { save: runtime.content('save') },
    } as any);

    runtime.emit('save', 'CLICK');

    expect(formular.submit).toHaveBeenCalled();
    expect(loadSource).toHaveBeenCalled();
    expect(toast.add).toHaveBeenCalledWith('Saved Ada', 'success');
    expect(listener).toHaveBeenCalledWith({ id: 7 });
    plugin.destroy();
    dispose();
  });

  it('executes confirmed submissions and fail notifications', () => {
    const button = {
      id: 'save',
      '@type': 'button',
      buttonType: 'SUBMIT',
      userConfirmation: {
        headline: 'Save :name?',
        content: 'Confirm :name',
        params: [{ '@type': 'source-path', param: ':name', sourcePath: 'name' }],
      },
      failEvents: [{ '@type': 'notification', text: 'Failed', serenity: 'ERROR' }],
    };
    const runtime = runtimeWithContent(button, undefined);
    runtime.replaceSource({ name: 'Ada' });
    const modal = renderedModal();
    let modalConfig: any;
    const modalService = {
      add: vi.fn((config) => {
        modalConfig = config;
        return modal;
      }),
    };
    const toast = { add: vi.fn() };
    const formular = {
      runtime,
      source: runtime.source,
      submit: vi.fn(() => throwError(() => new Error('failed'))),
    } as any;
    const plugin = new ButtonListenerPlugin(modalService as any, toast as any, formular);
    plugin.onFormularLoaded({ contents: { save: button } } as any);

    runtime.emit('save', 'CLICK');
    expect(modalConfig.headline).toBe('Save Ada?');
    const confirmButton = modalConfig.buttons[1];
    confirmButton.onClick(new Event('click'), modal, confirmButton);
    expect(toast.add).toHaveBeenCalledWith('Failed', 'error');
    expect(confirmButton.loading).toBe(false);
    expect(confirmButton.disabled).toBe(false);
    modalConfig.buttons[0].onClick();
    modalConfig.clickOutside();
    expect(modal.close).toHaveBeenCalled();
    plugin.destroy();
  });

  it('validates button, confirmation, notification and event contracts', () => {
    const runtime = runtimeWithContent({ id: 'field', '@type': 'input' }, 'Ada');
    const formular = { runtime, source: runtime.source, loadSource: vi.fn(), submit: vi.fn(() => of({})) } as any;
    const plugin = new ButtonListenerPlugin({ add: vi.fn() } as any, { add: vi.fn() } as any, formular) as any;
    expect(() => plugin.requireButtonId({ '@type': 'button' })).toThrow('missing button id');
    expect(() => plugin.registerButtonListener({ id: 'missing', '@type': 'button' })).toThrow(
      'Button handle not found',
    );
    expect(() => plugin.registerButtonListener({ id: 'field', '@type': 'input' })).not.toThrow();
    expect(() => plugin.handleButtonEvent('field', { id: 'field', '@type': 'input' }, 'INPUT')).not.toThrow();
    expect(() => plugin.applyPostProcessingAction({ '@type': 'unknown' })).toThrow('Unknown PostProcessingAction');
    expect(() => plugin.resolveUserConfirmationParamValue({})).toThrow('Missing param');
    expect(() => plugin.resolveUserConfirmationParamValue({ param: ':x', '@type': 'unknown' })).toThrow(
      'Unknown param type',
    );
    expect(() => plugin.resolveUserConfirmationParamValue({ param: ':x', '@type': 'source-path' })).toThrow(
      'Missing valuePath',
    );
    expect(() => plugin.executeButtonEvents([{ '@type': 'unknown' }])).toThrow('Unknown event type');
    expect(() => plugin.showButtonNotification({})).toThrow('Missing text');
    expect(() => plugin.showButtonNotification({ text: ':x', params: [{}] })).toThrow('Missing param');
    expect(() => plugin.resolveNotificationParamValue({ '@type': 'unknown' })).toThrow('Unknown param type');
    expect(() => plugin.resolveNotificationParamValue({ '@type': 'value', param: ':x' })).toThrow('Missing valuePath');
    expect(plugin.formNotificationSerenity({})).toBe('info');
    expect(() => plugin.formNotificationSerenity({ serenity: 'UNKNOWN' })).toThrow('Unknown Notification serenity');
    expect(() => plugin.propagateButtonGlobalEvent({})).toThrow('Missing eventName');
    expect(() => plugin.executeWithUserConfirmation({}, () => of('ok'))).not.toThrow();
    expect(() => plugin.executeWithUserConfirmation({}, () => throwError(() => new Error('failed')))).not.toThrow();
    const confirmationModal = renderedModal();
    const confirmationButton: any = { loading: false, disabled: false };
    plugin.executeConfirmedAction(() => of('ok'), confirmationModal, {}, confirmationButton);
    expect(confirmationButton).toEqual(expect.objectContaining({ loading: false, disabled: false }));
    expect(confirmationModal.close).toHaveBeenCalled();
    plugin.destroy();
  });
});

describe('UnsavedChangesPreventionGuardPlugin', () => {
  it('registers once and resolves clean, cancelled and confirmed navigation', () => {
    let decisionFactory: (() => Observable<boolean>) | undefined;
    const guard = {
      registerUnsavedChangesPrevention: vi.fn((_owner, factory) => {
        decisionFactory = factory;
      }),
      unregisterUnsavedChangesPrevention: vi.fn(),
    };
    const dirty = signal(false);
    const modal = renderedModal();
    let modalConfig: any;
    const modalService = {
      add: vi.fn((config) => {
        modalConfig = config;
        return modal;
      }),
      close: vi.fn(),
    };
    const plugin = new UnsavedChangesPreventionGuardPlugin(guard as any, modalService as any, { dirty } as any);
    plugin.onSourceLoaded();
    plugin.onSourceLoaded();
    expect(guard.registerUnsavedChangesPrevention).toHaveBeenCalledOnce();

    const clean = vi.fn();
    decisionFactory!().subscribe(clean);
    expect(clean).toHaveBeenCalledWith(true);

    dirty.set(true);
    const cancelled = vi.fn();
    decisionFactory!().subscribe(cancelled);
    modalConfig.buttons[0].onClick(new Event('click'), modal);
    expect(cancelled).toHaveBeenCalledWith(false);

    dirty.set(true);
    const confirmed = vi.fn();
    decisionFactory!().subscribe(confirmed);
    modalConfig.buttons[1].onClick(new Event('click'), modal);
    expect(confirmed).toHaveBeenCalledWith(true);
    expect(dirty()).toBe(false);
    dirty.set(true);
    const outside = vi.fn();
    decisionFactory!().subscribe(outside);
    modalConfig.clickOutside();
    expect(outside).toHaveBeenCalledWith(false);
    plugin.destroy();
    expect(guard.unregisterUnsavedChangesPrevention).toHaveBeenCalledWith(plugin);
  });
});

describe('BusinessExceptionPlugin', () => {
  it('handles confirmation errors with close and alternate endpoint buttons', () => {
    const modal = renderedModal();
    let modalConfig: any;
    const modalService = {
      add: vi.fn((config) => {
        modalConfig = config;
        return modal;
      }),
      close: vi.fn(),
    };
    const requestSubmit = vi.fn(() => of({ accepted: true }));
    const plugin = new BusinessExceptionPlugin(modalService as any, { requestSubmit } as any);
    const next = vi.fn();
    const complete = vi.fn();
    const request = new Subscriber({ next, error: vi.fn(), complete });
    const error = new HttpErrorResponse({
      status: 409,
      error: {
        '@type': 'business-error-with-confirmation-message',
        headline: 'Conflict',
        confirmationMessage: 'Continue?',
        closeOnClickOutside: true,
        buttons: [
          { '@type': 'close-button', text: 'Close' },
          {
            '@type': 'send-to-different-endpoint-button',
            text: 'Force',
            endpointMethod: 'PUT',
            endpointUrl: '/force',
            showLoadingAnimation: true,
            showDisabledState: true,
          },
        ],
      },
    });

    expect(plugin.onSubmitError(error, request, 'POST', '/submit', { id: 1 })).toBe(true);
    const endpointButton = modalConfig.buttons[1];
    endpointButton.onClick(new Event('click'), modal, endpointButton);
    expect(requestSubmit).toHaveBeenCalledWith('PUT', '/force', { id: 1 });
    expect(next).toHaveBeenCalledWith({ accepted: true });
    expect(complete).toHaveBeenCalled();
    modalConfig.buttons[0].onClick();
    modalConfig.clickOutside();
    expect(modalService.close).toHaveBeenCalled();
    expect(plugin.onSubmitError(new HttpErrorResponse({ status: 500, error: {} }), request, 'POST', '/', {})).toBe(
      false,
    );
  });

  it('rejects unknown confirmation button types', () => {
    const plugin = new BusinessExceptionPlugin({} as any, {} as any) as any;
    expect(() => plugin.createSubmitBusinessExceptionButton({ '@type': 'unknown' })).toThrow('Unknown button type');
  });

  it('resets alternate-endpoint button state after request failures', () => {
    const plugin = new BusinessExceptionPlugin(
      { close: vi.fn() } as any,
      { requestSubmit: vi.fn(() => throwError(() => new Error('failed'))) } as any,
    ) as any;
    const button = { loading: false, disabled: false };
    plugin.submitBusinessExceptionEndpoint(
      { showLoadingAnimation: true, showDisabledState: true },
      new Subscriber(),
      'POST',
      '/submit',
      {},
      () => renderedModal(),
      button,
    );
    expect(button).toEqual({ loading: false, disabled: false });
  });
});

describe('BusinessRulePlugin', () => {
  it('emits INIT then REINIT and clears empty rule registrations', () => {
    const runtime = runtimeWithContent({ id: 'field', '@type': 'input', valuePath: 'field' }, '');
    const events: string[] = [];
    runtime.contentHandle('field')!.events.subscribe((event) => events.push(event.eventName));
    const plugin = new BusinessRulePlugin({} as any, {} as any, { add: vi.fn() } as any, { runtime } as any);
    plugin.onFormularLoaded({ businessRules: [{}] } as any);
    const executor = (plugin as any).registeredBusinessRuleExecutors[0];
    executor.contentRuntime.contentHandle('field');
    executor.contentRuntime.updateContentValue('field', 'updated');
    executor.contentRuntime.updateContentLoading('field', 'rule', true);
    executor.hooks.onExecutionError(new Error('failed'));
    plugin.onSourceLoaded();
    plugin.onSourceLoaded();
    expect(events).toEqual(['INIT', 'REINIT']);
    plugin.destroy();
  });
});

describe('formular plugin providers', () => {
  it('creates every configured plugin through its Angular injection contract', () => {
    TestBed.configureTestingModule({
      providers: [
        ...FORMULAR_PLUGIN_PROVIDERS,
        { provide: HttpClient, useValue: {} },
        { provide: ModalService, useValue: {} },
        { provide: ToastService, useValue: {} },
        { provide: UnsavedChangesPreventionGuard, useValue: {} },
      ],
    });
    const factories = TestBed.inject(FORMULAR_PLUGIN) as unknown as ((formular: any) => any)[];
    const formular = { runtime: {}, dirty: signal(false) };
    const plugins = factories.map((factory) => TestBed.runInInjectionContext(() => factory(formular)));
    expect(plugins.map((plugin) => plugin.constructor)).toEqual([
      BusinessRulePlugin,
      ButtonListenerPlugin,
      UnsavedChangesPreventionGuardPlugin,
      BusinessExceptionPlugin,
      GlobalEventListenerPlugin,
    ]);
  });
});
