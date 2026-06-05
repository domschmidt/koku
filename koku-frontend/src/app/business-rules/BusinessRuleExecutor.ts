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
  private loadingAnimationContentIds = new Set<string>();
  private asyncLoadingSubscription: Subscription | undefined;
  private contentEventSubscriptions: Subscription[] = [];

  private uniqueInstanceRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private httpClient: HttpClient,
    private modalService: ModalService,
    private modalSetup: Partial<ModalContentSetup>,
    private config: KokuDto.KokuBusinessRuleDto,
    private contentRuntime: BusinessRuleExecutorContentRuntime,
    private hooks: BusinessRuleExecutorHooks = {},
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
    if (!this.config.execution) {
      throw new Error('Execution is not defined');
    }
    if (!this.config.id) {
      throw new Error('Rule id is not defined');
    }
    if (!this.config.references) {
      throw new Error('References not defined');
    }

    if (this.asyncLoadingSubscription) {
      this.asyncLoadingSubscription.unsubscribe();
    }
    this.setLoadingAnimation(true);

    switch (this.config.execution['@type']) {
      case 'open-dialog-content': {
        const castedExecution = this.config.execution as KokuDto.KokuBusinessRuleOpenDialogContentDto;

        if (!castedExecution.content) {
          throw new Error('open-content content is not defined');
        }

        const newModal = this.modalService.add({
          dynamicContent: castedExecution.content,
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

        for (const currentCloseEventListener of castedExecution.closeEventListeners || []) {
          switch (currentCloseEventListener['@type']) {
            case 'global-event-listener': {
              const castedCloseEventListener =
                currentCloseEventListener as KokuDto.KokuBusinessRuleOpenContentCloseGlobalEventListenerDto;

              if (!castedCloseEventListener.eventName) {
                throw new Error('Missing eventName in Listener');
              }

              GLOBAL_EVENT_BUS.addGlobalEventListener(
                this.uniqueInstanceRef,
                castedCloseEventListener.eventName,
                () => {
                  newModal.close();
                },
              );
              break;
            }
            default: {
              throw new Error(`Unknown event listener type ${currentCloseEventListener['@type']}`);
            }
          }
        }
        this.setLoadingAnimation(false);
        break;
      }
      case 'call-http-endpoint': {
        const castedExecution = this.config.execution as KokuDto.KokuBusinessRuleCallHttpEndpoint;

        if (!castedExecution.method) {
          throw new Error('Call Http Endpoint Method is not defined');
        }
        if (!castedExecution.url) {
          throw new Error('Call Http Endpoint Url is not defined');
        }

        const requestBody = {};
        for (const currentReference of this.config.references) {
          if (!currentReference.reference) {
            throw new Error('Reference not specified');
          }
          if (currentReference.requestParam !== undefined) {
            const referencedContent = this.contentRuntime.contentHandle(currentReference.reference);
            if (!referencedContent) {
              throw new Error(`Reference not found: ${currentReference.reference}`);
            }
            if (!referencedContent.value) {
              throw new Error(`Reference has no value: ${currentReference.reference}`);
            }
            set(requestBody, currentReference.requestParam, referencedContent.value());
          }
        }

        this.asyncLoadingSubscription = this.httpClient
          .request(
            castedExecution.method,
            castedExecution.url +
              (castedExecution.method === 'GET'
                ? (castedExecution.url.indexOf('?') === -1 ? '?' : '&') +
                  qs.stringify(requestBody, {
                    arrayFormat: 'indices',
                    allowDots: true,
                  })
                : ''),
            {
              body: castedExecution.method === 'GET' ? undefined : requestBody,
            },
          )
          .pipe(finalize(() => this.setLoadingAnimation(false)))
          .subscribe({
            next: (result) => {
              this.afterExecutionFinished(result);
            },
            error: (error) => {
              this.hooks.onExecutionError?.(error);
            },
          });

        break;
      }
      default: {
        throw new Error('Execution is unknown');
      }
    }
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

      switch (currentReference.resultUpdateMode) {
        case 'ALWAYS': {
          if (!currentReference.resultValuePath) {
            throw new Error('Reference resultValuePath not specified');
          }
          if (!referencedContent.value) {
            throw new Error(`Reference has no value: ${currentReference.reference}`);
          }
          const resultValue = get(result, currentReference.resultValuePath);
          this.contentRuntime.updateContentValue(currentReference.reference, resultValue);
          break;
        }
        default:
          break;
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
