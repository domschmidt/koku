import { signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { of, Subject } from 'rxjs';
import { ModalService } from '../modal/modal.service';
import { BusinessRuleExecutor, BusinessRuleExecutorContentHandle } from './BusinessRuleExecutor';
import { GLOBAL_EVENT_BUS } from '../events/global-events';

describe('BusinessRuleExecutor', () => {
  it('keeps loading active when a running request is replaced and handles errors', () => {
    const firstRequest = new Subject<unknown>();
    const secondRequest = new Subject<unknown>();
    const request = vi.fn().mockReturnValueOnce(firstRequest).mockReturnValueOnce(secondRequest);
    const events = new Subject<any>();
    const loadingCauses = signal(new Set<string>());
    const handles: Record<string, BusinessRuleExecutorContentHandle> = {
      field: {
        value: signal('value'),
        events,
      },
    };
    const onExecutionError = vi.fn();
    const executor = new BusinessRuleExecutor(
      { request } as unknown as HttpClient,
      {} as ModalService,
      {},
      {
        id: 'rule',
        references: [
          {
            reference: 'field',
            loadingAnimation: true,
            listeners: [{ event: 'CHANGE' }],
          },
        ],
        execution: { '@type': 'call-http-endpoint', method: 'GET', url: '/test' },
      } as KokuDto.KokuBusinessRuleDto,
      {
        contentHandle: (referenceId) => handles[referenceId],
        updateContentValue: () => undefined,
        updateContentLoading: (_referenceId, cause, loading) => {
          const causes = new Set(loadingCauses());
          if (loading) {
            causes.add(cause);
          } else {
            causes.delete(cause);
          }
          loadingCauses.set(causes);
        },
      },
      { onExecutionError },
    );

    events.next({ eventName: 'CHANGE' });
    expect(loadingCauses().has('rule')).toBe(true);
    expect(() => (executor as any).requireEventName(undefined)).toThrow('Event name is required');

    events.next({ eventName: 'CHANGE' });
    expect(loadingCauses().has('rule')).toBe(true);

    secondRequest.error(new Error('failed'));
    expect(loadingCauses().has('rule')).toBe(false);
    expect(onExecutionError).toHaveBeenCalled();
    executor.destroy();
  });

  it('reports applied result values through the adapter hook', () => {
    const requestResult = new Subject<any>();
    const triggerEvents = new Subject<any>();
    const resultValue = signal(0);
    const handles: Record<string, BusinessRuleExecutorContentHandle> = {
      trigger: {
        value: signal('value'),
        events: triggerEvents,
      },
      result: {
        value: resultValue,
        events: new Subject<any>(),
      },
    };
    const updateContentValue = vi.fn().mockImplementation((referenceId: string, value: any) => {
      if (referenceId === 'result') {
        resultValue.set(value);
      }
    });
    const executor = new BusinessRuleExecutor(
      { request: () => requestResult } as unknown as HttpClient,
      {} as ModalService,
      {},
      {
        id: 'rule',
        references: [
          { reference: 'trigger', listeners: [{ event: 'CHANGE' }] },
          { reference: 'result', resultUpdateMode: 'ALWAYS', resultValuePath: 'summary' },
        ],
        execution: { '@type': 'call-http-endpoint', method: 'GET', url: '/test' },
      } as KokuDto.KokuBusinessRuleDto,
      {
        contentHandle: (referenceId) => handles[referenceId],
        updateContentValue,
        updateContentLoading: () => undefined,
      },
    );

    triggerEvents.next({ eventName: 'CHANGE' });
    requestResult.next({ summary: 42 });

    expect(resultValue()).toBe(42);
    expect(updateContentValue).toHaveBeenCalledTimes(1);
    expect(updateContentValue).toHaveBeenCalledWith('result', 42);
    executor.destroy();
  });

  it('opens dialogs, serializes request values and enforces execution contracts', () => {
    const events = new Subject<any>();
    const handles: Record<string, BusinessRuleExecutorContentHandle> = {
      field: { value: signal(['Ada', 'Grace']), events },
    };
    const runtime = {
      contentHandle: (id: string) => handles[id],
      updateContentValue: vi.fn(),
      updateContentLoading: vi.fn(),
    };
    const modal = { close: vi.fn() };
    let modalConfig: any;
    const modalService = {
      add: vi.fn((config: any) => {
        modalConfig = config;
        return modal;
      }),
    };
    const dialogExecutor = new BusinessRuleExecutor(
      { request: vi.fn() } as any,
      modalService as any,
      { formular: vi.fn() } as any,
      {
        id: 'dialog-rule',
        references: [{ reference: 'field', listeners: [{ event: 'CHANGE' }], loadingAnimation: true }],
        execution: {
          '@type': 'open-dialog-content',
          content: { '@type': 'formular' },
          closeEventListeners: [{ '@type': 'global-event-listener', eventName: 'dialog-saved' }],
        },
      } as any,
      runtime,
    );
    events.next({ eventName: 'INPUT' });
    expect(modalService.add).not.toHaveBeenCalled();
    events.next({ eventName: 'CHANGE' });
    expect(modalService.add).toHaveBeenCalledWith(expect.objectContaining({ fullscreen: true }));
    modalConfig.clickOutside();
    modalConfig.onCloseRequested();
    GLOBAL_EVENT_BUS.propagateGlobalEvent('dialog-saved', {});
    expect(modal.close).toHaveBeenCalledTimes(3);
    expect(runtime.updateContentLoading).toHaveBeenCalledWith('field', 'dialog-rule', false);
    dialogExecutor.destroy();

    const request = vi.fn(() => of({}));
    const httpExecutor = new BusinessRuleExecutor(
      { request } as any,
      {} as any,
      {},
      {
        id: 'http-rule',
        references: [{ reference: 'field', requestParam: 'filters.names', listeners: [{ event: 'CHANGE' }] }],
        execution: { '@type': 'call-http-endpoint', method: 'GET', url: '/search?fixed=true' },
      } as any,
      runtime,
    );
    events.next({ eventName: 'CHANGE' });
    expect(request).toHaveBeenCalledWith(
      'GET',
      expect.stringMatching(/^\/search\?fixed=true&filters\.names%5B0%5D=Ada/),
      { body: undefined },
    );
    expect((httpExecutor as any).buildEndpointUrl({ method: 'POST', url: '/save' }, {})).toBe('/save');
    expect(() => (httpExecutor as any).buildEndpointUrl({ method: 'GET' }, {})).toThrow('Url is not defined');
    expect(() => (httpExecutor as any).registerCloseEventListener({ '@type': 'unknown' }, vi.fn())).toThrow(
      'Unknown event listener type',
    );
    expect(() =>
      (httpExecutor as any).registerCloseEventListener({ '@type': 'global-event-listener' }, vi.fn()),
    ).toThrow('Missing eventName');
    expect(() => (httpExecutor as any).executeHttpEndpoint({ url: '/x' })).toThrow('Method is not defined');
    expect(() => (httpExecutor as any).executeHttpEndpoint({ method: 'GET' })).toThrow('Url is not defined');
    expect(() => (httpExecutor as any).executeOpenDialog({})).toThrow('content is not defined');
    httpExecutor.destroy();

    const make = (config: any, customHandles = handles) =>
      new BusinessRuleExecutor({} as any, {} as any, {}, config, {
        ...runtime,
        contentHandle: (id: string) => customHandles[id],
      });
    expect(() => make({ references: [{}] })).toThrow('Reference not specified');
    expect(() => make({ references: [{ reference: 'missing' }] })).toThrow('Reference not found');
    expect(() => make({ references: [{ reference: 'field', listeners: [{}] }] })).toThrow('Listener not specified');
    const noValue = { field: { events } } as any;
    const valueExecutor = make({ id: 'x', references: [{ reference: 'field' }] }, noValue);
    expect(() => (valueExecutor as any).addReferenceValueToRequestBody({}, {})).toThrow('Reference not specified');
    expect(() => (valueExecutor as any).addReferenceValueToRequestBody({}, { reference: 'field' })).toThrow(
      'requestParam not specified',
    );
    expect(() =>
      (valueExecutor as any).addReferenceValueToRequestBody({}, { reference: 'missing', requestParam: 'x' }),
    ).toThrow('Reference not found');
    expect(() =>
      (valueExecutor as any).addReferenceValueToRequestBody({}, { reference: 'field', requestParam: 'x' }),
    ).toThrow('Reference has no value');
    valueExecutor.destroy();

    const missingExecution = make({ id: 'x', references: [] });
    expect(() => (missingExecution as any).triggerContentEvent()).toThrow('Execution is not defined');
    const missingId = make({ references: [], execution: { '@type': 'call-http-endpoint', method: 'GET', url: '/x' } });
    expect(() => (missingId as any).triggerContentEvent()).toThrow('Rule id is not defined');
    expect(() => (missingId as any).setLoadingAnimation(true)).toThrow('Rule id is not defined');
    expect(() => (missingId as any).afterExecutionFinished({})).toThrow('Rule id is not defined');
    const missingReferences = make({ id: 'x', execution: { '@type': 'call-http-endpoint', method: 'GET', url: '/x' } });
    expect(() => (missingReferences as any).triggerContentEvent()).toThrow('References not defined');
    const unknownExecution = make({ id: 'x', references: [], execution: { '@type': 'unknown' } });
    expect(() => (unknownExecution as any).triggerContentEvent()).toThrow('Execution is unknown');

    const resultExecutor = make({ id: 'x', references: [] });
    (resultExecutor as any).config.references = [{}];
    expect(() => (resultExecutor as any).afterExecutionFinished({})).toThrow('Reference not specified');
    (resultExecutor as any).config.references = [{ reference: 'missing' }];
    expect(() => (resultExecutor as any).afterExecutionFinished({})).toThrow('Reference not found');
    (resultExecutor as any).config.references = [{ reference: 'field', resultUpdateMode: 'ALWAYS' }];
    expect(() => (resultExecutor as any).afterExecutionFinished({})).toThrow('resultValuePath not specified');
    (valueExecutor as any).config.references = [
      { reference: 'field', resultUpdateMode: 'ALWAYS', resultValuePath: 'value' },
    ];
    expect(() => (valueExecutor as any).afterExecutionFinished({ value: 1 })).toThrow('Reference has no value');
    missingExecution.destroy();
    missingId.destroy();
    missingReferences.destroy();
    unknownExecution.destroy();
    resultExecutor.destroy();
  });
});
