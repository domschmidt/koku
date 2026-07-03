import { inject } from '@angular/core';
import { FORMULAR_PLUGIN, FormularComponent, FormularPlugin } from './formular/formular.component';
import { BusinessRuleExecutor } from './business-rules/BusinessRuleExecutor';
import { Observable, Subscriber, Subscription } from 'rxjs';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ModalService } from './modal/modal.service';
import { BUSINESS_RULES_CONTENT_SETUP } from './business-rules-binding/registry';
import { UnsavedChangesPreventionGuard } from './navi/UnsavedChangesPreventionGuard';
import { ModalButtonType, ModalType, RenderedModalType } from './modal/modal.type';
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
        (eventPayload) => this.handleGlobalEventListener(currentEventListener, eventPayload),
      );
    }
  }

  private handleGlobalEventListener(
    currentEventListener: KokuDto.AbstractFormViewGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    if (currentEventListener['@type'] === 'source-update-via-payload') {
      this.updateSourceViaPayload(
        currentEventListener as KokuDto.FormViewEventPayloadSourceUpdateGlobalEventListenerDto,
        eventPayload,
      );
      return;
    }
    if (currentEventListener['@type'] === 'field-update-via-payload') {
      this.updateFieldsViaPayload(
        currentEventListener as KokuDto.FormViewEventPayloadFieldUpdateGlobalEventListenerDto,
        eventPayload,
      );
      return;
    }
    throw new Error(`Unknown EventListenerType ${currentEventListener['@type']}`);
  }

  private updateSourceViaPayload(
    eventListener: KokuDto.FormViewEventPayloadSourceUpdateGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    if (!eventListener.idPath) {
      throw new Error('Missing idPath configuration in EventListener');
    }

    const sourceSnapshot = this.formularInstance.source();
    if (get(eventPayload, eventListener.idPath) === get(sourceSnapshot, eventListener.idPath)) {
      this.formularInstance.runtime.replaceSource({
        ...sourceSnapshot,
        ...eventPayload,
      });
    }
  }

  private updateFieldsViaPayload(
    eventListener: KokuDto.FormViewEventPayloadFieldUpdateGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    this.updateContentConfigViaPayload(eventListener, eventPayload);
    this.updateContentValuesViaPayload(eventListener, eventPayload);
  }

  private updateContentConfigViaPayload(
    eventListener: KokuDto.FormViewEventPayloadFieldUpdateGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    for (const [fieldRef, currentMappingDefinition] of Object.entries(eventListener.configMapping || {})) {
      const registeredContent = this.formularInstance.runtime.content(fieldRef);
      if (!registeredContent) {
        throw new Error(`Content state not found: ${fieldRef}`);
      }
      if ((currentMappingDefinition.valueMapping || {})['@type'] === 'append-list') {
        this.appendContentConfigListItem(fieldRef, registeredContent, currentMappingDefinition, eventPayload);
      }
    }
  }

  private appendContentConfigListItem(
    fieldRef: string,
    registeredContent: any,
    currentMappingDefinition: KokuDto.FormViewFieldConfigMapping,
    eventPayload: any,
  ): void {
    const targetConfigPath = currentMappingDefinition.targetConfigPath;
    if (!targetConfigPath) {
      throw new Error('Missing targetConfigPath');
    }

    const registeredFieldConfig = { ...registeredContent };
    const currentArray = [...(get(registeredContent, targetConfigPath) || [])];
    currentArray.push(
      this.createConfigListItem(
        currentMappingDefinition.valueMapping as KokuDto.ConfigMappingAppendListDto,
        eventPayload,
      ),
    );
    set(registeredFieldConfig, targetConfigPath, currentArray);
    this.formularInstance.runtime.updateContentConfig(fieldRef, () => registeredFieldConfig);
  }

  private createConfigListItem(
    mappingDefinition: KokuDto.ConfigMappingAppendListDto,
    eventPayload: any,
  ): Record<string, any> {
    const newItem: Record<string, any> = {};
    for (const currentValueMapping of mappingDefinition.valueMapping || []) {
      this.applyConfigListItemValue(newItem, currentValueMapping, eventPayload);
    }
    return newItem;
  }

  private applyConfigListItemValue(
    newItem: Record<string, any>,
    currentValueMapping: KokuDto.AbstractConfigMappingAppendListItemDto,
    eventPayload: any,
  ): void {
    if (currentValueMapping['@type'] === 'source-path') {
      this.applySourcePathConfigListItemValue(
        newItem,
        currentValueMapping as KokuDto.StringConversionConfigMappingAppendListItemDto,
        eventPayload,
      );
      return;
    }
    if (currentValueMapping['@type'] === 'string-transformation') {
      this.applyStringTransformationConfigListItemValue(
        newItem,
        currentValueMapping as KokuDto.StringTransformationConfigMappingAppendListItemDto,
        eventPayload,
      );
      return;
    }
    if (currentValueMapping['@type'] === 'string-conversion') {
      this.applyStringConversionConfigListItemValue(
        newItem,
        currentValueMapping as KokuDto.SourcePathConfigMappingAppendListItemDto,
        eventPayload,
      );
      return;
    }
    if (currentValueMapping['@type'] === 'static-value') {
      this.applyStaticConfigListItemValue(
        newItem,
        currentValueMapping as KokuDto.StaticValueConfigMappingAppendListItemDto,
      );
      return;
    }
    throw new Error(`Unknown value Mapping type ${currentValueMapping['@type']}`);
  }

  private applySourcePathConfigListItemValue(
    newItem: Record<string, any>,
    valueMapping: KokuDto.StringConversionConfigMappingAppendListItemDto,
    eventPayload: any,
  ): void {
    if (!valueMapping.sourcePath) {
      throw new Error('Missing sourcePath');
    }
    if (!valueMapping.targetPath) {
      throw new Error('Missing targetPath');
    }
    const currentValue = get(eventPayload, valueMapping.sourcePath);
    if (currentValue !== undefined) {
      set(newItem, valueMapping.targetPath, currentValue);
    }
  }

  private applyStringTransformationConfigListItemValue(
    newItem: Record<string, any>,
    valueMapping: KokuDto.StringTransformationConfigMappingAppendListItemDto,
    eventPayload: any,
  ): void {
    if (!valueMapping.targetPath) {
      throw new Error('Missing targetPath');
    }
    set(newItem, valueMapping.targetPath, this.transformStringValue(valueMapping, eventPayload));
  }

  private transformStringValue(
    valueMapping: KokuDto.StringTransformationConfigMappingAppendListItemDto,
    eventPayload: any,
  ): string {
    let replacedValue = valueMapping.transformPattern || '';
    for (const [currentParam, currentParamMapping] of Object.entries(valueMapping.transformPatternParameters || {})) {
      if (currentParamMapping['@type'] !== 'source-path') {
        throw new Error(`Unknown Param type ${currentParamMapping['@type']}`);
      }
      const castedParam = currentParamMapping as KokuDto.StringTransformationSourcePathPatternParam;
      if (!castedParam.sourcePath) {
        throw new Error('Missing sourcePath');
      }
      replacedValue = replacedValue.replaceAll(currentParam, get(eventPayload, castedParam.sourcePath));
    }
    return replacedValue;
  }

  private applyStringConversionConfigListItemValue(
    newItem: Record<string, any>,
    valueMapping: KokuDto.SourcePathConfigMappingAppendListItemDto,
    eventPayload: any,
  ): void {
    if (!valueMapping.sourcePath) {
      throw new Error('Missing sourcePath');
    }
    if (!valueMapping.targetPath) {
      throw new Error('Missing targetPath');
    }
    const currentValue = get(eventPayload, valueMapping.sourcePath);
    if (currentValue !== undefined) {
      set(newItem, valueMapping.targetPath, String(currentValue));
    }
  }

  private applyStaticConfigListItemValue(
    newItem: Record<string, any>,
    valueMapping: KokuDto.StaticValueConfigMappingAppendListItemDto,
  ): void {
    if (!valueMapping.targetPath) {
      throw new Error('Missing targetPath');
    }
    if (valueMapping.value !== undefined) {
      set(newItem, valueMapping.targetPath, valueMapping.value);
    }
  }

  private updateContentValuesViaPayload(
    eventListener: KokuDto.FormViewEventPayloadFieldUpdateGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    for (const [currentFieldId, reference] of Object.entries(eventListener.fieldValueMapping || {})) {
      if (!reference) {
        throw new Error('Unexpected reference');
      }
      const newValue = this.resolveUpdatedContentValue(currentFieldId, reference, eventPayload);
      if (newValue !== undefined) {
        this.formularInstance.runtime.updateContentValue(currentFieldId, newValue);
        this.formularInstance.runtime.emit(currentFieldId, 'INPUT', newValue);
        this.formularInstance.runtime.emit(currentFieldId, 'CHANGE', newValue);
      }
    }
  }

  private resolveUpdatedContentValue(
    currentFieldId: string,
    reference: KokuDto.AbstractFormViewFieldValueMapping,
    eventPayload: any,
  ): any {
    const content = this.formularInstance.runtime.content(currentFieldId);
    const contentValue = this.formularInstance.runtime.contentHandle(currentFieldId)?.value;
    if (!content || !contentValue) {
      throw new Error(`Content value state not found: ${currentFieldId}`);
    }
    if (reference['@type'] === 'field-reference') {
      return this.resolveFieldReferenceValue(reference as KokuDto.FormViewFieldReferenceValueMapping, eventPayload);
    }
    if (reference['@type'] === 'append-list') {
      return [
        ...contentValue(),
        this.resolveAppendListValue(reference as KokuDto.FormViewFieldReferenceMultiSelectValueMapping, eventPayload),
      ];
    }
    throw new Error('Unexpected reference');
  }

  private resolveFieldReferenceValue(reference: KokuDto.FormViewFieldReferenceValueMapping, eventPayload: any): any {
    if (!reference.source) {
      throw new Error('Unexpected reference source');
    }
    return this.resolvePayloadValueSource(reference.source, eventPayload);
  }

  private resolveAppendListValue(
    reference: KokuDto.FormViewFieldReferenceMultiSelectValueMapping,
    eventPayload: any,
  ): Record<string, any> {
    let newValueItem: Record<string, any> = {};
    for (const [currentMappingTarget, currentMappingSource] of Object.entries(reference.targetPathMapping || {})) {
      const currentValue = this.resolvePayloadValueSource(currentMappingSource, eventPayload);
      if (currentValue !== undefined) {
        if (currentMappingTarget !== undefined) {
          newValueItem[currentMappingTarget] = currentValue;
        } else {
          newValueItem = currentValue;
        }
      }
    }
    return newValueItem;
  }

  private resolvePayloadValueSource(
    valueSource: KokuDto.AbstractFormViewEventPayloadFieldUpdateValueSource,
    eventPayload: any,
  ): any {
    if (valueSource['@type'] === 'source-path') {
      const castedSource = valueSource as KokuDto.FormViewEventPayloadSourcePathFieldUpdateValueSourceDto;
      if (!castedSource.sourcePath) {
        throw new Error('Unexpected reference sourcePath');
      }
      return get(eventPayload, castedSource.sourcePath);
    }
    if (valueSource['@type'] === 'static-value') {
      return (valueSource as KokuDto.FormViewEventPayloadStaticValueFieldUpdateValueSourceDto).value;
    }
    throw new Error(`Unknown mapping source type ${valueSource['@type']}`);
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
    private readonly modalService: ModalService,
    private readonly toastService: ToastService,
    private readonly formularInstance: FormularComponent,
  ) {}

  onFormularLoaded(formularData: KokuDto.FormViewDto): void {
    this.clearButtonSubscriptions();
    for (const configuredContent of Object.values(formularData.contents ?? {})) {
      this.registerButtonListener(configuredContent);
    }
  }

  private registerButtonListener(configuredContent: KokuDto.AbstractFormularContent): void {
    if (configuredContent['@type'] !== 'button') {
      return;
    }
    const contentId = this.requireButtonId(configuredContent);
    const handle = this.formularInstance.runtime.contentHandle(contentId);
    if (!handle) {
      throw new Error(`Button handle not found: ${contentId}`);
    }

    this.buttonSubscriptions.push(
      handle.events.subscribe({
        next: ({ eventName }) => this.handleButtonEvent(contentId, configuredContent, eventName),
      }),
    );
  }

  private requireButtonId(configuredContent: KokuDto.AbstractFormularContent): string {
    if (!configuredContent.id) {
      throw new Error('missing button id for button config');
    }
    return configuredContent.id;
  }

  private handleButtonEvent(
    contentId: string,
    configuredContent: KokuDto.AbstractFormularContent,
    eventName: string,
  ): void {
    const content = this.formularInstance.runtime.content(contentId) ?? configuredContent;
    if (content['@type'] !== 'button' || eventName !== 'CLICK') {
      return;
    }
    const buttonCfg = content as KokuDto.KokuFormButton;
    if (buttonCfg.buttonType === 'SUBMIT') {
      this.submitButton(buttonCfg);
    }
  }

  private submitButton(buttonCfg: KokuDto.KokuFormButton): void {
    this.executeWithUserConfirmation(buttonCfg, () =>
      this.formularInstance.submit(buttonCfg.submitPayload).pipe(
        tap({
          next: (rawValue) => this.handleSubmitSuccess(buttonCfg, rawValue),
          error: () => this.executeButtonEvents(buttonCfg.failEvents || []),
        }),
      ),
    );
  }

  private handleSubmitSuccess(buttonCfg: KokuDto.KokuFormButton, rawValue: any): void {
    for (const currentPostProcessingAction of buttonCfg.postProcessingActions || []) {
      this.applyPostProcessingAction(currentPostProcessingAction);
    }
    this.executeButtonEvents(buttonCfg.successEvents || [], rawValue);
  }

  private applyPostProcessingAction(currentPostProcessingAction: KokuDto.AbstractFormButtonButtonAction): void {
    if (currentPostProcessingAction['@type'] !== 'reload') {
      throw new Error('Unknown PostProcessingAction');
    }
    this.formularInstance.loadSource();
  }

  private executeWithUserConfirmation(buttonCfg: KokuDto.KokuFormButton, cb: () => Observable<any>): void {
    if (!buttonCfg.userConfirmation) {
      cb().subscribe({
        error: () => undefined,
      });
      return;
    }

    const confirmationText = this.resolveUserConfirmationText(buttonCfg.userConfirmation);
    const confirmationModal = this.modalService.add({
      headline: confirmationText.headline,
      content: confirmationText.content,
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
            this.executeConfirmedAction(cb, confirmationModal, modal, button);
          },
        },
      ],
      clickOutside: () => {
        confirmationModal.close();
      },
    });
  }

  private resolveUserConfirmationText(userConfirmation: KokuDto.FormUserConfirmationDto): {
    headline: string;
    content: string;
  } {
    let headline = userConfirmation.headline || '';
    let content = userConfirmation.content || '';

    for (const currentParam of userConfirmation.params || []) {
      const value = this.resolveUserConfirmationParamValue(currentParam);
      headline = headline.replaceAll(currentParam.param!, value);
      content = content.replaceAll(currentParam.param!, value);
    }
    return { headline, content };
  }

  private resolveUserConfirmationParamValue(currentParam: KokuDto.AbstractFormUserConfirmationParamDto): any {
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
    return get(this.formularInstance.source(), castedParam.sourcePath);
  }

  private executeConfirmedAction(
    cb: () => Observable<any>,
    confirmationModal: RenderedModalType,
    modal: ModalType,
    button: ModalButtonType,
  ): void {
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
  }

  private executeButtonEvents(events: KokuDto.AbstractFormEventDto[], eventPayload?: any): void {
    for (const currentEvent of events || []) {
      if (currentEvent['@type'] === 'notification') {
        this.showButtonNotification(currentEvent as KokuDto.FormNotificationEvent);
        continue;
      }
      if (currentEvent['@type'] === 'propagate-global-event') {
        this.propagateButtonGlobalEvent(currentEvent as KokuDto.FormPropagateGlobalEventDto, eventPayload);
        continue;
      }
      throw new Error(`Unknown event type ${currentEvent['@type']}`);
    }
  }

  private showButtonNotification(event: KokuDto.FormNotificationEvent): void {
    if (!event.text) {
      throw new Error('Missing text in Notification');
    }
    let notificationText = event.text;
    for (const currentParam of event.params || []) {
      if (!currentParam.param) {
        throw new Error(`Missing param`);
      }
      notificationText = notificationText.replaceAll(
        currentParam.param,
        this.resolveNotificationParamValue(currentParam),
      );
    }
    this.toastService.add(notificationText, this.formNotificationSerenity(event));
  }

  private resolveNotificationParamValue(
    currentParam: KokuDto.FormNotificationEventValueParamDto | KokuDto.FormNotificationEventDateValueParamDto,
  ): any {
    if (currentParam['@type'] !== 'value' && currentParam['@type'] !== 'date-value') {
      throw new Error(`Unknown param type ${currentParam['@type']}`);
    }
    if (!currentParam.sourcePath) {
      throw new Error(`Missing valuePath for param: ${currentParam.param}`);
    }
    return get(this.formularInstance.source(), currentParam.sourcePath);
  }

  private formNotificationSerenity(event: KokuDto.FormNotificationEvent): ToastTypeUnion {
    if (event.serenity === undefined) {
      return 'info';
    }
    if (event.serenity === 'SUCCESS') {
      return 'success';
    }
    if (event.serenity === 'ERROR') {
      return 'error';
    }
    throw new Error(`Unknown Notification serenity ${event.serenity}`);
  }

  private propagateButtonGlobalEvent(event: KokuDto.FormPropagateGlobalEventDto, eventPayload?: any): void {
    if (!event.eventName) {
      throw new Error('Missing eventName');
    }
    GLOBAL_EVENT_BUS.propagateGlobalEvent(event.eventName, eventPayload);
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
    private readonly unsavedChangesPreventionGuard: UnsavedChangesPreventionGuard<any>,
    private readonly modalService: ModalService,
    private readonly formularInstance: FormularComponent,
  ) {}

  destroy(): void {
    this.unsavedChangesPreventionGuard.unregisterUnsavedChangesPrevention(this);
  }

  onSourceLoaded(): void {
    if (this.unsavedChangesPreventionGuardInitialized) {
      return;
    }

    this.unsavedChangesPreventionGuardInitialized = true;
    this.unsavedChangesPreventionGuard.registerUnsavedChangesPrevention(this, () =>
      this.confirmNavigationWithUnsavedChanges(),
    );
  }

  private confirmNavigationWithUnsavedChanges(): Observable<boolean> {
    return new Observable<boolean>((observer) => {
      if (!this.formularInstance.dirty()) {
        this.completeNavigationDecision(observer, true);
        return;
      }
      this.openUnsavedChangesConfirmation(observer);
    });
  }

  private openUnsavedChangesConfirmation(observer: Subscriber<boolean>): void {
    const confirmationModal: RenderedModalType = this.modalService.add({
      headline: 'Ungespeicherte Änderungen',
      content: 'Es gibt ungespeicherte Änderungen. Willst du trotzdem fortfahren?',
      buttons: [this.createCancelNavigationButton(observer), this.createContinueNavigationButton(observer)],
      clickOutside: () => {
        this.rejectUnsavedChangesNavigation(confirmationModal, observer);
      },
    });
  }

  private createCancelNavigationButton(observer: Subscriber<boolean>): ModalButtonType {
    return {
      buttonType: 'BUTTON',
      title: 'Jetzt abbrechen',
      text: 'Abbruch',
      onClick: (_event, modal) => {
        this.rejectUnsavedChangesNavigation(modal, observer);
      },
    };
  }

  private createContinueNavigationButton(observer: Subscriber<boolean>): ModalButtonType {
    return {
      buttonType: 'BUTTON',
      title: 'Trotzdem jetzt fortfahren',
      text: 'Fortfahren',
      onClick: (_event, modal) => {
        this.modalService.close(modal);
        this.formularInstance.dirty.set(false);
        this.completeNavigationDecision(observer, true);
      },
    };
  }

  private rejectUnsavedChangesNavigation(modal: RenderedModalType, observer: Subscriber<boolean>): void {
    this.modalService.close(modal);
    this.completeNavigationDecision(observer, false);
  }

  private completeNavigationDecision(observer: Subscriber<boolean>, decision: boolean): void {
    observer.next(decision);
    observer.complete();
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
    if (error.error?.['@type'] !== 'business-error-with-confirmation-message') {
      return false;
    }

    this.openSubmitBusinessExceptionModal(
      error.error as KokuDto.KokuBusinessErrorWithConfirmationMessageDto,
      request,
      submitMethod,
      submitUrl,
      submitData,
    );
    return true;
  }

  private openSubmitBusinessExceptionModal(
    businessError: KokuDto.KokuBusinessErrorWithConfirmationMessageDto,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
  ): void {
    const confirmationModal = this.modalService.add({
      headline: businessError.headline,
      content: businessError.confirmationMessage,
      buttons: this.createSubmitBusinessExceptionButtons(
        businessError,
        request,
        submitMethod,
        submitUrl,
        submitData,
        () => confirmationModal,
      ),
      clickOutside: () => {
        if (businessError.closeOnClickOutside) {
          this.modalService.close(confirmationModal);
          request.complete();
        }
      },
    });
  }

  private createSubmitBusinessExceptionButtons(
    businessError: KokuDto.KokuBusinessErrorWithConfirmationMessageDto,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
    confirmationModal: () => RenderedModalType,
  ): ModalButtonType[] {
    return (businessError.buttons || []).map((buttonCfg) =>
      this.createSubmitBusinessExceptionButton(
        buttonCfg,
        request,
        submitMethod,
        submitUrl,
        submitData,
        confirmationModal,
      ),
    );
  }

  private createSubmitBusinessExceptionButton(
    buttonCfg: KokuDto.KokuBusinessExceptionButtonDto,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
    confirmationModal: () => RenderedModalType,
  ): ModalButtonType {
    if (buttonCfg['@type'] === 'close-button') {
      return this.createSubmitCloseButton(
        buttonCfg as KokuDto.KokuBusinessExceptionCloseButtonDto,
        request,
        confirmationModal,
      );
    }
    if (buttonCfg['@type'] === 'send-to-different-endpoint-button') {
      return this.createSubmitEndpointButton(
        buttonCfg as KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
        request,
        submitMethod,
        submitUrl,
        submitData,
        confirmationModal,
      );
    }
    throw new Error('Unknown button type');
  }

  private createSubmitCloseButton(
    buttonCfg: KokuDto.KokuBusinessExceptionCloseButtonDto,
    request: Subscriber<any>,
    confirmationModal: () => RenderedModalType,
  ): ModalButtonType {
    return {
      loading: buttonCfg.loading,
      disabled: buttonCfg.disabled,
      buttonType: 'BUTTON',
      title: buttonCfg.title,
      icon: buttonCfg.icon,
      text: buttonCfg.text,
      styles: buttonCfg.styles,
      size: buttonCfg.size,
      onClick: () => {
        this.modalService.close(confirmationModal());
        request.complete();
      },
    };
  }

  private createSubmitEndpointButton(
    buttonCfg: KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
    confirmationModal: () => RenderedModalType,
  ): ModalButtonType {
    return {
      loading: buttonCfg.loading,
      disabled: buttonCfg.disabled,
      buttonType: 'BUTTON',
      title: buttonCfg.title,
      icon: buttonCfg.icon,
      text: buttonCfg.text,
      styles: buttonCfg.styles,
      size: buttonCfg.size,
      onClick: (event: Event, modal: ModalType, button: ModalButtonType) => {
        this.submitBusinessExceptionEndpoint(
          buttonCfg,
          request,
          submitMethod,
          submitUrl,
          submitData,
          confirmationModal,
          button,
        );
      },
    };
  }

  private submitBusinessExceptionEndpoint(
    buttonCfg: KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
    request: Subscriber<any>,
    submitMethod: string,
    submitUrl: string,
    submitData: any,
    confirmationModal: () => RenderedModalType,
    button: ModalButtonType,
  ): void {
    this.updateSubmitBusinessExceptionButtonState(buttonCfg, button, true);
    this.formularInstance
      .requestSubmit(buttonCfg.endpointMethod || submitMethod, buttonCfg.endpointUrl || submitUrl, submitData)
      .subscribe({
        next: (response) => {
          this.modalService.close(confirmationModal());
          request.next(response);
          request.complete();
        },
        error: () => {
          this.updateSubmitBusinessExceptionButtonState(buttonCfg, button, false);
        },
        complete: () => {
          this.modalService.close(confirmationModal());
          request.complete();
        },
      });
  }

  private updateSubmitBusinessExceptionButtonState(
    buttonCfg: KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
    button: ModalButtonType,
    active: boolean,
  ): void {
    if (buttonCfg.showLoadingAnimation) {
      button.loading = active;
    }
    if (buttonCfg.showDisabledState) {
      button.disabled = active;
    }
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
