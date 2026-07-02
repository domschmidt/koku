import { inject } from '@angular/core';
import { FORMULAR_PLUGIN, FormularComponent, FormularPlugin } from './formular/formular.component';
import { BusinessRuleExecutor } from './business-rules/BusinessRuleExecutor';
import { Observable, Subscriber, Subscription } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ModalService } from './modal/modal.service';
import { BUSINESS_RULES_CONTENT_SETUP } from './business-rules-binding/registry';
import { UnsavedChangesPreventionGuard } from './navi/UnsavedChangesPreventionGuard';
import { ModalButtonType, ModalType } from './modal/modal.type';
import { GLOBAL_EVENT_BUS } from './events/global-events';
import { get } from './utils/get';
import { UNIQUE_REF_GENERATOR } from './utils/uniqueRef';
import { set } from './utils/set';
import dayjs from 'dayjs';
import dayOfYear from 'dayjs/plugin/dayOfYear';
import { ToastService, ToastTypeUnion } from './toast/toast.service';
import { tap } from 'rxjs/operators';

dayjs.extend(dayOfYear);

export class BusinessRulePlugin implements FormularPlugin {
  private registeredBusinessRuleExecutors: BusinessRuleExecutor[] = [];
  private sourceInitialized = false;

  constructor(
    private readonly httpClient: HttpClient,
    private readonly modalService: ModalService,
    private readonly toastService: ToastService,
    private readonly formularInstance: FormularComponent,
  ) {}

  onFormularLoaded(formularData: KokuDto.FormViewDto): void {
    this.clearBusinessRuleExecutors();

    for (const currentBusinessRule of formularData.businessRules || []) {
      this.registeredBusinessRuleExecutors.push(
        new BusinessRuleExecutor(
          this.httpClient,
          this.modalService,
          BUSINESS_RULES_CONTENT_SETUP.modalContentRegistry,
          currentBusinessRule,
          {
            contentHandle: (referenceId) => this.formularInstance.runtime.contentHandle(referenceId),
            updateContentValue: (referenceId, value) =>
              this.formularInstance.runtime.updateContentValue(referenceId, value),
            updateContentLoading: (referenceId, cause, loading) =>
              this.formularInstance.runtime.updateContentLoading(referenceId, cause, loading),
          },
          {
            onExecutionError: () => {
              this.toastService.add('Business Rule konnte nicht ausgeführt werden', 'error');
            },
          },
        ),
      );
    }
  }

  onSourceLoaded(): void {
    const eventName = this.sourceInitialized ? 'REINIT' : 'INIT';
    for (const contentId of this.formularInstance.runtime.contentIds()) {
      this.formularInstance.runtime.emit(contentId, eventName);
    }
    this.sourceInitialized = true;
  }

  destroy(): void {
    this.clearBusinessRuleExecutors();
  }

  private clearBusinessRuleExecutors() {
    this.sourceInitialized = false;
    for (const currentBusinessRuleCase of this.registeredBusinessRuleExecutors || []) {
      currentBusinessRuleCase.destroy();
    }
    this.registeredBusinessRuleExecutors = [];
  }
}

export class GlobalEventListenerPlugin implements FormularPlugin {
  readonly componentRef = UNIQUE_REF_GENERATOR.generate();

  constructor(private readonly formularInstance: FormularComponent) {}

  onFormularLoaded(formularData: KokuDto.FormViewDto): void {
    this.clearGlobalEventListeners();
    for (const currentEventListener of formularData.globalEventListeners || []) {
      if (!currentEventListener.eventName) {
        throw new Error('Missing eventName in Global Listener Configuration');
      }
      GLOBAL_EVENT_BUS.addGlobalEventListener(
        String(this.componentRef),
        currentEventListener.eventName,
        (eventPayload) => {
          switch (currentEventListener['@type']) {
            case 'source-update-via-payload': {
              const castedEventListener =
                currentEventListener as KokuDto.FormViewEventPayloadSourceUpdateGlobalEventListenerDto;

              if (!castedEventListener.idPath) {
                throw new Error('Missing idPath configuration in EventListener');
              }

              const sourceSnapshot = this.formularInstance.source();
              if (get(eventPayload, castedEventListener.idPath) === get(sourceSnapshot, castedEventListener.idPath)) {
                this.formularInstance.runtime.replaceSource({
                  ...sourceSnapshot,
                  ...eventPayload,
                });
              }
              break;
            }
            case 'field-update-via-payload': {
              const castedEventListener =
                currentEventListener as KokuDto.FormViewEventPayloadFieldUpdateGlobalEventListenerDto;

              for (const [fieldRef, currentMappingDefinition] of Object.entries(
                castedEventListener.configMapping || {},
              )) {
                const registeredContent = this.formularInstance.runtime.content(fieldRef);
                if (!registeredContent) {
                  throw new Error(`Content state not found: ${fieldRef}`);
                }
                switch ((currentMappingDefinition.valueMapping || {})['@type']) {
                  case 'append-list': {
                    const castedMappingDefinition =
                      currentMappingDefinition.valueMapping as KokuDto.ConfigMappingAppendListDto;

                    if (!currentMappingDefinition.targetConfigPath) {
                      throw new Error('Missing targetConfigPath');
                    }

                    const newItem: Record<string, any> = {};

                    for (const currentValueMapping of castedMappingDefinition.valueMapping || []) {
                      switch (currentValueMapping['@type']) {
                        case 'source-path': {
                          const castedValueMapping =
                            currentValueMapping as KokuDto.SourcePathConfigMappingAppendListItemDto;
                          if (!castedValueMapping.sourcePath) {
                            throw new Error('Missing sourcePath');
                          }
                          if (!castedValueMapping.targetPath) {
                            throw new Error('Missing targetPath');
                          }
                          const currentValue = get(eventPayload, castedValueMapping.sourcePath);
                          if (currentValue !== undefined) {
                            set(newItem, castedValueMapping.targetPath, currentValue);
                          }
                          break;
                        }
                        case 'string-transformation': {
                          const castedValueMapping =
                            currentValueMapping as KokuDto.StringTransformationConfigMappingAppendListItemDto;
                          if (!castedValueMapping.targetPath) {
                            throw new Error('Missing targetPath');
                          }
                          let replacedValue = castedValueMapping.transformPattern || '';
                          if (castedValueMapping.transformPattern) {
                            for (const [currentParam, currentParamMapping] of Object.entries(
                              castedValueMapping.transformPatternParameters || {},
                            )) {
                              switch (currentParamMapping['@type']) {
                                case 'source-path': {
                                  const castedParam =
                                    currentParamMapping as KokuDto.StringTransformationSourcePathPatternParam;
                                  if (!castedParam.sourcePath) {
                                    throw new Error('Missing sourcePath');
                                  }
                                  replacedValue = replacedValue.replaceAll(
                                    currentParam,
                                    get(eventPayload, castedParam.sourcePath),
                                  );
                                  break;
                                }
                                default: {
                                  throw new Error(`Unknown Param type ${currentParamMapping['@type']}`);
                                }
                              }
                            }
                            set(newItem, castedValueMapping.targetPath, replacedValue);
                          }
                          set(newItem, castedValueMapping.targetPath, replacedValue);

                          break;
                        }
                        case 'string-conversion': {
                          const castedValueMapping =
                            currentValueMapping as KokuDto.SourcePathConfigMappingAppendListItemDto;
                          if (!castedValueMapping.sourcePath) {
                            throw new Error('Missing sourcePath');
                          }
                          if (!castedValueMapping.targetPath) {
                            throw new Error('Missing targetPath');
                          }
                          const currentValue = get(eventPayload, castedValueMapping.sourcePath);
                          if (currentValue !== undefined) {
                            set(newItem, castedValueMapping.targetPath, String(currentValue));
                          }
                          break;
                        }
                        case 'static-value': {
                          const castedValueMapping =
                            currentValueMapping as KokuDto.StaticValueConfigMappingAppendListItemDto;
                          if (!castedValueMapping.targetPath) {
                            throw new Error('Missing targetPath');
                          }
                          if (castedValueMapping.value !== undefined) {
                            set(newItem, castedValueMapping.targetPath, castedValueMapping.value);
                          }
                          break;
                        }
                        default: {
                          throw new Error(`Unknown value Mapping type ${currentValueMapping['@type']}`);
                        }
                      }
                    }

                    const registeredFieldConfig = { ...registeredContent };
                    const currentArray = [...(get(registeredContent, currentMappingDefinition.targetConfigPath) || [])];
                    currentArray.push(newItem);
                    set(registeredFieldConfig, currentMappingDefinition.targetConfigPath, currentArray);

                    this.formularInstance.runtime.updateContentConfig(fieldRef, () => registeredFieldConfig);

                    break;
                  }
                }
              }

              for (const [currentFieldId, reference] of Object.entries(castedEventListener.fieldValueMapping || {})) {
                if (!reference) {
                  throw new Error('Unexpected reference');
                }

                const content = this.formularInstance.runtime.content(currentFieldId);
                const contentValue = this.formularInstance.runtime.contentHandle(currentFieldId)?.value;
                if (!content || !contentValue) {
                  throw new Error(`Content value state not found: ${currentFieldId}`);
                }
                let newValue;
                switch (reference['@type']) {
                  case 'field-reference': {
                    const castedReference = reference as KokuDto.FormViewFieldReferenceValueMapping;
                    if (!castedReference) {
                      throw new Error('Unexpected reference');
                    }
                    if (!castedReference.source) {
                      throw new Error('Unexpected reference source');
                    }

                    switch (castedReference.source['@type']) {
                      case 'source-path': {
                        const castedReferenceSource =
                          castedReference.source as KokuDto.FormViewEventPayloadSourcePathFieldUpdateValueSourceDto;
                        if (!castedReferenceSource.sourcePath) {
                          throw new Error('Unexpected reference sourcePath');
                        }
                        newValue = get(eventPayload, castedReferenceSource.sourcePath);
                        break;
                      }
                      case 'static-value': {
                        const castedReferenceSource =
                          castedReference.source as KokuDto.FormViewEventPayloadStaticValueFieldUpdateValueSourceDto;
                        newValue = castedReferenceSource.value;
                        break;
                      }
                      default: {
                        throw new Error('unexpected reference');
                      }
                    }

                    break;
                  }
                  case 'append-list': {
                    const castedReference = reference as KokuDto.FormViewFieldReferenceMultiSelectValueMapping;

                    let newValueItem: Record<string, any> = {};
                    for (const [currentMappingTarget, currentMappingSource] of Object.entries(
                      castedReference.targetPathMapping || {},
                    )) {
                      let currentValue;
                      switch (currentMappingSource['@type']) {
                        case 'source-path': {
                          const castedMappingSource =
                            currentMappingSource as KokuDto.FormViewEventPayloadSourcePathFieldUpdateValueSourceDto;
                          if (!castedMappingSource.sourcePath) {
                            throw new Error('Unexpected reference source path');
                          }
                          currentValue = get(eventPayload, castedMappingSource.sourcePath);
                          break;
                        }
                        case 'static-value': {
                          const castedMappingSource =
                            currentMappingSource as KokuDto.FormViewEventPayloadStaticValueFieldUpdateValueSourceDto;
                          currentValue = castedMappingSource.value;
                          break;
                        }
                        default: {
                          throw new Error(`Unknown mapping source type ${currentMappingSource['@type']}`);
                        }
                      }
                      if (currentValue !== undefined) {
                        if (currentMappingTarget !== undefined) {
                          newValueItem[currentMappingTarget] = currentValue;
                        } else {
                          newValueItem = currentValue;
                        }
                      }
                    }
                    newValue = [...contentValue(), newValueItem];

                    break;
                  }
                  default: {
                    throw new Error('Unexpected reference');
                  }
                }
                if (newValue !== undefined) {
                  this.formularInstance.runtime.updateContentValue(currentFieldId, newValue);
                  this.formularInstance.runtime.emit(currentFieldId, 'INPUT', newValue);
                  this.formularInstance.runtime.emit(currentFieldId, 'CHANGE', newValue);
                }
              }

              break;
            }
            default: {
              throw new Error(`Unknown EventListenerType ${currentEventListener['@type']}`);
            }
          }
        },
      );
    }
  }

  destroy(): void {
    this.clearGlobalEventListeners();
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }
}

export class ButtonListenerPlugin implements FormularPlugin {
  buttonSubscriptions: Subscription[] = [];

  constructor(
    private modalService: ModalService,
    private toastService: ToastService,
    private formularInstance: FormularComponent,
  ) {}

  onFormularLoaded(formularData: KokuDto.FormViewDto): void {
    this.clearButtonSubscriptions();
    for (const configuredContent of Object.values(formularData.contents ?? {})) {
      if (configuredContent['@type'] !== 'button') {
        continue;
      }
      if (!configuredContent.id) {
        throw new Error(`missing button id for config ${configuredContent}`);
      }
      const contentId = configuredContent.id;
      const handle = this.formularInstance.runtime.contentHandle(contentId);
      if (!handle) {
        throw new Error(`Button handle not found: ${contentId}`);
      }

      this.buttonSubscriptions.push(
        handle.events.subscribe(({ eventName }) => {
          const content = this.formularInstance.runtime.content(contentId) ?? configuredContent;
          const buttonCfg = content;
          if (content['@type'] === 'button') {
            const castedButtonCfg = buttonCfg as KokuDto.KokuFormButton;

            const executeEvents = (events: KokuDto.AbstractFormEventDto[], eventPayload?: any) => {
              for (const currentEvent of events || []) {
                switch (currentEvent['@type']) {
                  case 'notification': {
                    const castedEvent = currentEvent as KokuDto.FormNotificationEvent;
                    if (!castedEvent.text) {
                      throw new Error('Missing text in Notification');
                    }
                    let notificationText = castedEvent.text;
                    for (const currentParam of castedEvent.params || []) {
                      if (!currentParam.param) {
                        throw new Error(`Missing param`);
                      }
                      switch (currentParam['@type']) {
                        case 'value': {
                          const castedParam = currentParam as KokuDto.FormNotificationEventValueParamDto;
                          if (!castedParam.sourcePath) {
                            throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                          }
                          notificationText = notificationText.replaceAll(
                            currentParam.param,
                            get(this.formularInstance.source(), castedParam.sourcePath),
                          );
                          break;
                        }
                        case 'date-value': {
                          const castedParam = currentParam as KokuDto.FormNotificationEventValueParamDto;
                          if (!castedParam.sourcePath) {
                            throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                          }
                          notificationText = notificationText.replaceAll(
                            currentParam.param,
                            get(this.formularInstance.source(), castedParam.sourcePath),
                          );
                          break;
                        }
                        default: {
                          throw new Error(`Unknown param type ${currentParam['@type']}`);
                        }
                      }
                    }
                    let serenity: ToastTypeUnion = 'info';
                    if (castedEvent.serenity) {
                      switch (castedEvent.serenity) {
                        case 'SUCCESS': {
                          serenity = 'success';
                          break;
                        }
                        case 'ERROR': {
                          serenity = 'error';
                          break;
                        }
                        default:
                          throw new Error(`Unknown Notification serenity ${serenity}`);
                      }
                    }
                    this.toastService.add(notificationText, serenity);
                    break;
                  }
                  case 'propagate-global-event': {
                    const castedEvent = currentEvent as KokuDto.FormPropagateGlobalEventDto;
                    if (!castedEvent.eventName) {
                      throw new Error('Missing eventName');
                    }
                    GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, eventPayload);
                    break;
                  }
                  default: {
                    throw new Error(`Unknown event type ${currentEvent}`);
                  }
                }
              }
            };

            const executeUserConfirmation = (cb: () => Observable<any>) => {
              if (castedButtonCfg.userConfirmation) {
                let headline = castedButtonCfg.userConfirmation.headline || '';
                let content = castedButtonCfg.userConfirmation.content || '';

                for (const currentParam of castedButtonCfg.userConfirmation.params || []) {
                  if (!currentParam.param) {
                    throw new Error(`Missing param`);
                  }
                  if (currentParam['@type'] !== 'source-path') {
                    throw new Error(`Unknown param type ${currentParam['@type']}`);
                  }
                  const castedParam = currentParam as KokuDto.FormButtonUserConfirmationSourcePathParamDto;
                  if (!castedParam.sourcePath) {
                    throw new Error('Missing valuePath in FieldReference');
                  }
                  headline = headline.replaceAll(
                    currentParam.param,
                    get(this.formularInstance.source(), castedParam.sourcePath),
                  );
                  content = content.replaceAll(
                    currentParam.param,
                    get(this.formularInstance.source(), castedParam.sourcePath),
                  );
                }

                const confirmationModal = this.modalService.add({
                  headline: headline,
                  content: content,
                  buttons: [
                    {
                      text: 'Abbrechen',
                      styles: ['OUTLINE'],
                      onClick: () => {
                        confirmationModal.close();
                      },
                    },
                    {
                      text: 'Bestätigen',
                      onClick: (event, modal, button) => {
                        button.loading = true;
                        button.disabled = true;

                        cb().subscribe({
                          next: () => {
                            button.loading = false;
                            button.disabled = false;
                            confirmationModal.close();
                          },
                          error: () => {
                            button.loading = false;
                            button.disabled = false;
                            confirmationModal.update(modal);
                          },
                          complete: () => {
                            confirmationModal.close();
                          },
                        });
                      },
                    },
                  ],
                  clickOutside: () => {
                    confirmationModal.close();
                  },
                });
              } else {
                cb().subscribe({
                  error: () => undefined,
                });
              }
            };

            switch (eventName) {
              case 'CLICK': {
                if (castedButtonCfg.buttonType === 'SUBMIT') {
                  executeUserConfirmation(() => {
                    return this.formularInstance.submit(castedButtonCfg.submitPayload).pipe(
                      tap(
                        (rawValue) => {
                          for (const currentPostProcessingAction of castedButtonCfg.postProcessingActions || []) {
                            if (currentPostProcessingAction['@type'] !== 'reload') {
                              throw new Error('Unknown PostProcessingAction');
                            }
                            this.formularInstance.loadSource();
                          }
                          executeEvents(castedButtonCfg.successEvents || [], rawValue);
                        },
                        () => {
                          executeEvents(castedButtonCfg.failEvents || []);
                        },
                      ),
                    );
                  });
                }
                break;
              }
            }
          }
        }),
      );
    }
  }

  destroy(): void {
    this.clearButtonSubscriptions();
  }

  private clearButtonSubscriptions() {
    for (const buttonSubscription of this.buttonSubscriptions) {
      buttonSubscription.unsubscribe();
    }
    this.buttonSubscriptions = [];
  }
}

export class UnsavedChangesPreventionGuardPlugin implements FormularPlugin {
  private unsavedChangesPreventionGuardInitialized = false;

  constructor(
    private unsavedChangesPreventionGuard: UnsavedChangesPreventionGuard<any>,
    private modalService: ModalService,
    private formularInstance: FormularComponent,
  ) {}

  destroy(): void {
    this.unsavedChangesPreventionGuard.unregisterUnsavedChangesPrevention(this);
  }

  onSourceLoaded(): void {
    if (!this.unsavedChangesPreventionGuardInitialized) {
      this.unsavedChangesPreventionGuardInitialized = true;
      this.unsavedChangesPreventionGuard.registerUnsavedChangesPrevention(this, () => {
        return new Observable<boolean>((observer) => {
          if (this.formularInstance.dirty()) {
            const confirmationModal = this.modalService.add({
              headline: 'Ungespeicherte Änderungen',
              content: 'Es gibt ungespeicherte Änderungen. Willst du trotzdem fortfahren?',
              buttons: [
                {
                  buttonType: 'BUTTON',
                  title: 'Jetzt abbrechen',
                  text: 'Abbruch',
                  onClick: () => {
                    this.modalService.close(confirmationModal);
                    observer.next(false);
                    observer.complete();
                  },
                },
                {
                  buttonType: 'BUTTON',
                  title: 'Trotzdem jetzt fortfahren',
                  text: 'Fortfahren',
                  onClick: () => {
                    this.modalService.close(confirmationModal);
                    this.formularInstance.dirty.set(false);
                    observer.next(true);
                    observer.complete();
                  },
                },
              ],
              clickOutside: () => {
                this.modalService.close(confirmationModal);
                observer.next(false);
                observer.complete();
              },
            });
          } else {
            observer.next(true);
            observer.complete();
          }
        });
      });
    }
  }
}

export class BusinessExceptionPlugin implements FormularPlugin {
  constructor(
    private readonly modalService: ModalService,
    private readonly formularInstance: FormularComponent,
  ) {}

  onSubmitError(
    error: HttpErrorResponse,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
  ): boolean {
    if (error.error && error.error['@type'] === 'business-error-with-confirmation-message') {
      const castedError = error.error as KokuDto.KokuBusinessErrorWithConfirmationMessageDto;
      const buttons: ModalButtonType[] = [];
      for (const buttonCfg of castedError.buttons || []) {
        if (buttonCfg['@type'] === 'close-button') {
          buttons.push({
            loading: buttonCfg.loading,
            disabled: buttonCfg.disabled,
            buttonType: 'BUTTON',
            title: buttonCfg.title,
            icon: buttonCfg.icon,
            text: buttonCfg.text,
            styles: buttonCfg.styles,
            size: buttonCfg.size,
            onClick: () => {
              this.modalService.close(confirmationModal);
              request.complete();
            },
          });
          continue;
        }
        if (buttonCfg['@type'] === 'send-to-different-endpoint-button') {
          const castedButtonCfg = buttonCfg as KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;

          buttons.push({
            loading: castedButtonCfg.loading,
            disabled: castedButtonCfg.disabled,
            buttonType: 'BUTTON',
            title: castedButtonCfg.title,
            icon: castedButtonCfg.icon,
            text: castedButtonCfg.text,
            styles: castedButtonCfg.styles,
            size: castedButtonCfg.size,
            onClick: (event: Event, modal: ModalType, button: ModalButtonType) => {
              if (castedButtonCfg.showLoadingAnimation) {
                button.loading = true;
              }
              if (castedButtonCfg.showDisabledState) {
                button.disabled = true;
              }

              this.formularInstance
                .requestSubmit(
                  castedButtonCfg.endpointMethod || submitMethod,
                  castedButtonCfg.endpointUrl || submitUrl,
                  submitData,
                )
                .subscribe({
                  next: (response) => {
                    this.modalService.close(confirmationModal);
                    request.next(response);
                    request.complete();
                  },
                  error: () => {
                    if (castedButtonCfg.showLoadingAnimation) {
                      button.loading = false;
                    }
                    if (castedButtonCfg.showDisabledState) {
                      button.disabled = false;
                    }
                  },
                  complete: () => {
                    this.modalService.close(confirmationModal);
                    request.complete();
                  },
                });
            },
          });
          continue;
        }
        throw new Error('Unknown button type');
      }

      const confirmationModal = this.modalService.add({
        headline: castedError.headline,
        content: castedError.confirmationMessage,
        buttons: buttons,
        clickOutside: () => {
          if (castedError.closeOnClickOutside) {
            this.modalService.close(confirmationModal);
            request.complete();
          }
        },
      });
      return true;
    }

    return false;
  }
}
export const FORMULAR_PLUGIN_PROVIDERS = [
  {
    provide: FORMULAR_PLUGIN,
    useFactory: (): ((formularInstance: FormularComponent) => FormularPlugin) => (formularInstance) =>
      new BusinessRulePlugin(inject(HttpClient), inject(ModalService), inject(ToastService), formularInstance),
    multi: true,
  },
  {
    provide: FORMULAR_PLUGIN,
    useFactory: (): ((formularInstance: FormularComponent) => FormularPlugin) => (formularInstance) =>
      new ButtonListenerPlugin(inject(ModalService), inject(ToastService), formularInstance),
    multi: true,
  },
  {
    provide: FORMULAR_PLUGIN,
    useFactory: (): ((formularInstance: FormularComponent) => FormularPlugin) => (formularInstance) =>
      new UnsavedChangesPreventionGuardPlugin(
        inject(UnsavedChangesPreventionGuard),
        inject(ModalService),
        formularInstance,
      ),
    multi: true,
  },
  {
    provide: FORMULAR_PLUGIN,
    useFactory: (): ((formularInstance: FormularComponent) => FormularPlugin) => (formularInstance) =>
      new BusinessExceptionPlugin(inject(ModalService), formularInstance),
    multi: true,
  },
  {
    provide: FORMULAR_PLUGIN,
    useFactory: (): ((formularInstance: FormularComponent) => FormularPlugin) => (formularInstance) =>
      new GlobalEventListenerPlugin(formularInstance),
    multi: true,
  },
];
