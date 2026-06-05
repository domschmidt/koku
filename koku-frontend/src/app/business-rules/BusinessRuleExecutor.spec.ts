import { signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject } from 'rxjs';
import { ModalService } from '../modal/modal.service';
import { BusinessRuleExecutor, BusinessRuleExecutorContentHandle } from './BusinessRuleExecutor';

describe('BusinessRuleExecutor', () => {
  it('keeps loading active when a running request is replaced and handles errors', () => {
    const firstRequest = new Subject<unknown>();
    const secondRequest = new Subject<unknown>();
    const request = jasmine.createSpy().and.returnValues(firstRequest, secondRequest);
    const events = new Subject<any>();
    const loadingCauses = signal(new Set<string>());
    const handles: Record<string, BusinessRuleExecutorContentHandle> = {
      field: {
        value: signal('value'),
        events,
      },
    };
    const onExecutionError = jasmine.createSpy();
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
    expect(loadingCauses().has('rule')).toBeTrue();

    events.next({ eventName: 'CHANGE' });
    expect(loadingCauses().has('rule')).toBeTrue();

    secondRequest.error(new Error('failed'));
    expect(loadingCauses().has('rule')).toBeFalse();
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
    const updateContentValue = jasmine.createSpy().and.callFake((referenceId: string, value: any) => {
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
    expect(updateContentValue).toHaveBeenCalledOnceWith('result', 42);
    executor.destroy();
  });
});
