import { WritableSignal } from '@angular/core';
import { BehaviorSubject, filter, Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { get } from '../utils/get';
import { set } from '../utils/set';
import qs from 'qs';
import { ModalService } from '../modal/modal.service';
import { ModalContentSetup } from '../modal/modal.type';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { UNIQUE_REF_GENERATOR } from '../utils/uniqueRef';

export interface BusinessRuleExecutorFieldInstance {
  value: WritableSignal<any>;
  fieldEventBus: BehaviorSubject<{
    eventName: KokuDto.KokuBusinessRuleFieldReferenceListenerEventEnum;
    payload?: any;
  } | null>;
  disabledCauses: WritableSignal<Set<string>>;
  requiredCauses: WritableSignal<Set<string>>;
  readonlyCauses: WritableSignal<Set<string>>;
  loadingCauses: WritableSignal<Set<string>>;
}

export type BusinessRuleExecutorFieldInstanceIndex = Record<string, BusinessRuleExecutorFieldInstance>;

class BusinessRuleExecutor {
  private readonly fastListenerRegistry: Partial<
    Record<KokuDto.KokuBusinessRuleFieldReferenceListenerEventEnum, () => void>
  > = {};

  private loadingAnimationFields = new Set<BusinessRuleExecutorFieldInstance>([]);
  private asyncLoadingSubscription: Subscription | undefined;

  private uniqueInstanceRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private httpClient: HttpClient,
    private modalService: ModalService,
    private modalSetup: ModalContentSetup,
    private config: KokuDto.KokuBusinessRuleDto,
    private fieldInstanceIndex: BusinessRuleExecutorFieldInstanceIndex,
  ) {
    console.log('Created BusinessRuleExecutor');

    for (const currentReference of config.references || []) {
      if (!currentReference.reference) {
        throw new Error('Reference not specified');
      }
      const referencedField = this.fieldInstanceIndex[currentReference.reference];
      if (!referencedField) {
        throw new Error(`Reference not found: ${currentReference.reference}`);
      }
      console.log(`BusinessRuleExecutor Registered Field ${currentReference.reference}`);
      for (const currentListener of currentReference.listeners || []) {
        if (!currentListener.event) {
          throw new Error('Listener not specified');
        }
        this.fastListenerRegistry[currentListener.event] = () => {
          this.triggerFieldEvent();
        };
      }

      if (currentReference.loadingAnimation) {
        this.loadingAnimationFields.add(referencedField);
      }

      referencedField.fieldEventBus
        .pipe(
          filter((value) => {
            return value !== null;
          }),
        )
        .subscribe((eventDetails) => {
          if (!eventDetails.eventName) {
            throw new Error('Event name is required');
          }
          console.log(
            `BusinessRuleExecutor Received Event '${eventDetails.eventName}' for Field ${currentReference.reference}`,
            eventDetails.payload,
          );
          const listenerFnLookup = this.fastListenerRegistry[eventDetails.eventName];
          if (listenerFnLookup) {
            listenerFnLookup();
          }
        });
    }
  }

  init() {
    console.log('Initializing BusinessRuleExecutor');
  }

  reinit() {
    console.log('Re-Initializing BusinessRuleExecutor');
  }

  destroy() {
    console.log('Destroying BusinessRuleExecutor');
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.uniqueInstanceRef);
  }

  private triggerFieldEvent(): void {
    if (!this.config.execution) {
      throw new Error('Execution is not defined');
    }
    if (!this.config.id) {
      throw new Error('Rule id is not defined');
    }
    if (!this.config.references) {
      throw new Error('References not defined');
    }

    for (const currentLoadingField of this.loadingAnimationFields) {
      const loadingCauses = currentLoadingField.loadingCauses();
      loadingCauses.add(this.config.id);
      currentLoadingField.loadingCauses.set(new Set(loadingCauses));
    }

    if (this.asyncLoadingSubscription) {
      this.asyncLoadingSubscription.unsubscribe();
    }

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
            const referencedField = this.fieldInstanceIndex[currentReference.reference];
            if (!referencedField) {
              throw new Error(`Reference not found: ${currentReference.reference}`);
            }
            set(requestBody, currentReference.requestParam, referencedField.value());
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
          .subscribe((result) => {
            console.log('Execution finished', result);
            this.afterExecutionFinished(result);
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
      const referencedField = this.fieldInstanceIndex[currentReference.reference];
      if (!referencedField) {
        throw new Error(`Reference not found: ${currentReference.reference}`);
      }

      switch (currentReference.resultUpdateMode) {
        case 'ALWAYS': {
          if (!currentReference.resultValuePath) {
            throw new Error('Reference resultValuePath not specified');
          }
          referencedField.value.set(get(result, currentReference.resultValuePath));
          break;
        }
        default:
          break;
      }
    }

    for (const currentLoadingField of this.loadingAnimationFields) {
      const loadingCauses = currentLoadingField.loadingCauses();
      loadingCauses.delete(this.config.id);
      currentLoadingField.loadingCauses.set(new Set(loadingCauses));
    }
  }
}

export { BusinessRuleExecutor };
