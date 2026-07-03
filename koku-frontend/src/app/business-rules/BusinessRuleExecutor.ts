import { Signal } from '@angular/core';
import { finalize, Observable, Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { get } from '../utils/get';
import { set } from '../utils/set';
import qs from 'qs';
import { ModalService } from '../modal/modal.service';
import { ModalContentSetup } from '../modal/modal.type';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { UNIQUE_REF_GENERATOR } from '../utils/uniqueRef';

interface BusinessRuleExecutorContentEvent {
  eventName: KokuDto.KokuBusinessRuleFieldReferenceListenerEventEnum;
  payload?: any;
}

export interface BusinessRuleExecutorContentHandle {
  value?: Signal<any>;
  events: Observable<BusinessRuleExecutorContentEvent>;
}

export interface BusinessRuleExecutorContentRuntime {
  contentHandle(referenceId: string): BusinessRuleExecutorContentHandle | undefined;
  updateContentValue(referenceId: string, value: any): void;
  updateContentLoading(referenceId: string, cause: string, loading: boolean): void;
}

export interface BusinessRuleExecutorHooks {
  onExecutionError?(error: unknown): void;
}

class BusinessRuleExecutor {
  private readonly loadingAnimationContentIds = new Set<string>();
  private asyncLoadingSubscription: Subscription | undefined;
  private contentEventSubscriptions: Subscription[] = [];

  private readonly uniqueInstanceRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private readonly httpClient: HttpClient,
    private readonly modalService: ModalService,
    private readonly modalSetup: Partial<ModalContentSetup>,
    private readonly config: KokuDto.KokuBusinessRuleDto,
    private readonly contentRuntime: BusinessRuleExecutorContentRuntime,
    private readonly hooks: BusinessRuleExecutorHooks = {},
  ) {
    for (const currentReference of config.references || []) {
      if (!currentReference.reference) {
        throw new Error('Reference not specified');
      }
      const referencedContent = this.contentRuntime.contentHandle(currentReference.reference);
      if (!referencedContent) {
        throw new Error(`Reference not found: ${currentReference.reference}`);
      }
      const listenerEvents = new Set<KokuDto.KokuBusinessRuleFieldReferenceListenerEventEnum>();
      for (const currentListener of currentReference.listeners || []) {
        if (!currentListener.event) {
          throw new Error('Listener not specified');
        }
        listenerEvents.add(currentListener.event);
      }

      if (currentReference.loadingAnimation) {
        this.loadingAnimationContentIds.add(currentReference.reference);
      }

      if (listenerEvents.size > 0) {
        this.contentEventSubscriptions.push(
          referencedContent.events.subscribe((eventDetails) => {
            if (!eventDetails.eventName) {
              throw new Error('Event name is required');
            }
            if (listenerEvents.has(eventDetails.eventName)) {
              this.triggerContentEvent();
            }
          }),
        );
      }
    }
  }

  destroy() {
    this.asyncLoadingSubscription?.unsubscribe();
    for (const subscription of this.contentEventSubscriptions) {
      subscription.unsubscribe();
    }
    this.contentEventSubscriptions = [];
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.uniqueInstanceRef);
  }

  private triggerContentEvent(): void {
    this.assertExecutableRule();
    this.asyncLoadingSubscription?.unsubscribe();
    this.setLoadingAnimation(true);

    const execution = this.config.execution!;
    switch (execution['@type']) {
      case 'open-dialog-content': {
        this.executeOpenDialog(execution as KokuDto.KokuBusinessRuleOpenDialogContentDto);
        break;
      }
      case 'call-http-endpoint': {
        this.executeHttpEndpoint(execution as KokuDto.KokuBusinessRuleCallHttpEndpoint);
        break;
      }
      default: {
        throw new Error('Execution is unknown');
      }
    }
  }

  private assertExecutableRule(): void {
    if (!this.config.execution) {
      throw new Error('Execution is not defined');
    }
    if (!this.config.id) {
      throw new Error('Rule id is not defined');
    }
    if (!this.config.references) {
      throw new Error('References not defined');
    }
  }

  private executeOpenDialog(execution: KokuDto.KokuBusinessRuleOpenDialogContentDto): void {
    if (!execution.content) {
      throw new Error('open-content content is not defined');
    }

    const newModal = this.modalService.add({
      dynamicContent: execution.content,
      urlSegments: {},
      dynamicContentSetup: this.modalSetup,
      fullscreen: true,
      parentRoutePath: '',
      clickOutside: () => {
        newModal.close();
      },
      onCloseRequested: () => {
        newModal.close();
      },
    });

    for (const currentCloseEventListener of execution.closeEventListeners || []) {
      this.registerCloseEventListener(currentCloseEventListener, () => {
        newModal.close();
      });
    }
    this.setLoadingAnimation(false);
  }

  private registerCloseEventListener(
    listener: KokuDto.AbstractKokuBusinessRuleOpenContentCloseListenerDto,
    closeModal: () => void,
  ): void {
    if (listener['@type'] !== 'global-event-listener') {
      throw new Error(`Unknown event listener type ${listener['@type']}`);
    }

    const castedListener = listener as KokuDto.KokuBusinessRuleOpenContentCloseGlobalEventListenerDto;
    if (!castedListener.eventName) {
      throw new Error('Missing eventName in Listener');
    }

    GLOBAL_EVENT_BUS.addGlobalEventListener(this.uniqueInstanceRef, castedListener.eventName, closeModal);
  }

  private executeHttpEndpoint(execution: KokuDto.KokuBusinessRuleCallHttpEndpoint): void {
    if (!execution.method) {
      throw new Error('Call Http Endpoint Method is not defined');
    }
    if (!execution.url) {
      throw new Error('Call Http Endpoint Url is not defined');
    }

    const requestBody = this.buildRequestBody();
    this.asyncLoadingSubscription = this.httpClient
      .request(execution.method, this.buildEndpointUrl(execution, requestBody), {
        body: execution.method === 'GET' ? undefined : requestBody,
      })
      .pipe(finalize(() => this.setLoadingAnimation(false)))
      .subscribe({
        next: (result) => {
          this.afterExecutionFinished(result);
        },
        error: (error) => {
          this.hooks.onExecutionError?.(error);
        },
      });
  }

  private buildRequestBody(): Record<string, any> {
    const requestBody: Record<string, any> = {};
    for (const currentReference of this.config.references || []) {
      if (currentReference.requestParam !== undefined) {
        this.addReferenceValueToRequestBody(requestBody, currentReference);
      }
    }
    return requestBody;
  }

  private addReferenceValueToRequestBody(
    requestBody: Record<string, any>,
    currentReference: KokuDto.KokuBusinessRuleFieldReferenceDto,
  ): void {
    if (!currentReference.reference) {
      throw new Error('Reference not specified');
    }
    if (currentReference.requestParam === undefined) {
      throw new Error('Reference requestParam not specified');
    }

    const referencedContent = this.contentRuntime.contentHandle(currentReference.reference);
    if (!referencedContent) {
      throw new Error(`Reference not found: ${currentReference.reference}`);
    }
    if (!referencedContent.value) {
      throw new Error(`Reference has no value: ${currentReference.reference}`);
    }
    set(requestBody, currentReference.requestParam, referencedContent.value());
  }

  private buildEndpointUrl(
    execution: KokuDto.KokuBusinessRuleCallHttpEndpoint,
    requestBody: Record<string, any>,
  ): string {
    const url = execution.url;
    if (!url) {
      throw new Error('Call Http Endpoint Url is not defined');
    }
    if (execution.method !== 'GET') {
      return url;
    }

    const querySeparator = url.includes('?') ? '&' : '?';
    const query = qs.stringify(requestBody, {
      arrayFormat: 'indices',
      allowDots: true,
    });
    return `${url}${querySeparator}${query}`;
  }

  private afterExecutionFinished(result: any) {
    if (!this.config.id) {
      throw new Error('Rule id is not defined');
    }

    for (const currentReference of this.config.references || []) {
      if (!currentReference.reference) {
        throw new Error('Reference not specified');
      }
      const referencedContent = this.contentRuntime.contentHandle(currentReference.reference);
      if (!referencedContent) {
        throw new Error(`Reference not found: ${currentReference.reference}`);
      }

      if (currentReference.resultUpdateMode === 'ALWAYS') {
        if (!currentReference.resultValuePath) {
          throw new Error('Reference resultValuePath not specified');
        }
        if (!referencedContent.value) {
          throw new Error(`Reference has no value: ${currentReference.reference}`);
        }
        const resultValue = get(result, currentReference.resultValuePath);
        this.contentRuntime.updateContentValue(currentReference.reference, resultValue);
      }
    }

    this.setLoadingAnimation(false);
  }

  private setLoadingAnimation(loading: boolean) {
    if (!this.config.id) {
      throw new Error('Rule id is not defined');
    }

    for (const contentId of this.loadingAnimationContentIds) {
      this.contentRuntime.updateContentLoading(contentId, this.config.id, loading);
    }
  }
}

export { BusinessRuleExecutor };
