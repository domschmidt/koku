import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {ThemingService} from './theme/theming.service';
import {ToastComponent} from './toast/toast.component';
import {ModalComponent} from './modal/modal.component';
import {FieldEvent, FORMULAR_PLUGIN, FormularComponent, FormularPlugin} from './formular/formular.component';
import {BusinessRuleExecutor, BusinessRuleExecutorFieldInstanceIndex} from './business-rules/BusinessRuleExecutor';
import {BehaviorSubject, filter, Observable, Subscriber, Subscription} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {ModalService} from './modal/modal.service';
import {BUSINESS_RULES_CONTENT_SETUP} from './business-rules-binding/registry';
import {UnsavedChangesPreventionGuard} from './navi/UnsavedChangesPreventionGuard';
import {ModalButtonType, ModalType, RenderedModalType} from './modal/modal.type';
import {GLOBAL_EVENT_BUS} from './events/global-events';
import {get} from './utils/get';
import {UNIQUE_REF_GENERATOR} from './utils/uniqueRef';
import {set} from './utils/set';
import Holidays, {HolidaysTypes} from 'date-holidays';
import {addMinutes, differenceInMinutes, format, formatISO, getDayOfYear, parseISO} from 'date-fns';
import {
  CALENDAR_PLUGIN,
  CalendarComponent,
  CalendarContext,
  CalendarPlugin,
  CalendarPluginEventSourceFactory,
  CalendarPluginInstanceDetails,
  DateSelection,
  RenderedCalendarInlineItem
} from './calendar/calendar.component';
import {ToastService} from './toast/toast.service';
import {catchError} from 'rxjs/operators';
import {EventInput, EventSourceInput} from '@fullcalendar/core';
import {Frequency, Options} from 'rrule/dist/esm/types';
import {LIST_CONTENT_SETUP} from './list-binding/registry';
import {
  CalendarInlineHeaderContainerComponent
} from './calendar-binding/header-container/calendar-inline-header-container.component';
import {
  CalendarInlineListContainerComponent
} from './calendar-binding/list-container/calendar-inline-list-container.component';
import {CALENDAR_CONTENT_SETUP} from './calendar-binding/registry';

class BusinessRulePlugin implements FormularPlugin {
  private businessRuleExecutorsInitialized = false;
  private sourceInitialInitialization = true;
  private businessRuleEventNameMapping: Partial<Record<FieldEvent, KokuDto.KokuBusinessRuleFieldReferenceListenerEventEnum>> = {
    onChange: 'CHANGE',
    onInput: 'INPUT',
    onBlur: 'BLUR',
    onFocus: 'FOCUS',
    onInit: 'INIT',
    onClickAppendOuter: 'CLICK_APPEND_OUTER',
    onBlurAppendOuter: 'BLUR_APPEND_OUTER',
    onFocusAppendOuter: 'FOCUS_APPEND_OUTER',
    onClickAppendInner: 'CLICK_APPEND_INNER',
    onBlurAppendInner: 'BLUR_APPEND_INNER',
    onFocusAppendInner: 'FOCUS_APPEND_INNER',
    onClickPrependInner: 'CLICK_PREPEND_INNER',
    onBlurPrependInner: 'BLUR_PREPEND_INNER',
    onFocusPrependInner: 'FOCUS_PREPEND_INNER',
    onClickPrependOuter: 'CLICK_PREPEND_OUTER',
    onBlurPrependOuter: 'BLUR_PREPEND_OUTER',
    onFocusPrependOuter: 'FOCUS_PREPEND_OUTER',
  };
  private registeredBusinessRuleExecutors: BusinessRuleExecutor[] = [];

  constructor(
    private httpClient: HttpClient,
    private modalService: ModalService,
    private formularInstance: FormularComponent,
  ) {
  }

  onFormularLoaded(formularData: KokuDto.FormViewDto): void {
    if (!this.businessRuleExecutorsInitialized) {
      this.businessRuleExecutorsInitialized = true;
      for (const currentBusinessRule of formularData.businessRules || []) {
        const businessRuleFields: BusinessRuleExecutorFieldInstanceIndex = {}
        for (const currentReference of currentBusinessRule.references || []) {
          if (!currentReference.reference) {
            throw new Error('Unexpected reference');
          }
          const lookupField = this.formularInstance.fieldRegister()[currentReference.reference];
          if (!lookupField) {
            throw new Error('Field reference not found');
          }

          const businessRuleEventBus = new BehaviorSubject<{
            eventName: KokuDto.KokuBusinessRuleFieldReferenceListenerEventEnum,
            payload?: any
          } | null>(null);
          lookupField.fieldEventBus
            .subscribe(value => {
                const mappedEvent = this.businessRuleEventNameMapping[value.eventName];
                if (mappedEvent !== undefined) {
                  businessRuleEventBus.next({
                    payload: value?.payload,
                    eventName: mappedEvent,
                  });
                }
              }
            );
          businessRuleFields[currentReference.reference] = {
            value: lookupField.value,
            fieldEventBus: businessRuleEventBus,
            disabledCauses: lookupField.disabledCauses,
            requiredCauses: lookupField.requiredCauses,
            readonlyCauses: lookupField.readonlyCauses,
            loadingCauses: lookupField.loadingCauses,
          }
        }
        this.registeredBusinessRuleExecutors.push(new BusinessRuleExecutor(
          this.httpClient,
          this.modalService,
          BUSINESS_RULES_CONTENT_SETUP.modalContentRegistry,
          currentBusinessRule,
          businessRuleFields
        ));
      }
    }
  }

  onSourceLoaded(source: any): void {
    if (this.sourceInitialInitialization) {
      this.sourceInitialInitialization = false;
      for (const currentBusinessRuleCase of this.registeredBusinessRuleExecutors || []) {
        currentBusinessRuleCase.init();
      }
    } else {
      for (const currentBusinessRuleCase of this.registeredBusinessRuleExecutors || []) {
        currentBusinessRuleCase.reinit();
      }
    }
  }

  destroy(): void {
    for (const currentBusinessRuleCase of this.registeredBusinessRuleExecutors || []) {
      currentBusinessRuleCase.destroy();
    }
  }

}

class GlobalEventListenerPlugin implements FormularPlugin {

  componentRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private formularInstance: FormularComponent,
  ) {
  }

  onFormularLoaded(formularData: KokuDto.FormViewDto): void {
    this.clearGlobalEventListeners();
    for (const currentEventListener of formularData.globalEventListeners || []) {
      if (!currentEventListener.eventName) {
        throw new Error('Missing eventName in Global Listener Configuration');
      }
      GLOBAL_EVENT_BUS.addGlobalEventListener(String(this.componentRef), currentEventListener.eventName, (eventPayload) => {
        switch (currentEventListener['@type']) {
          case "field-update-via-payload": {
            const castedEventListener = currentEventListener as KokuDto.FormViewEventPayloadFieldUpdateGlobalEventListenerDto;

            for (const [fieldRef, currentMappingDefinition] of Object.entries(castedEventListener.configMapping || {})) {

              const registeredField = this.formularInstance.fieldRegister()[fieldRef];
              switch ((currentMappingDefinition.valueMapping || {})['@type']) {
                case "append-list": {
                  const castedMappingDefinition = currentMappingDefinition.valueMapping as KokuDto.ConfigMappingAppendListDto;

                  if (!currentMappingDefinition.targetConfigPath) {
                    throw new Error('Missing targetConfigPath');
                  }

                  const newItem: { [key: string]: any } = {};

                  for (const currentValueMapping of castedMappingDefinition.valueMapping || []) {
                    switch (currentValueMapping['@type']) {
                      case "source-path": {
                        const castedValueMapping = currentValueMapping as KokuDto.SourcePathConfigMappingAppendListItemDto;
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
                      case "string-transformation": {
                        const castedValueMapping = currentValueMapping as KokuDto.StringTransformationConfigMappingAppendListItemDto;
                        if (!castedValueMapping.targetPath) {
                          throw new Error('Missing targetPath');
                        }
                        let replacedValue = castedValueMapping.transformPattern || '';
                        if (castedValueMapping.transformPattern) {
                          for (const [currentParam, currentParamMapping] of Object.entries(castedValueMapping.transformPatternParameters || {})) {
                            switch (currentParamMapping['@type']) {
                              case "source-path": {
                                const castedParam = currentParamMapping as KokuDto.StringTransformationSourcePathPatternParam;
                                if (!castedParam.sourcePath) {
                                  throw new Error('Missing sourcePath');
                                }
                                replacedValue = replacedValue.replaceAll(currentParam, get(eventPayload, castedParam.sourcePath));
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
                      case "string-conversion": {
                        const castedValueMapping = currentValueMapping as KokuDto.SourcePathConfigMappingAppendListItemDto;
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
                      case "static-value": {
                        const castedValueMapping = currentValueMapping as KokuDto.StaticValueConfigMappingAppendListItemDto;
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

                  const registeredFieldConfig = {...registeredField.config};
                  const currentArray = [...(get(registeredField.config, currentMappingDefinition.targetConfigPath) || [])];
                  currentArray.push(newItem);
                  set(registeredFieldConfig, currentMappingDefinition.targetConfigPath, currentArray);

                  this.formularInstance.fieldRegister.set({
                    ...this.formularInstance.fieldRegister(),
                    [fieldRef]: {
                      ...registeredField,
                      config: registeredFieldConfig
                    }
                  });

                  break;
                }


              }
            }

            const fieldRegisterSnapshot = this.formularInstance.fieldRegister();
            for (const [currentFieldId, reference] of Object.entries(castedEventListener.fieldValueMapping || {})) {

              if (!reference) {
                throw new Error('Unexpected reference');
              }

              const fieldRegisterField = fieldRegisterSnapshot[currentFieldId];
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
                    case "source-path": {
                      const castedReferenceSource = castedReference.source as KokuDto.FormViewEventPayloadSourcePathFieldUpdateValueSourceDto;
                      if (!castedReferenceSource.sourcePath) {
                        throw new Error('Unexpected reference sourcePath');
                      }
                      newValue = get(eventPayload, castedReferenceSource.sourcePath);
                      break;
                    }
                    case "static-value": {
                      const castedReferenceSource = castedReference.source as KokuDto.FormViewEventPayloadStaticValueFieldUpdateValueSourceDto;
                      newValue = castedReferenceSource.value;
                      break;
                    }
                    default: {
                      throw new Error("unexpected reference");
                    }
                  }

                  break;
                }
                case 'append-list': {
                  const castedReference = reference as KokuDto.FormViewFieldReferenceMultiSelectValueMapping;

                  let newValueItem: { [key: string]: any } = {};
                  for (const [currentMappingTarget, currentMappingSource] of Object.entries(castedReference.targetPathMapping || {})) {

                    let currentValue;
                    switch (currentMappingSource['@type']) {
                      case "source-path": {
                        const castedMappingSource = currentMappingSource as KokuDto.FormViewEventPayloadSourcePathFieldUpdateValueSourceDto;
                        if (!castedMappingSource.sourcePath) {
                          throw new Error('Unexpected reference source path');
                        }
                        currentValue = get(eventPayload, castedMappingSource.sourcePath);
                        break;
                      }
                      case "static-value": {
                        const castedMappingSource = currentMappingSource as KokuDto.FormViewEventPayloadStaticValueFieldUpdateValueSourceDto;
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
                  newValue = [...fieldRegisterField.value(), newValueItem];

                  break;
                }
                default: {
                  throw new Error('Unexpected reference');
                }
              }
              if (newValue !== undefined) {
                fieldRegisterField.fieldEventBus.next({
                  eventName: 'onInput',
                  payload: newValue,
                })
                fieldRegisterField.fieldEventBus.next({
                  eventName: 'onChange',
                  payload: newValue,
                })
              }

            }

            break;
          }
          default: {
            throw new Error(`Unknown EventListenerType ${currentEventListener['@type']}`);
          }
        }
      });
    }
  }

  destroy(): void {
    this.clearGlobalEventListeners();
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }

}

class UnsavedChangesPreventionGuardPlugin implements FormularPlugin {

  private unsavedChangesPreventionGuardInitialized = false;

  constructor(
    private unsavedChangesPreventionGuard: UnsavedChangesPreventionGuard<any>,
    private modalService: ModalService,
    private formularInstance: FormularComponent,
  ) {
  }

  destroy(): void {
    this.unsavedChangesPreventionGuard.unregisterUnsavedChangesPrevention(this);
  }

  onSourceLoaded(source: any): void {
    if (!this.unsavedChangesPreventionGuardInitialized) {
      this.unsavedChangesPreventionGuardInitialized = true;
      this.unsavedChangesPreventionGuard.registerUnsavedChangesPrevention(this, () => {
        return new Observable<boolean>(observer => {
          if (this.formularInstance.dirty()) {
            const confirmationModal = this.modalService.add({
              headline: "Ungespeicherte Änderungen",
              content: "Es gibt ungespeicherte Änderungen. Willst du trotzdem fortfahren?",
              buttons: [{
                buttonType: "BUTTON",
                title: "Jetzt abbrechen",
                text: "Abbruch",
                onClick: (event: Event, modal: ModalType, button: ModalButtonType) => {
                  this.modalService.close(confirmationModal);
                  observer.next(false);
                  observer.complete();
                }
              }, {
                buttonType: "BUTTON",
                title: "Trotzdem jetzt fortfahren",
                text: "Fortfahren",
                onClick: (event: Event, modal: ModalType, button: ModalButtonType) => {
                  this.modalService.close(confirmationModal);
                  this.formularInstance.dirty.set(false);
                  observer.next(true);
                  observer.complete();
                }
              }],
              clickOutside: () => {
                this.modalService.close(confirmationModal);
                observer.next(false);
                observer.complete();
              }
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

class BusinessExceptionPlugin implements FormularPlugin {

  constructor(
    private modalService: ModalService,
    private formularInstance: FormularComponent,
  ) {
  }

  onSubmitError(error: HttpErrorResponse, request: Subscriber<any>, submitMethod: string, submitUrl: string, submitData: any): boolean {
    if (error.error && error.error["@type"] === "business-exception-with-confirmation-message") {
      const castedError = error.error as KokuDto.KokuBusinessExceptionWithConfirmationMessageDto;
      const buttons: ModalButtonType[] = [];
      for (const buttonCfg of castedError.buttons || []) {
        switch (buttonCfg["@type"]) {
          case "close-button":
            buttons.push({
              loading: buttonCfg.loading,
              disabled: buttonCfg.disabled,
              buttonType: "BUTTON",
              title: buttonCfg.title,
              icon: buttonCfg.icon,
              text: buttonCfg.text,
              styles: buttonCfg.styles,
              size: buttonCfg.size,
              onClick: (event: Event, modal: ModalType, button: ModalButtonType) => {
                this.modalService.close(confirmationModal);
                request.complete();
              }
            });
            break;
          case "send-to-different-endpoint-button":
            const castedButtonCfg = buttonCfg as KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;

            buttons.push({
              loading: castedButtonCfg.loading,
              disabled: castedButtonCfg.disabled,
              buttonType: "BUTTON",
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

                this.formularInstance.requestSubmit(
                  castedButtonCfg.endpointMethod || submitMethod,
                  castedButtonCfg.endpointUrl || submitUrl,
                  submitData
                ).subscribe({
                  next: (response) => {
                    this.modalService.close(confirmationModal);
                    request.next(response);
                    request.complete();
                  },
                  error: (error: HttpErrorResponse) => {
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
                  }
                });
              }
            });
            break;
          default:
            throw new Error('Unknown button type');
        }
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
        }
      });
      return true;
    }

    return false;
  }

}

class CalendarInteractionPlugin implements CalendarPlugin {

  constructor(
    private calendarInstance: CalendarComponent,
  ) {
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarInteractionPlugin'
    };
  }

  onDateSelect(dateClickInfo: DateSelection): void {
    const calendarConfig = this.calendarInstance.config();
    if (calendarConfig !== undefined) {
      const clickAction = calendarConfig.calendarClickAction;
      if (clickAction) {
        switch (clickAction['@type']) {
          case "open-routed-content": {
            const castedClickAction = clickAction as KokuDto.CalendarOpenRoutedContentClickActionDto;
            if (castedClickAction.route) {
              let context: CalendarContext | undefined = undefined;
              if (dateClickInfo && dateClickInfo.selectionStart) {
                context = {
                  selectionStartDate: format(dateClickInfo.selectionStart, 'yyyy-MM-dd'),
                  selectionStartTime: format(dateClickInfo.selectionStart, 'HH:mm'),
                  selectionStartDateTime: format(dateClickInfo.selectionStart, "yyyy-MM-dd'T'HH:mm"),
                  selectionEndDate: format(dateClickInfo.selectionEnd, 'yyyy-MM-dd'),
                  selectionEndTime: format(dateClickInfo.selectionEnd, 'HH:mm'),
                  selectionEndDateTime: format(dateClickInfo.selectionEnd, "yyyy-MM-dd'T'HH:mm"),
                };
              }
              this.calendarInstance.openRoutedContent(castedClickAction.route.split('/'), context);
            }
            break;
          }
          default: {
            throw new Error(`Unknown ClickAction Type ${clickAction['@type']}`);
          }
        }
      }
    }
  }
}


class CalendarActionPlugin implements CalendarPlugin {

  constructor(
    private calendarInstance: CalendarComponent,
  ) {
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarActionPlugin',
    };
  }

  onCalendarActionClicked(action: KokuDto.AbstractCalendarActionDto): void {
    switch (action['@type']) {
      case "open-routed-content": {
        const castedActionType = action as KokuDto.CalendarOpenRoutedContentActionDto;
        if (castedActionType.route) {
          this.calendarInstance.openRoutedContent(castedActionType.route.split('/'));
        }
        break;
      }
    }
  }

}

export interface CalendarUserSelectActionPluginApi {
  selectedUserDetails: BehaviorSubject<KokuDto.KokuUserDto | null>;
}

class CalendarUserSelectActionPlugin implements CalendarPlugin {

  private selectedUserDetails = new BehaviorSubject<KokuDto.KokuUserDto | null>(null);
  private selectedUserSubscription: Subscription | undefined;
  componentRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private httpClient: HttpClient,
    private toastService: ToastService,
    private modalService: ModalService,
  ) {
    this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/@self').subscribe((userResult) => {
      this.selectedUserDetails.next(userResult);
    }, () => {
      this.toastService.add("Fehler beim Laden der Nutzerinformationen", 'error');
    });
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarUserSelectActionPlugin',
      api: {
        selectedUserDetails: this.selectedUserDetails
      } as CalendarUserSelectActionPluginApi
    };
  }

  destroy(): void {
    if (this.selectedUserSubscription) {
      this.selectedUserSubscription.unsubscribe();
    }
  }

  onCalendarActionClicked(action: KokuDto.AbstractCalendarActionDto): void {
    switch (action['@type']) {
      case "select-user": {
        const castedActionType = action as KokuDto.CalendarUserSelectionActionDto;

        const newModal = this.modalService.add({
          dynamicContent: {
            '@type': 'header',
            title: 'Bedienung',
            content: {
              '@type': 'list',
              listUrl: '/services/users/users/list?selectMode=true',
              sourceUrl: '/services/users/users/query',
              contentSetup: LIST_CONTENT_SETUP,
              parentRoutePath: '/calendar'
            },
          },
          urlSegments: {},
          dynamicContentSetup: {
            "header": {
              componentType: CalendarInlineHeaderContainerComponent,
              inputBindings(instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarHeaderInlineContentDto): {
                [key: string]: any
              } {
                let segmentMapping: { [key: string]: string } = {...(modal.urlSegments || {})};
                let sourceUrl = undefined;
                if (inlineContent.sourceUrl) {
                  const mappedSourceParts: string[] = [];
                  for (const currentRoutePathToMatch of inlineContent.sourceUrl.split("/")) {
                    if (currentRoutePathToMatch.indexOf(":") === 0) {
                      mappedSourceParts.push(segmentMapping[currentRoutePathToMatch]);
                    } else {
                      mappedSourceParts.push(currentRoutePathToMatch);
                    }
                  }
                  sourceUrl = mappedSourceParts.join("/");
                }

                return {
                  'content': inlineContent,
                  'sourceUrl': sourceUrl,
                  'titlePath': inlineContent.titlePath,
                  'title': inlineContent.title,
                  'contentSetup': CALENDAR_CONTENT_SETUP,
                  'urlSegments': modal.urlSegments,
                  'parentRoutePath': modal.parentRoutePath,
                }
              },
              outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarHeaderInlineContentDto) => {
                return {
                  onClose: () => {
                    if (modal.onCloseRequested) {
                      modal.onCloseRequested();
                    } else {
                      modal.close();
                    }
                  },
                }
              }
            },
            "list": {
              componentType: CalendarInlineListContainerComponent,
              inputBindings(instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarListInlineContentDto): {
                [key: string]: any
              } {
                let listUrl = inlineContent.listUrl || '';
                for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
                  listUrl = listUrl.replace(segment, value);
                }
                let sourceUrl = inlineContent.sourceUrl || '';
                for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
                  sourceUrl = sourceUrl.replace(segment, value);
                }

                return {
                  'title': 'Termine',
                  'listUrl': listUrl,
                  'sourceUrl': sourceUrl,
                  'contentSetup': LIST_CONTENT_SETUP,
                  'urlSegments': modal.urlSegments,
                  'parentRoutePath': modal.parentRoutePath,
                }
              },
              outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarListInlineContentDto) => {
                return {
                  // onClose: () => {
                  //   instance.closeInlineContent()
                  // },
                  // onOpenRoutedContent: (routes: string[]) => {
                  //   instance.openRoutedContent(routes)
                  // },
                }
              }
            },
          },
          fullscreen: true,
          maxWidthInPx: 799,
          parentRoutePath: '',
          clickOutside: (event) => {
            newModal.close();
          },
          onCloseRequested: () => {
            newModal.close();
          }
        });
        GLOBAL_EVENT_BUS.addGlobalEventListener(this.componentRef, 'user-selected', (payload) => {
          this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/' + payload.id).subscribe((userResult) => {
            this.selectedUserDetails.next(userResult);
            newModal.close();
          }, (err) => {
            this.toastService.add("Fehler beim Laden der Nutzerinformationen", 'error');
          });
        });
        break;
      }
    }
  }

  initCalendarAction(currentCalendarAction: KokuDto.AbstractCalendarActionDto, updateCb: (updatedAction: KokuDto.AbstractCalendarActionDto) => void): void {
    switch (currentCalendarAction['@type']) {
      case "select-user": {
        updateCb({
          ...currentCalendarAction,
          loading: true
        });

        if (this.selectedUserSubscription) {
          this.selectedUserSubscription.unsubscribe();
        }
        this.selectedUserSubscription = this.selectedUserDetails.pipe(filter(value => value !== null)).subscribe((selectedUserDetails) => {
          updateCb({
            ...currentCalendarAction,
            loading: false,
            imgBase64: selectedUserDetails.avatarBase64
          });
        });

        break;
      }
    }
  }

}

class CalendarGlobalEventPlugin implements CalendarPlugin {

  componentRef = UNIQUE_REF_GENERATOR.generate();
  componentRefInlineContent = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private calendarInstance: CalendarComponent,
  ) {
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarGlobalEventPlugin'
    };
  }

  destroy(): void {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRefInlineContent);
  }

  onRoutedInlineContentOpened(routedContent: KokuDto.CalendarRoutedContentDto): void {
    for (const currentListener of routedContent.globalEventListeners || []) {
      if (!currentListener.eventName) {
        throw new Error("Missing eventName in Listener Config");
      }
      GLOBAL_EVENT_BUS.addGlobalEventListener(this.componentRefInlineContent, currentListener.eventName, () => {
        switch (currentListener['@type']) {
          case 'close': {
            this.calendarInstance.closeInlineContent().subscribe();
            break;
          }
          default: {
            throw new Error(`Unexpected listener Type: ${currentListener['@type']}`)
          }
        }
      });
    }
  }

  onRoutedInlineContentClose(content: RenderedCalendarInlineItem | null): void {
    if (content !== null && content.id !== null) {
      GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRefInlineContent);
    }
  }

  afterConfigLoaded(config: KokuDto.CalendarConfigDto): void {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
    for (const currentEventListener of config.globalEventListeners || []) {
      if (!currentEventListener.eventName) {
        throw new Error('Missing eventName in Global Listener Configuration');
      }
      GLOBAL_EVENT_BUS.addGlobalEventListener(String(this.componentRef), currentEventListener.eventName, (eventPayload) => {
        switch (currentEventListener['@type']) {
          case 'refresh': {
            this.calendarInstance.calendarComponent()?.getApi().refetchEvents();
            break;
          }
          case 'replace-via-payload': {
            const castedEventListener = currentEventListener as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto;

            if (castedEventListener.sourceId === undefined) {
              throw new Error(`Missing sourceId`);
            }

            const eventSource = this.calendarInstance.calendarComponent()?.getApi().getEventSourceById(castedEventListener.sourceId)
            if (!eventSource) {
              throw new Error(`eventSource ${castedEventListener.sourceId} cannot be resolved`);
            }
            const calendarSourceFactory = this.calendarInstance.registeredEventSourceFactories[castedEventListener.sourceId];
            if (!calendarSourceFactory) {
              throw new Error(`EventSourceFactory ${castedEventListener.sourceId} missing`);
            }

            let lookedUpEvent = calendarSourceFactory.lookupEvent(eventPayload);
            const newEvent = calendarSourceFactory.generateEventItem(eventPayload);

            if (!lookedUpEvent) {
              lookedUpEvent = this.calendarInstance.calendarComponent()?.getApi().addEvent(newEvent, eventSource);
            } else {
              if (newEvent.allDay !== undefined && newEvent.allDay !== !!lookedUpEvent.allDay) {
                lookedUpEvent.setAllDay(newEvent.allDay);
              }
              if (newEvent.start !== undefined && newEvent.end !== undefined) {
                lookedUpEvent.setDates(newEvent.start, newEvent.end);
              } else {
                if (newEvent.start !== undefined) {
                  lookedUpEvent.setStart(newEvent.start, {maintainDuration: true});
                }
                if (newEvent.end !== undefined) {
                  lookedUpEvent.setEnd(newEvent.end, {maintainDuration: true});
                }
              }
              lookedUpEvent.setProp('title', newEvent.title);
              lookedUpEvent.setProp('display', newEvent.display);
              lookedUpEvent.setProp('rrule', newEvent.rrule);
              lookedUpEvent.setProp('className', newEvent.className);
              lookedUpEvent.setExtendedProp('item', eventPayload);
            }
            if (lookedUpEvent) {
              const classnameSnapshot = [...lookedUpEvent.classNames];
              setTimeout(() => {
                classnameSnapshot.splice(classnameSnapshot.indexOf('calendar-item--flash'), 1);
                lookedUpEvent.setProp('classNames', classnameSnapshot);
              }, 1000);
              classnameSnapshot.push('calendar-item--flash');
              lookedUpEvent.setProp('classNames', classnameSnapshot);
            }

            break;
          }
          default: {
            throw new Error(`Unknown event Listener Type: ${currentEventListener['@type']}`);
          }
        }
      });
    }
  }

}

class CalendarListSourcePlugin implements CalendarPlugin {

  private userDetailsSubscriptions: {[key: string]: Subscription} = {};

  constructor(
    private calendarInstance: CalendarComponent,
    private httpClient: HttpClient,
    private toastService: ToastService,
    private modalService: ModalService,
  ) {
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarListSourcePlugin'
    };
  }

  destroy(): void {
    for (const currentSubscription of Object.values(this.userDetailsSubscriptions)) {
      currentSubscription.unsubscribe();
    }
  }

  private callHttpEndpount(method: "POST" | "PUT" | "GET" | "DELETE", url: string, requestBody: {}) {
    return this.httpClient.request(method, url, {
      body: requestBody
    }).pipe(
      catchError((error) => {
        return new Observable(
          (subscriber) => {
            if (error.error && error.error["@type"] === "business-exception-with-confirmation-message") {
              const castedError = error.error as KokuDto.KokuBusinessExceptionWithConfirmationMessageDto;
              const buttons: ModalButtonType[] = [];
              for (const buttonCfg of castedError.buttons || []) {
                switch (buttonCfg["@type"]) {
                  case "close-button":
                    buttons.push({
                      loading: buttonCfg.loading,
                      disabled: buttonCfg.disabled,
                      buttonType: "BUTTON",
                      title: buttonCfg.title,
                      icon: buttonCfg.icon,
                      text: buttonCfg.text,
                      styles: buttonCfg.styles,
                      size: buttonCfg.size,
                      onClick: (event: Event, modal: ModalType, button: ModalButtonType) => {
                        this.modalService.close(confirmationModal);
                        subscriber.error(error);
                      }
                    });
                    break;
                  case "send-to-different-endpoint-button":
                    const castedButtonCfg = buttonCfg as KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto;

                    buttons.push({
                      loading: castedButtonCfg.loading,
                      disabled: castedButtonCfg.disabled,
                      buttonType: "BUTTON",
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

                        this.callHttpEndpount(
                          castedButtonCfg.endpointMethod || method,
                          castedButtonCfg.endpointUrl || url,
                          requestBody
                        ).subscribe({
                          next: (args) => {
                            this.modalService.close(confirmationModal);
                            subscriber.next(args);
                            subscriber.complete();
                          },
                          error: (args) => {
                            if (castedButtonCfg.showLoadingAnimation) {
                              button.loading = false;
                            }
                            if (castedButtonCfg.showDisabledState) {
                              button.disabled = false;
                            }
                            subscriber.error(args);
                          }
                        });
                      }
                    });
                    break;
                  default:
                    throw new Error('Unknown button type');
                }
              }

              const confirmationModal = this.modalService.add({
                headline: castedError.headline,
                content: castedError.confirmationMessage,
                buttons: buttons,
                clickOutside: () => {
                  if (castedError.closeOnClickOutside) {
                    this.modalService.close(confirmationModal);
                    subscriber.complete();
                  }
                }
              });
            }
          }
        );
      })
    );
  }

  provideEventSourceFactory(currentSource: KokuDto.AbstractCalendarListSourceConfigDto): CalendarPluginEventSourceFactory | void {
    if (currentSource && currentSource['@type'] === 'list') {
      const castedSource = currentSource as KokuDto.CalendarListSourceConfigDto;

      const generateEventItem = (item: {
        [key: string]: any
      }): EventInput => {
        let rrule: Partial<Options> | undefined = undefined;
        if (castedSource.searchOperatorHint === 'YEARLY_RECURRING') {
          rrule = {
            freq: Frequency.YEARLY,
            dtstart: get(item, castedSource.startDateFieldSelectionPath || '')
          }
        }
        let prefix = '';
        if (castedSource.sourceItemText) {
          prefix = castedSource.sourceItemText + '\n';
        }
        const text = `${prefix}${get(item, castedSource.displayTextFieldSelectionPath || '')}`;
        if (castedSource.startDateFieldSelectionPath === undefined) {
          throw new Error(`Missing startDateFieldSelectionPath`);
        }
        const start = [
          get(item, castedSource.startDateFieldSelectionPath)
        ];
        if (castedSource.startTimeFieldSelectionPath !== undefined) {
          start.push(get(item, castedSource.startTimeFieldSelectionPath));
        }
        if (castedSource.endDateFieldSelectionPath === undefined) {
          throw new Error(`Missing endDateFieldSelectionPath`);
        }
        const end = [
          get(item, castedSource.endDateFieldSelectionPath)
        ];
        if (castedSource.endTimeFieldSelectionPath !== undefined) {
          end.push(get(item, castedSource.endTimeFieldSelectionPath));
        }

        const date1 = parseISO(start.join('T'));
        const date2 = parseISO(end.join('T'));
        let calculatedEndDate
        const diffMinutes = differenceInMinutes(date2, date1);
        if (diffMinutes < 60) {
          calculatedEndDate = addMinutes(date1, 60);
        } else {
          calculatedEndDate = date2;
        }

        return {
          id: `${castedSource.id}/${get(item, castedSource.idPath || '')}`,
          title: text,
          display: text,
          start: start.join('T'),
          end: calculatedEndDate,
          rrule: rrule,
          allDay: !(castedSource.startTimeFieldSelectionPath && castedSource.endTimeFieldSelectionPath),
          className: `calendar-item calendar-item--color-${castedSource.sourceItemColor}`,
          item: item,
          editable: castedSource.editable !== false,
          onClickHandler: (event: {
            item: { [key: string]: any }
          }) => {
            if (castedSource.clickAction) {
              switch (castedSource.clickAction['@type']) {
                case 'open-routed-content': {
                  const castedActionType = castedSource.clickAction as KokuDto.CalendarOpenRoutedContentItemClickAction;
                  let url = castedActionType.route || '';
                  for (const currentParam of castedActionType.params || []) {
                    if (!currentParam.param) {
                      throw new Error(`Missing param`);
                    }
                    switch (currentParam['@type']) {
                      case "item-value": {
                        const castedCurrentParam = currentParam as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto;
                        if (castedCurrentParam.valuePath === undefined) {
                          throw new Error(`Missing valuePath`);
                        }
                        url = url.replaceAll(currentParam.param, String(get({
                          ...event.item,
                        }, castedCurrentParam.valuePath)));
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`)
                      }
                    }
                  }

                  if (castedActionType.route) {
                    this.calendarInstance.openRoutedContent(url.split("/"));
                  }
                  break;
                }
                default: {
                  throw new Error(`Unknown item click action type ${castedSource.clickAction['@type']}`)
                }
              }
            }
          },
          onDropHandler: (event: {
            item: { [key: string]: any },
            newStart: Date,
            newEnd: Date,
            allDay: boolean,
            revert: () => void,
            setLoading: (loading: boolean) => void,
          }) => {
            let dropHandled = false;
            if (castedSource.dropAction) {
              switch (castedSource.dropAction['@type']) {
                case 'call-http': {
                  const castedActionType = castedSource.dropAction as KokuDto.CalendarCallHttpItemClickAction;
                  let url = castedActionType.url || '';
                  for (const currentParam of castedActionType.urlParams || []) {
                    if (!currentParam.param) {
                      throw new Error(`Missing param`);
                    }
                    switch (currentParam['@type']) {
                      case "item-value": {
                        const castedCurrentParam = currentParam as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto;
                        if (castedCurrentParam.valuePath === undefined) {
                          throw new Error(`Missing valuePath`);
                        }
                        url = url.replaceAll(currentParam.param, String(get({
                          ...event.item,
                        }, castedCurrentParam.valuePath)));
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`)
                      }
                    }
                  }
                  if (!castedActionType.method) {
                    throw new Error(`Missing method`);
                  }
                  if (!castedActionType.startDatePath) {
                    throw new Error(`Missing startDatePath`);
                  }
                  if (!castedActionType.startTimePath) {
                    throw new Error(`Missing startTimePath`);
                  }

                  const requestBody = {};
                  set(requestBody, castedActionType.startDatePath, format(event.newStart, 'yyyy-MM-dd'));
                  set(requestBody, castedActionType.startTimePath, format(event.newStart, 'HH:mm'));
                  if (castedActionType.endDatePath) {
                    set(requestBody, castedActionType.endDatePath, format(event.newEnd, 'yyyy-MM-dd'));
                  }
                  if (castedActionType.endTimePath) {
                    set(requestBody, castedActionType.endTimePath, format(event.newEnd, 'HH:mm'));
                  }

                  for (const [source, target] of Object.entries(castedActionType.valueMapping || {})) {
                    set(requestBody, target, get(event.item, source, null));
                  }

                  event.setLoading(true);
                  this.callHttpEndpount(
                    castedActionType.method,
                    url,
                    requestBody
                  ).subscribe({
                    next: (response) => {
                      event.setLoading(false);
                      for (const currentEvent of castedActionType.successEvents || []) {
                        switch (currentEvent['@type']) {
                          case "propagate-global-event": {
                            const castedEvent = currentEvent as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto;
                            if (!castedEvent.eventName) {
                              throw new Error(`Missing eventName`);
                            }
                            GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, response);
                            break;
                          }
                          default: {
                            throw new Error(`Unknown event type ${currentEvent['@type']}`)
                          }
                        }
                      }
                      this.toastService.add(`Erfolgreich gespeichert`, 'success');
                    },
                    error: () => {
                      event.setLoading(false);
                      event.revert();
                      this.toastService.add(`Es ist ein Fehler bei der Anfrage aufgetreten. Versuche es später erneut!`, 'error');
                    }
                  })
                  dropHandled = true;
                  break;
                }
                default: {
                  console.log(`Unknown item drop action type ${castedSource.dropAction['@type']}`);
                  break;
                }
              }
            }
            if (!dropHandled) {
              event.revert();
            }
          },
          onResizeHandler: (event: {
            item: { [key: string]: any },
            newEnd: Date,
            allDay: boolean,
            revert: () => void,
            setLoading: (loading: boolean) => void,
          }) => {
            let resizeHandled = false;

            if (castedSource.resizeAction) {
              switch (castedSource.resizeAction['@type']) {
                case 'call-http': {
                  const castedActionType = castedSource.resizeAction as KokuDto.CalendarCallHttpItemResizeAction;
                  let url = castedActionType.url || '';
                  for (const currentParam of castedActionType.urlParams || []) {
                    if (!currentParam.param) {
                      throw new Error(`Missing param`);
                    }
                    switch (currentParam['@type']) {
                      case "item-value": {
                        const castedCurrentParam = currentParam as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto;
                        if (castedCurrentParam.valuePath === undefined) {
                          throw new Error(`Missing valuePath`);
                        }
                        url = url.replaceAll(currentParam.param, String(get({
                          ...event.item,
                        }, castedCurrentParam.valuePath)));
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`)
                      }
                    }
                  }
                  if (!castedActionType.method) {
                    throw new Error(`Missing method`);
                  }
                  if (!castedActionType.endDatePath) {
                    throw new Error(`Missing endDatePath`);
                  }
                  if (!castedActionType.endTimePath) {
                    throw new Error(`Missing endTimePath`);
                  }

                  const requestBody = {};
                  set(requestBody, castedActionType.endDatePath, format(event.newEnd, 'yyyy-MM-dd'));
                  set(requestBody, castedActionType.endTimePath, format(event.newEnd, 'HH:mm'));

                  for (const [source, target] of Object.entries(castedActionType.valueMapping || {})) {
                    set(requestBody, target, get(event.item, source, null));
                  }

                  event.setLoading(true);
                  this.callHttpEndpount(
                    castedActionType.method,
                    url,
                    requestBody
                  ).subscribe({
                    next: (response) => {
                      event.setLoading(false);
                      for (const currentEvent of castedActionType.successEvents || []) {
                        switch (currentEvent['@type']) {
                          case "propagate-global-event": {
                            const castedEvent = currentEvent as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto;
                            if (!castedEvent.eventName) {
                              throw new Error(`Missing eventName`);
                            }
                            GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, response);
                            break;
                          }
                          default: {
                            throw new Error(`Unknown event type ${currentEvent['@type']}`)
                          }
                        }
                      }
                      this.toastService.add(`Erfolgreich gespeichert`, 'success');
                    },
                    error: () => {
                      event.setLoading(false);
                      event.revert();
                      this.toastService.add(`Es ist ein Fehler bei der Anfrage aufgetreten. Versuche es später erneut!`, 'error');
                    }
                  })
                  resizeHandled = true;
                  break;
                }
                default: {
                  console.log(`Unknown item drop action type ${castedSource.resizeAction['@type']}`);
                  resizeHandled = false;
                  break;
                }
              }
            }

            if (!resizeHandled) {
              event.revert();
            }
          },
        };

      }

      const generateEventSource = (): EventSourceInput => {
        return {
          id: castedSource.id,
          events: (arg, successCallback, failureCallback) => {
            const loadList = (
              fieldSelection: Set<string>,
              fieldPredicates: { [index: string]: KokuDto.ListFieldQuery }
            ) => {
              const query: KokuDto.ListQuery = {
                fieldSelection: [...fieldSelection],
                limit: 100,
                page: 0,
                fieldPredicates: fieldPredicates
              }

              if (castedSource.sourceUrl) {
                this.httpClient.post<KokuDto.ListPage>(castedSource.sourceUrl, query)
                  .subscribe((result) => {
                    const results: EventInput[] = [];

                    for (const currentListItem of result.results || []) {
                      results.push(generateEventItem(currentListItem.values || {}));
                    }

                    successCallback(results);
                  }, (err) => {
                    failureCallback(err);
                  });
              }
            }

            const fieldSelection: Set<string> = new Set<string>();
            const fieldPredicates: { [index: string]: KokuDto.ListFieldQuery } = {};

            let startAndEndDOYOrGroupIdentifier = getDayOfYear(arg.start) > getDayOfYear(arg.end) ? 'startGTend' : undefined;

            if (castedSource.startDateFieldSelectionPath) {
              fieldSelection.add(castedSource.startDateFieldSelectionPath);
              fieldPredicates[castedSource.startDateFieldSelectionPath] = {
                predicates: [
                  {
                    searchExpression: formatISO(arg.start, {representation: 'date'}),
                    searchOperator: 'GREATER_OR_EQ',
                    searchOperatorHint: castedSource.searchOperatorHint,
                    orGroupIdentifier: startAndEndDOYOrGroupIdentifier
                  },
                  ...((fieldPredicates[castedSource.startDateFieldSelectionPath] || {}).predicates || [])
                ]
              }
            }
            if (castedSource.endDateFieldSelectionPath) {
              fieldSelection.add(castedSource.endDateFieldSelectionPath);
              fieldPredicates[castedSource.endDateFieldSelectionPath] = {
                predicates: [
                  {
                    searchExpression: formatISO(arg.endStr, {representation: 'date'}),
                    searchOperator: 'LESS_OR_EQ',
                    searchOperatorHint: castedSource.searchOperatorHint,
                    orGroupIdentifier: startAndEndDOYOrGroupIdentifier
                  },
                  ...((fieldPredicates[castedSource.endDateFieldSelectionPath] || {}).predicates || [])
                ]
              }
            }
            if (castedSource.startTimeFieldSelectionPath) {
              fieldSelection.add(castedSource.startTimeFieldSelectionPath);
            }
            if (castedSource.endTimeFieldSelectionPath) {
              fieldSelection.add(castedSource.endTimeFieldSelectionPath);
            }
            if (castedSource.displayTextFieldSelectionPath) {
              fieldSelection.add(castedSource.displayTextFieldSelectionPath);
            }
            if (castedSource.deletedFieldSelectionPath) {
              fieldSelection.add(castedSource.deletedFieldSelectionPath);
              fieldPredicates[castedSource.deletedFieldSelectionPath] = {
                predicates: [
                  {
                    searchExpression: 'TRUE',
                    searchOperator: 'EQ',
                    negate: true
                  },
                  ...((fieldPredicates[castedSource.deletedFieldSelectionPath] || {}).predicates || [])
                ]
              }
            }
            for (const currentAdditionalFieldSelectionPath of castedSource.additionalFieldSelectionPaths || []) {
              fieldSelection.add(currentAdditionalFieldSelectionPath);
            }
            const userIdFieldSelectionPath = castedSource.userIdFieldSelectionPath;
            if (userIdFieldSelectionPath) {
              fieldSelection.add(userIdFieldSelectionPath);

              const calendarActionPluginApi = this.calendarInstance.getPluginApi('CalendarUserSelectActionPlugin') as CalendarUserSelectActionPluginApi;

              let firstCall = true;
              const sourceId = castedSource.id;
              if (sourceId === undefined) {
                throw new Error('Expected source id');
              }
              const oldSubscription = this.userDetailsSubscriptions[sourceId];
              if (oldSubscription) {
                oldSubscription.unsubscribe();
              }
              this.userDetailsSubscriptions[sourceId] = calendarActionPluginApi.selectedUserDetails.pipe(filter(value => value !== null)).subscribe((userDetails) => {
                if (firstCall) {
                  if (userDetails.id === undefined) {
                    throw new Error('user id required');
                  }
                  fieldPredicates[userIdFieldSelectionPath] = {
                    predicates: [
                      {
                        searchExpression: String(userDetails.id),
                        searchOperator: 'EQ',
                      },
                      ...((fieldPredicates[userIdFieldSelectionPath] || {}).predicates || [])
                    ]
                  }
                  loadList(fieldSelection, fieldPredicates);
                } else {
                  this.calendarInstance.calendarComponent()?.getApi().getEventSourceById(sourceId)?.refetch();
                }
                firstCall = false;
              });
            } else {
              loadList(fieldSelection, fieldPredicates);
            }
          }
        };
      };
      const lookupEvent = (eventPayload: any): any => {
        if (castedSource.idPath === undefined) {
          throw new Error(`Missing idPath`);
        }
        const eventId = get(eventPayload, castedSource.idPath);
        if (eventId === undefined) {
          throw new Error(`${castedSource.idPath} cannot be resolved`);
        }

        return this.calendarInstance.calendarComponent()?.getApi().getEventById(`${castedSource.id}/${eventId}`);
      };

      return {
        generateEventSource,
        generateEventItem,
        lookupEvent
      }
    }
  }

}

class CalendarHolidaySourcePlugin implements CalendarPlugin {

  constructor(
    private calendarInstance: CalendarComponent,
    private httpClient: HttpClient,
  ) {
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarHolidaySourcePlugin'
    };
  }

  private userRegion: KokuDto.KokuUserRegionDto | null = null;

  provideEventSourceFactory(currentSource: KokuDto.AbstractCalendarListSourceConfigDto): CalendarPluginEventSourceFactory | void {
    if (currentSource && currentSource['@type'] === 'holiday') {
      const castedSource = currentSource as KokuDto.CalendarHolidaySourceConfigDto;

      const generateEventItem = (item: HolidaysTypes.Holiday): EventInput => {

        return {
          id: `${castedSource.id}/${item.start}`,
          title: item.name,
          start: item.start,
          end: item.end,
          allDay: true,
          editable: false,
          display: 'background',
          className: `calendar-item calendar-item--color-${castedSource.sourceItemColor}`,
          item: item,
        };

      }

      const generateEventSource = (): EventSourceInput => {
        return {
          id: castedSource.id,
          events: (arg, successCallback, failureCallback) => {
            const afterRegionLoaded = (regionResult: KokuDto.KokuUserRegionDto) => {
              const results: EventInput[] = [];

              if (regionResult.country) {
                let holidays;
                if (regionResult.state) {
                  holidays = new Holidays(regionResult.country, regionResult.state);
                } else {
                  holidays = new Holidays(regionResult.country);
                }

                const rangeOfYears = (startYear: number, endYear: number) => {
                  return Array<number>(endYear - startYear + 1)
                    .fill(startYear)
                    .map((year, index) => year + index);
                };
                const years = rangeOfYears(arg.start.getFullYear(), arg.end.getFullYear());

                for (const currentYear of years) {
                  for (const holiday of holidays.getHolidays(currentYear)) {
                    results.push(generateEventItem(holiday));
                  }
                }
              }
              successCallback(results);
            }

            if (this.userRegion) {
              afterRegionLoaded(this.userRegion);
            } else {
              this.httpClient.get<KokuDto.KokuUserRegionDto>('/services/users/users/@self/region').subscribe((regionResult) => {
                this.userRegion = regionResult;
                afterRegionLoaded(regionResult);
              }, (err) => {
                failureCallback(err);
              });
            }
          }
        };
      };
      const lookupEvent = (eventPayload: HolidaysTypes.Holiday): any => {
        return this.calendarInstance.calendarComponent()?.getApi().getEventById(`${castedSource.id}/${eventPayload.start}`);
      };

      return {
        generateEventSource,
        generateEventItem,
        lookupEvent
      }
    }
  }

}

@Component({
  selector: 'koku-root',
  imports: [
    RouterOutlet,
    ToastComponent,
    ModalComponent,
  ],
  providers: [
    ThemingService,
    {
      provide: FORMULAR_PLUGIN,
      useFactory: (): (formularInstance: FormularComponent) => FormularPlugin => (formularInstance) => {
        const httpClient = inject(HttpClient);
        const modalService = inject(ModalService);
        return new BusinessRulePlugin(httpClient, modalService, formularInstance);
      },
      multi: true,
    },
    {
      provide: FORMULAR_PLUGIN,
      useFactory: (): (formularInstance: FormularComponent) => FormularPlugin => (formularInstance) => {
        const unsavedChangesPreventionGuard = inject(UnsavedChangesPreventionGuard);
        const modalService = inject(ModalService);
        return new UnsavedChangesPreventionGuardPlugin(unsavedChangesPreventionGuard, modalService, formularInstance);
      },
      multi: true,
    },
    {
      provide: FORMULAR_PLUGIN,
      useFactory: (): (formularInstance: FormularComponent) => FormularPlugin => (formularInstance) => {
        const modalService = inject(ModalService);
        return new BusinessExceptionPlugin(modalService, formularInstance);
      },
      multi: true,
    },
    {
      provide: FORMULAR_PLUGIN,
      useFactory: (): (formularInstance: FormularComponent) => FormularPlugin => (formularInstance) => {
        return new GlobalEventListenerPlugin(formularInstance);
      },
      multi: true,
    },
    {
      provide: CALENDAR_PLUGIN,
      useFactory: (): (calendarInstance: CalendarComponent) => CalendarPlugin => (calendarInstance) => {
        return new CalendarInteractionPlugin(calendarInstance);
      },
      multi: true,
    },
    {
      provide: CALENDAR_PLUGIN,
      useFactory: (): (calendarInstance: CalendarComponent) => CalendarPlugin => (calendarInstance) => {
        return new CalendarGlobalEventPlugin(calendarInstance);
      },
      multi: true,
    },
    {
      provide: CALENDAR_PLUGIN,
      useFactory: (): (calendarInstance: CalendarComponent) => CalendarPlugin => (calendarInstance) => {
        return new CalendarActionPlugin(calendarInstance);
      },
      multi: true,
    },
    {
      provide: CALENDAR_PLUGIN,
      useFactory: (): (calendarInstance: CalendarComponent) => CalendarPlugin => () => {
        return new CalendarUserSelectActionPlugin(inject(HttpClient), inject(ToastService), inject(ModalService));
      },
      multi: true,
    },
    {
      provide: CALENDAR_PLUGIN,
      useFactory: (): (calendarInstance: CalendarComponent) => CalendarPlugin => (calendarInstance) => {
        return new CalendarListSourcePlugin(calendarInstance, inject(HttpClient), inject(ToastService), inject(ModalService));
      },
      multi: true,
    },
    {
      provide: CALENDAR_PLUGIN,
      useFactory: (): (calendarInstance: CalendarComponent) => CalendarPlugin => (calendarInstance) => {
        return new CalendarHolidaySourcePlugin(calendarInstance, inject(HttpClient));
      },
      multi: true,
    },
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  private readonly themingService = inject(ThemingService);

  theme: string;

  constructor() {
    const themingService = this.themingService;

    this.themingService.theme.subscribe((theme: string) => {
      this.theme = theme;
    });
    this.theme = themingService.theme.value;
  }

}
