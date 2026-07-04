import { inject } from '@angular/core';
import { BehaviorSubject, filter, Observable, Subscriber, Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ModalService } from './modal/modal.service';
import { ModalButtonType, ModalType, RenderedModalType } from './modal/modal.type';
import { GLOBAL_EVENT_BUS } from './events/global-events';
import { get } from './utils/get';
import { UNIQUE_REF_GENERATOR } from './utils/uniqueRef';
import { set } from './utils/set';
import Holidays, { HolidaysTypes } from 'date-holidays';
import dayjs from 'dayjs';
import dayOfYear from 'dayjs/plugin/dayOfYear';
import {
  CALENDAR_PLUGIN,
  CalendarComponent,
  CalendarContext,
  CalendarPlugin,
  CalendarPluginEventSourceFactory,
  CalendarPluginInstanceDetails,
  DateSelection,
  RenderedCalendarInlineItem,
} from './calendar/calendar.component';
import { ToastService } from './toast/toast.service';
import { catchError } from 'rxjs/operators';
import { EventApi, EventInput, EventSourceInput } from '@fullcalendar/core';
import { Frequency, Options } from 'rrule/dist/esm/types';
import { LIST_CONTENT_SETUP } from './list-binding/registry';
import { CALENDAR_CONTENT_SETUP } from './calendar-binding/registry';

dayjs.extend(dayOfYear);

interface CalendarItemValueParam {
  '@type'?: string;
  param?: string;
  valuePath?: string;
}

interface CalendarPropagateGlobalEventSuccessEvent {
  '@type'?: string;
  eventName?: string;
}

interface CalendarDropEvent {
  item: Record<string, never>;
  newStart: Date;
  newEnd: Date;
  allDay: boolean;
  revert: () => void;
  setLoading: (loading: boolean) => void;
}

interface CalendarResizeEvent {
  item: Record<string, never>;
  newEnd: Date;
  allDay: boolean;
  revert: () => void;
  setLoading: (loading: boolean) => void;
}

interface CalendarListQuerySetup {
  fieldSelection: Set<string>;
  fieldPredicates: Record<string, KokuDto.ListFieldQuery>;
}

type CalendarHttpMethod = 'POST' | 'PUT' | 'GET' | 'DELETE';

type CalendarDropHttpAction = KokuDto.CalendarCallHttpItemDropAction &
  Pick<KokuDto.CalendarCallHttpItemClickAction, 'endDatePath' | 'endTimePath'>;

export class CalendarInteractionPlugin implements CalendarPlugin {
  constructor(private readonly calendarInstance: CalendarComponent) {}

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarInteractionPlugin',
    };
  }

  onDateSelect(dateClickInfo: DateSelection): void {
    const calendarConfig = this.calendarInstance.config();
    const clickAction = calendarConfig?.calendarClickAction;
    if (!clickAction) {
      return;
    }
    if (clickAction['@type'] !== 'open-routed-content') {
      throw new Error(`Unknown ClickAction Type ${clickAction['@type']}`);
    }
    const castedClickAction = clickAction as KokuDto.CalendarOpenRoutedContentClickActionDto;
    if (castedClickAction.route) {
      let context: CalendarContext | undefined = undefined;
      if (dateClickInfo?.selectionStart) {
        context = {
          selectionStartDate: dayjs(dateClickInfo.selectionStart).format('YYYY-MM-DD'),
          selectionStartTime: dayjs(dateClickInfo.selectionStart).format('HH:mm'),
          selectionStartDateTime: dayjs(dateClickInfo.selectionStart).format("YYYY-MM-DD'T'HH:mm"),
          selectionEndDate: dayjs(dateClickInfo.selectionEnd).format('YYYY-MM-DD'),
          selectionEndTime: dayjs(dateClickInfo.selectionEnd).format('HH:mm'),
          selectionEndDateTime: dayjs(dateClickInfo.selectionEnd).format("YYYY-MM-DD'T'HH:mm"),
        };
      }
      this.calendarInstance.openRoutedContent(castedClickAction.route.split('/'), context);
    }
  }
}

export interface CalendarUserSelectActionPluginApi {
  selectedUserDetails: BehaviorSubject<KokuDto.KokuUserDto | null>;
  selectUser(): void;
}

export class CalendarUserSelectActionPlugin implements CalendarPlugin {
  private readonly selectedUserDetails = new BehaviorSubject<KokuDto.KokuUserDto | null>(null);
  private selectedUserSubscription: Subscription | undefined;
  readonly componentRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private readonly httpClient: HttpClient,
    private readonly toastService: ToastService,
    private readonly modalService: ModalService,
  ) {
    this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/@self').subscribe({
      next: (userResult) => {
        this.selectedUserDetails.next(userResult);
      },
      error: () => {
        this.toastService.add('Fehler beim Laden der Nutzerinformationen', 'error');
      },
    });
  }

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarUserSelectActionPlugin',
      api: {
        selectedUserDetails: this.selectedUserDetails,
        selectUser: () => this.selectUser(),
      } as CalendarUserSelectActionPluginApi,
    };
  }

  destroy(): void {
    if (this.selectedUserSubscription) {
      this.selectedUserSubscription.unsubscribe();
    }
  }

  selectUser(): void {
    const newModal = this.modalService.add({
      dynamicContent: {
        '@type': 'header',
        title: 'Bedienung',
        content: {
          '@type': 'list',
          listUrl: '/services/users/users/list?selectMode=true',
          sourceUrl: '/services/users/users/query',
          contentSetup: LIST_CONTENT_SETUP,
          parentRoutePath: '/calendar',
        },
      },
      urlSegments: {},
      dynamicContentSetup: CALENDAR_CONTENT_SETUP.modalContentRegistry,
      fullscreen: true,
      maxWidthInPx: 799,
      parentRoutePath: '',
      clickOutside: () => {
        newModal.close();
      },
      onCloseRequested: () => {
        newModal.close();
      },
    });
    GLOBAL_EVENT_BUS.addGlobalEventListener(this.componentRef, 'user-selected', (payload) => {
      this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/' + payload.id).subscribe({
        next: (userResult) => {
          this.selectedUserDetails.next(userResult);
          newModal.close();
        },
        error: () => {
          this.toastService.add('Fehler beim Laden der Nutzerinformationen', 'error');
        },
      });
    });
  }

  initCalendarAction(
    currentCalendarAction: KokuDto.AbstractCalendarActionDto,
    updateCb: (updatedAction: KokuDto.AbstractCalendarActionDto) => void,
  ): void {
    if (currentCalendarAction['@type'] !== 'select-user') {
      return;
    }

    updateCb({
      ...currentCalendarAction,
      loading: true,
    });

    if (this.selectedUserSubscription) {
      this.selectedUserSubscription.unsubscribe();
    }
    this.selectedUserSubscription = this.selectedUserDetails.pipe(filter((value) => value !== null)).subscribe({
      next: (selectedUserDetails) => {
        updateCb({
          ...currentCalendarAction,
          loading: false,
          imgBase64: selectedUserDetails.avatarBase64,
        });
      },
    });
  }
}

export class CalendarGlobalEventPlugin implements CalendarPlugin {
  readonly componentRef = UNIQUE_REF_GENERATOR.generate();
  readonly componentRefInlineContent = UNIQUE_REF_GENERATOR.generate();

  constructor(private readonly calendarInstance: CalendarComponent) {}

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarGlobalEventPlugin',
    };
  }

  destroy(): void {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRefInlineContent);
  }

  onRoutedInlineContentOpened(routedContent: KokuDto.CalendarRoutedContentDto): void {
    for (const currentListener of routedContent.globalEventListeners || []) {
      if (!currentListener.eventName) {
        throw new Error('Missing eventName in Listener Config');
      }
      GLOBAL_EVENT_BUS.addGlobalEventListener(this.componentRefInlineContent, currentListener.eventName, () => {
        if (currentListener['@type'] !== 'close') {
          throw new Error(`Unexpected listener Type: ${currentListener['@type']}`);
        }
        this.calendarInstance.closeInlineContent().subscribe();
      });
    }
  }

  onRoutedInlineContentClose(content: RenderedCalendarInlineItem | null): void {
    if (content?.id != null) {
      GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRefInlineContent);
    }
  }

  afterConfigLoaded(config: KokuDto.CalendarConfigDto): void {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
    for (const currentEventListener of config.globalEventListeners || []) {
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
    currentEventListener: KokuDto.AbstractCalendarGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    if (currentEventListener['@type'] === 'refresh') {
      this.calendarInstance.calendarComponent()?.getApi().refetchEvents();
      return;
    }

    if (currentEventListener['@type'] !== 'replace-via-payload') {
      throw new Error(`Unknown event Listener Type: ${currentEventListener['@type']}`);
    }

    this.replaceEventViaPayload(
      currentEventListener as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
      eventPayload,
    );
  }

  private replaceEventViaPayload(
    eventListener: KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    const sourceId = this.requireSourceId(eventListener);
    this.requireEventSource(sourceId);
    const calendarSourceFactory = this.requireEventSourceFactory(sourceId);
    const lookedUpEvent = calendarSourceFactory.lookupEvent(eventPayload);

    if (this.shouldDeleteEvent(eventListener, eventPayload)) {
      lookedUpEvent?.remove();
      return;
    }

    const updatedEvent = this.upsertCalendarEvent(
      lookedUpEvent,
      calendarSourceFactory.generateEventItem(eventPayload),
      sourceId,
      eventPayload,
    );
    this.flashEvent(updatedEvent);
  }

  private requireSourceId(eventListener: KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto): string {
    if (eventListener.sourceId === undefined) {
      throw new Error(`Missing sourceId`);
    }
    return eventListener.sourceId;
  }

  private requireEventSource(sourceId: string): void {
    const eventSource = this.calendarInstance.calendarComponent()?.getApi().getEventSourceById(sourceId);
    if (!eventSource) {
      throw new Error(`eventSource ${sourceId} cannot be resolved`);
    }
  }

  private requireEventSourceFactory(sourceId: string): CalendarPluginEventSourceFactory {
    const calendarSourceFactory = this.calendarInstance.registeredEventSourceFactories[sourceId];
    if (!calendarSourceFactory) {
      throw new Error(`EventSourceFactory ${sourceId} missing`);
    }
    return calendarSourceFactory;
  }

  private shouldDeleteEvent(
    eventListener: KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
    eventPayload: any,
  ): boolean {
    return (
      eventListener.deletedPath !== undefined &&
      eventListener.deletedExpression !== undefined &&
      get(eventPayload, eventListener.deletedPath) === eventListener.deletedExpression
    );
  }

  private upsertCalendarEvent(
    lookedUpEvent: EventApi | undefined,
    newEvent: EventInput,
    eventSourceId: string,
    eventPayload: any,
  ): EventApi | null {
    if (!lookedUpEvent) {
      return this.calendarInstance.calendarComponent()?.getApi().addEvent(newEvent, eventSourceId) ?? null;
    }
    if (newEvent.rrule !== undefined) {
      // FullCalendar does not rebuild recurrence instances when rrule is changed via setProp.
      lookedUpEvent.remove();
      return this.calendarInstance.calendarComponent()?.getApi().addEvent(newEvent, eventSourceId) ?? null;
    }

    this.updateCalendarEvent(lookedUpEvent, newEvent, eventPayload);
    return lookedUpEvent;
  }

  private updateCalendarEvent(lookedUpEvent: EventApi, newEvent: EventInput, eventPayload: any): void {
    if (newEvent.allDay !== undefined && newEvent.allDay !== !!lookedUpEvent.allDay) {
      lookedUpEvent.setAllDay(newEvent.allDay);
    }
    this.updateCalendarEventDates(lookedUpEvent, newEvent);
    lookedUpEvent.setProp('title', newEvent.title);
    lookedUpEvent.setProp('display', newEvent.display);
    lookedUpEvent.setProp('className', newEvent.className);
    lookedUpEvent.setExtendedProp('item', eventPayload);
  }

  private updateCalendarEventDates(lookedUpEvent: EventApi, newEvent: EventInput): void {
    if (newEvent.start !== undefined && newEvent.end !== undefined) {
      lookedUpEvent.setDates(newEvent.start, newEvent.end);
      return;
    }
    if (newEvent.start !== undefined) {
      lookedUpEvent.setStart(newEvent.start, { maintainDuration: true });
    }
    if (newEvent.end !== undefined) {
      lookedUpEvent.setEnd(newEvent.end);
    }
  }

  private flashEvent(event: EventApi | null): void {
    if (!event) {
      return;
    }

    const classnameSnapshot = event.classNames.filter((className: string) => className !== 'calendar-item--flash');
    setTimeout(() => {
      event.setProp(
        'classNames',
        event.classNames.filter((className: string) => className !== 'calendar-item--flash'),
      );
    }, 1000);
    classnameSnapshot.push('calendar-item--flash');
    event.setProp('classNames', classnameSnapshot);
  }
}

export class CalendarListSourcePlugin implements CalendarPlugin {
  private userDetailsSubscriptions: Record<string, Subscription> = {};

  constructor(
    private readonly calendarInstance: CalendarComponent,
    private readonly httpClient: HttpClient,
    private readonly toastService: ToastService,
    private readonly modalService: ModalService,
  ) {}

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarListSourcePlugin',
    };
  }

  destroy(): void {
    for (const currentSubscription of Object.values(this.userDetailsSubscriptions)) {
      currentSubscription.unsubscribe();
    }
  }

  private callHttpEndpoint(method: CalendarHttpMethod, url: string, requestBody: any) {
    return this.httpClient
      .request(method, url, {
        body: requestBody,
      })
      .pipe(catchError((error) => this.handleBusinessError(error, method, url, requestBody)));
  }

  private handleBusinessError(
    error: any,
    method: CalendarHttpMethod,
    url: string,
    requestBody: any,
  ): Observable<unknown> {
    return new Observable((subscriber) => {
      if (error.error?.['@type'] !== 'business-error-with-confirmation-message') {
        subscriber.error(error);
        return;
      }
      this.openBusinessErrorModal(
        error.error as KokuDto.KokuBusinessErrorWithConfirmationMessageDto,
        subscriber,
        method,
        url,
        requestBody,
        error,
      );
    });
  }

  private openBusinessErrorModal(
    businessError: KokuDto.KokuBusinessErrorWithConfirmationMessageDto,
    subscriber: Subscriber<unknown>,
    method: CalendarHttpMethod,
    url: string,
    requestBody: any,
    originalError: any,
  ): void {
    const confirmationModal = this.modalService.add({
      headline: businessError.headline,
      content: businessError.confirmationMessage,
      buttons: this.createBusinessErrorButtons(
        businessError,
        subscriber,
        method,
        url,
        requestBody,
        originalError,
        () => confirmationModal,
      ),
      clickOutside: () => {
        if (businessError.closeOnClickOutside) {
          this.modalService.close(confirmationModal);
          subscriber.complete();
        }
      },
    });
  }

  private createBusinessErrorButtons(
    businessError: KokuDto.KokuBusinessErrorWithConfirmationMessageDto,
    subscriber: Subscriber<unknown>,
    method: CalendarHttpMethod,
    url: string,
    requestBody: any,
    originalError: any,
    confirmationModal: () => RenderedModalType,
  ): ModalButtonType[] {
    return (businessError.buttons || []).map((buttonCfg) =>
      this.createBusinessErrorButton(buttonCfg, subscriber, method, url, requestBody, originalError, confirmationModal),
    );
  }

  private createBusinessErrorButton(
    buttonCfg: KokuDto.KokuBusinessExceptionButtonDto,
    subscriber: Subscriber<unknown>,
    method: CalendarHttpMethod,
    url: string,
    requestBody: any,
    originalError: any,
    confirmationModal: () => RenderedModalType,
  ): ModalButtonType {
    if (buttonCfg['@type'] === 'close-button') {
      return this.createCloseBusinessErrorButton(
        buttonCfg as KokuDto.KokuBusinessExceptionCloseButtonDto,
        subscriber,
        originalError,
        confirmationModal,
      );
    }
    if (buttonCfg['@type'] === 'send-to-different-endpoint-button') {
      return this.createEndpointBusinessErrorButton(
        buttonCfg as KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
        subscriber,
        method,
        url,
        requestBody,
        confirmationModal,
      );
    }
    throw new Error('Unknown button type');
  }

  private createCloseBusinessErrorButton(
    buttonCfg: KokuDto.KokuBusinessExceptionCloseButtonDto,
    subscriber: Subscriber<unknown>,
    originalError: any,
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
        subscriber.error(originalError);
      },
    };
  }

  private createEndpointBusinessErrorButton(
    buttonCfg: KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
    subscriber: Subscriber<unknown>,
    method: CalendarHttpMethod,
    url: string,
    requestBody: any,
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
        this.executeBusinessErrorEndpointAction(
          buttonCfg,
          subscriber,
          method,
          url,
          requestBody,
          button,
          confirmationModal,
        );
      },
    };
  }

  private executeBusinessErrorEndpointAction(
    buttonCfg: KokuDto.KokuBusinessExceptionSendToDifferentEndpointButtonDto,
    subscriber: Subscriber<unknown>,
    method: CalendarHttpMethod,
    url: string,
    requestBody: any,
    button: ModalButtonType,
    confirmationModal: () => RenderedModalType,
  ): void {
    this.updateModalButtonState(buttonCfg, button, true);
    this.callHttpEndpoint(buttonCfg.endpointMethod || method, buttonCfg.endpointUrl || url, requestBody).subscribe({
      next: (args) => {
        this.modalService.close(confirmationModal());
        subscriber.next(args);
        subscriber.complete();
      },
      error: (args) => {
        this.updateModalButtonState(buttonCfg, button, false);
        subscriber.error(args);
      },
    });
  }

  private updateModalButtonState(
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

  private replaceItemValueParams(
    url: string,
    params: CalendarItemValueParam[] | undefined,
    item: Record<string, never>,
  ): string {
    let result = url;
    for (const currentParam of params || []) {
      if (!currentParam.param) {
        throw new Error(`Missing param`);
      }
      if (currentParam['@type'] !== 'item-value') {
        throw new Error(`Unknown param type ${currentParam['@type']}`);
      }
      if (currentParam.valuePath === undefined) {
        throw new Error(`Missing valuePath`);
      }
      result = result.replaceAll(currentParam.param, String(get({ ...item }, currentParam.valuePath)));
    }
    return result;
  }

  private propagateSuccessEvents(
    events: CalendarPropagateGlobalEventSuccessEvent[] | undefined,
    response: unknown,
  ): void {
    for (const currentEvent of events || []) {
      if (currentEvent['@type'] !== 'propagate-global-event') {
        throw new Error(`Unknown event type ${currentEvent['@type']}`);
      }
      if (!currentEvent.eventName) {
        throw new Error(`Missing eventName`);
      }
      GLOBAL_EVENT_BUS.propagateGlobalEvent(currentEvent.eventName, response);
    }
  }

  private handleItemClick(castedSource: KokuDto.CalendarListSourceConfigDto, item: Record<string, never>): void {
    const clickAction = castedSource.clickAction;
    if (!clickAction) {
      return;
    }
    if (clickAction['@type'] !== 'open-routed-content') {
      throw new Error(`Unknown item click action type ${clickAction['@type']}`);
    }

    const castedActionType = clickAction as KokuDto.CalendarOpenRoutedContentItemClickAction;
    if (!castedActionType.route) {
      return;
    }
    const url = this.replaceItemValueParams(
      castedActionType.route,
      castedActionType.params as CalendarItemValueParam[] | undefined,
      item,
    );
    this.calendarInstance.openRoutedContent(url.split('/'));
  }

  private handleDrop(castedSource: KokuDto.CalendarListSourceConfigDto, event: CalendarDropEvent): void {
    const dropAction = castedSource.dropAction;
    if (!dropAction) {
      event.revert();
      return;
    }
    if (dropAction['@type'] !== 'call-http') {
      console.log(`Unknown item drop action type ${dropAction['@type']}`);
      event.revert();
      return;
    }

    this.executeDropAction(dropAction as CalendarDropHttpAction, event);
  }

  private executeDropAction(action: CalendarDropHttpAction, event: CalendarDropEvent): void {
    if (!action.method) {
      throw new Error(`Missing method`);
    }
    if (!action.startDatePath) {
      throw new Error(`Missing startDatePath`);
    }
    if (!action.startTimePath) {
      throw new Error(`Missing startTimePath`);
    }

    const requestBody = this.createDropRequestBody(action, event);
    const url = this.replaceItemValueParams(
      action.url || '',
      action.urlParams as CalendarItemValueParam[] | undefined,
      event.item,
    );
    this.executeCalendarHttpAction(action.method, url, requestBody, action.successEvents, event);
  }

  private createDropRequestBody(action: CalendarDropHttpAction, event: CalendarDropEvent): Record<string, any> {
    const requestBody = {};
    set(requestBody, action.startDatePath!, dayjs(event.newStart).format('YYYY-MM-DD'));
    set(requestBody, action.startTimePath!, dayjs(event.newStart).format('HH:mm'));
    if (action.endDatePath) {
      set(requestBody, action.endDatePath, dayjs(event.newEnd).format('YYYY-MM-DD'));
    }
    if (action.endTimePath) {
      set(requestBody, action.endTimePath, dayjs(event.newEnd).format('HH:mm'));
    }
    this.copyValueMapping(action.valueMapping, event.item, requestBody);
    return requestBody;
  }

  private handleResize(castedSource: KokuDto.CalendarListSourceConfigDto, event: CalendarResizeEvent): void {
    const resizeAction = castedSource.resizeAction;
    if (!resizeAction) {
      event.revert();
      return;
    }
    if (resizeAction['@type'] !== 'call-http') {
      console.log(`Unknown item drop action type ${resizeAction['@type']}`);
      event.revert();
      return;
    }

    this.executeResizeAction(resizeAction as KokuDto.CalendarCallHttpItemResizeAction, event);
  }

  private executeResizeAction(action: KokuDto.CalendarCallHttpItemResizeAction, event: CalendarResizeEvent): void {
    if (!action.method) {
      throw new Error(`Missing method`);
    }
    if (!action.endDatePath) {
      throw new Error(`Missing endDatePath`);
    }
    if (!action.endTimePath) {
      throw new Error(`Missing endTimePath`);
    }

    const requestBody = this.createResizeRequestBody(action, event);
    const url = this.replaceItemValueParams(
      action.url || '',
      action.urlParams as CalendarItemValueParam[] | undefined,
      event.item,
    );
    this.executeCalendarHttpAction(action.method, url, requestBody, action.successEvents, event);
  }

  private createResizeRequestBody(
    action: KokuDto.CalendarCallHttpItemResizeAction,
    event: CalendarResizeEvent,
  ): Record<string, any> {
    const requestBody = {};
    set(requestBody, action.endDatePath!, dayjs(event.newEnd).format('YYYY-MM-DD'));
    set(requestBody, action.endTimePath!, dayjs(event.newEnd).format('HH:mm'));
    this.copyValueMapping(action.valueMapping, event.item, requestBody);
    return requestBody;
  }

  private copyValueMapping(
    valueMapping: Record<string, string> | undefined,
    item: Record<string, never>,
    requestBody: Record<string, any>,
  ): void {
    for (const [source, target] of Object.entries(valueMapping || {})) {
      set(requestBody, target, get(item, source, null));
    }
  }

  private executeCalendarHttpAction(
    method: KokuDto.CalendarCallHttpItemActionMethodEnum,
    url: string,
    requestBody: Record<string, any>,
    successEvents: KokuDto.AbstractCalendarCallHttpItemActionSuccessEventDto[] | undefined,
    event: Pick<CalendarDropEvent, 'revert' | 'setLoading'>,
  ): void {
    event.setLoading(true);
    this.callHttpEndpoint(method, url, requestBody).subscribe({
      next: (response) => {
        event.setLoading(false);
        this.propagateSuccessEvents(successEvents as CalendarPropagateGlobalEventSuccessEvent[] | undefined, response);
        this.toastService.add(`Erfolgreich gespeichert`, 'success');
      },
      error: () => {
        event.setLoading(false);
        event.revert();
        this.toastService.add(`Es ist ein Fehler bei der Anfrage aufgetreten. Versuche es später erneut!`, 'error');
      },
    });
  }

  private generateListEventSource(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    generateEventItem: (item: Record<string, any>) => EventInput,
  ): EventSourceInput {
    return {
      id: castedSource.id,
      events: (arg, successCallback, failureCallback) => {
        const querySetup = this.createListQuerySetup(castedSource, arg);
        this.loadListSource(castedSource, querySetup, generateEventItem, successCallback, failureCallback);
      },
    };
  }

  private createListQuerySetup(castedSource: KokuDto.CalendarListSourceConfigDto, arg: any): CalendarListQuerySetup {
    const fieldSelection = new Set<string>();
    const fieldPredicates: Record<string, KokuDto.ListFieldQuery> = {};
    const startAndEndDOYOrGroupIdentifier = this.resolveDateOrGroupIdentifier(castedSource, arg);

    this.addStartDatePredicate(castedSource, arg, fieldSelection, fieldPredicates, startAndEndDOYOrGroupIdentifier);
    this.addEndDatePredicate(castedSource, arg, fieldSelection, fieldPredicates, startAndEndDOYOrGroupIdentifier);
    this.addSimpleFieldSelections(castedSource, fieldSelection);
    this.addDeletedPredicate(castedSource, fieldSelection, fieldPredicates);
    return { fieldSelection, fieldPredicates };
  }

  private resolveDateOrGroupIdentifier(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    arg: any,
  ): string | undefined {
    return castedSource.searchOperatorHint === 'YEARLY_RECURRING' &&
      dayjs(arg.start).format('MM-DD') > dayjs(arg.end).format('MM-DD')
      ? 'startGTend'
      : undefined;
  }

  private addStartDatePredicate(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    arg: any,
    fieldSelection: Set<string>,
    fieldPredicates: Record<string, KokuDto.ListFieldQuery>,
    orGroupIdentifier: string | undefined,
  ): void {
    if (!castedSource.startDateFieldSelectionPath) {
      return;
    }
    fieldSelection.add(castedSource.startDateFieldSelectionPath);
    fieldPredicates[castedSource.startDateFieldSelectionPath] = {
      predicates: [
        {
          searchExpression: dayjs(arg.end).format('YYYY-MM-DD'),
          searchOperator: 'LESS_OR_EQ',
          searchOperatorHint: castedSource.searchOperatorHint,
          orGroupIdentifier,
        },
        ...(fieldPredicates[castedSource.startDateFieldSelectionPath]?.predicates || []),
      ],
    };
  }

  private addEndDatePredicate(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    arg: any,
    fieldSelection: Set<string>,
    fieldPredicates: Record<string, KokuDto.ListFieldQuery>,
    orGroupIdentifier: string | undefined,
  ): void {
    if (!castedSource.endDateFieldSelectionPath) {
      return;
    }
    fieldSelection.add(castedSource.endDateFieldSelectionPath);
    fieldPredicates[castedSource.endDateFieldSelectionPath] = {
      predicates: [
        {
          searchExpression: dayjs(arg.start).format('YYYY-MM-DD'),
          searchOperator: 'GREATER_OR_EQ',
          searchOperatorHint: castedSource.searchOperatorHint,
          orGroupIdentifier,
        },
        ...(fieldPredicates[castedSource.endDateFieldSelectionPath]?.predicates || []),
      ],
    };
  }

  private addSimpleFieldSelections(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    fieldSelection: Set<string>,
  ): void {
    for (const fieldSelectionPath of [
      castedSource.startTimeFieldSelectionPath,
      castedSource.endTimeFieldSelectionPath,
      castedSource.displayTextFieldSelectionPath,
      ...(castedSource.additionalFieldSelectionPaths || []),
    ]) {
      if (fieldSelectionPath) {
        fieldSelection.add(fieldSelectionPath);
      }
    }
  }

  private addDeletedPredicate(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    fieldSelection: Set<string>,
    fieldPredicates: Record<string, KokuDto.ListFieldQuery>,
  ): void {
    if (!castedSource.deletedFieldSelectionPath) {
      return;
    }
    fieldSelection.add(castedSource.deletedFieldSelectionPath);
    fieldPredicates[castedSource.deletedFieldSelectionPath] = {
      predicates: [
        {
          searchExpression: 'TRUE',
          searchOperator: 'EQ',
          negate: true,
        },
        ...(fieldPredicates[castedSource.deletedFieldSelectionPath]?.predicates || []),
      ],
    };
  }

  private loadListSource(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    querySetup: CalendarListQuerySetup,
    generateEventItem: (item: Record<string, any>) => EventInput,
    successCallback: (events: EventInput[]) => void,
    failureCallback: (error: any) => void,
  ): void {
    if (castedSource.userIdFieldSelectionPath) {
      this.loadListSourceForSelectedUser(castedSource, querySetup, generateEventItem, successCallback, failureCallback);
      return;
    }

    this.fetchListSource(castedSource, querySetup, generateEventItem, successCallback, failureCallback);
  }

  private loadListSourceForSelectedUser(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    querySetup: CalendarListQuerySetup,
    generateEventItem: (item: Record<string, any>) => EventInput,
    successCallback: (events: EventInput[]) => void,
    failureCallback: (error: any) => void,
  ): void {
    const userIdFieldSelectionPath = castedSource.userIdFieldSelectionPath!;
    querySetup.fieldSelection.add(userIdFieldSelectionPath);
    const sourceId = this.requireListSourceId(castedSource);
    const calendarActionPluginApi = this.calendarInstance.getPluginApi(
      'CalendarUserSelectActionPlugin',
    ) as CalendarUserSelectActionPluginApi;

    this.userDetailsSubscriptions[sourceId]?.unsubscribe();
    let firstCall = true;
    this.userDetailsSubscriptions[sourceId] = calendarActionPluginApi.selectedUserDetails
      .pipe(filter((value) => value !== null))
      .subscribe({
        next: (userDetails) => {
          if (firstCall) {
            this.addSelectedUserPredicate(querySetup.fieldPredicates, userIdFieldSelectionPath, userDetails);
            this.fetchListSource(castedSource, querySetup, generateEventItem, successCallback, failureCallback);
          } else {
            this.calendarInstance.calendarComponent()?.getApi().getEventSourceById(sourceId)?.refetch();
          }
          firstCall = false;
        },
      });
  }

  private addSelectedUserPredicate(
    fieldPredicates: Record<string, KokuDto.ListFieldQuery>,
    userIdFieldSelectionPath: string,
    userDetails: KokuDto.KokuUserDto,
  ): void {
    if (userDetails.id === undefined) {
      throw new Error('user id required');
    }
    fieldPredicates[userIdFieldSelectionPath] = {
      predicates: [
        {
          searchExpression: String(userDetails.id),
          searchOperator: 'EQ',
        },
        ...(fieldPredicates[userIdFieldSelectionPath]?.predicates || []),
      ],
    };
  }

  private fetchListSource(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    querySetup: CalendarListQuerySetup,
    generateEventItem: (item: Record<string, any>) => EventInput,
    successCallback: (events: EventInput[]) => void,
    failureCallback: (error: any) => void,
  ): void {
    if (!castedSource.sourceUrl) {
      return;
    }

    const query: KokuDto.ListQuery = {
      fieldSelection: [...querySetup.fieldSelection],
      limit: 100,
      page: 0,
      fieldPredicates: querySetup.fieldPredicates,
    };
    this.httpClient.post<KokuDto.ListPage>(castedSource.sourceUrl, query).subscribe({
      next: (result) => {
        successCallback(
          (result.results || []).map((currentListItem) => generateEventItem(currentListItem.values || {})),
        );
      },
      error: (err) => {
        failureCallback(err);
      },
    });
  }

  private requireListSourceId(castedSource: KokuDto.CalendarListSourceConfigDto): string {
    if (castedSource.id === undefined) {
      throw new Error('Expected source id');
    }
    return castedSource.id;
  }

  provideEventSourceFactory(
    currentSource: KokuDto.AbstractCalendarListSourceConfigDto,
  ): CalendarPluginEventSourceFactory | void {
    if (currentSource?.['@type'] !== 'list') {
      return;
    }

    const castedSource = currentSource as KokuDto.CalendarListSourceConfigDto;
    const generateEventItem = (item: Record<string, any>): EventInput => this.createListEventItem(castedSource, item);
    const generateEventSource = (): EventSourceInput => this.generateListEventSource(castedSource, generateEventItem);

    return {
      generateEventSource,
      generateEventItem,
      lookupEvent: (eventPayload: never) => this.lookupListEvent(castedSource, eventPayload),
    };
  }

  private createListEventItem(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    item: Record<string, any>,
  ): EventInput {
    const start = this.resolveEventDateParts(
      item,
      castedSource.startDateFieldSelectionPath,
      castedSource.startTimeFieldSelectionPath,
      'start',
    );
    const end = this.resolveEventDateParts(
      item,
      castedSource.endDateFieldSelectionPath,
      castedSource.endTimeFieldSelectionPath,
      'end',
    );
    const text = this.createEventText(castedSource, item);

    return {
      id: `${castedSource.id}/${get(item, castedSource.idPath || '')}`,
      title: text,
      display: text,
      start: start.join('T'),
      end: this.calculateEventEndDate(start, end).toDate(),
      rrule: this.createRecurringRule(castedSource, item),
      allDay: !(castedSource.startTimeFieldSelectionPath && castedSource.endTimeFieldSelectionPath),
      className: 'calendar-item',
      extendedProps: { kokuColor: castedSource.sourceItemColor },
      item,
      editable: castedSource.editable !== false,
      onClickHandler: (event: { item: Record<string, never> }) => this.handleItemClick(castedSource, event.item),
      onDropHandler: (event: CalendarDropEvent) => this.handleDrop(castedSource, event),
      onResizeHandler: (event: CalendarResizeEvent) => this.handleResize(castedSource, event),
    };
  }

  private createRecurringRule(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    item: Record<string, any>,
  ): Partial<Options> | undefined {
    if (castedSource.searchOperatorHint !== 'YEARLY_RECURRING') {
      return undefined;
    }
    return {
      freq: Frequency.YEARLY,
      dtstart: get(item, castedSource.startDateFieldSelectionPath || ''),
    };
  }

  private createEventText(castedSource: KokuDto.CalendarListSourceConfigDto, item: Record<string, any>): string {
    const prefix = castedSource.sourceItemText ? `${castedSource.sourceItemText}\n` : '';
    return `${prefix}${get(item, castedSource.displayTextFieldSelectionPath || '')}`;
  }

  private resolveEventDateParts(
    item: Record<string, any>,
    datePath: string | undefined,
    timePath: string | undefined,
    label: 'start' | 'end',
  ): any[] {
    if (datePath === undefined) {
      throw new Error(`Missing ${label}DateFieldSelectionPath`);
    }
    const result = [get(item, datePath)];
    if (timePath !== undefined) {
      result.push(get(item, timePath));
    }
    return result;
  }

  private calculateEventEndDate(start: any[], end: any[]) {
    const startDate = dayjs(start.join('T'));
    const endDate = dayjs(end.join('T'));
    return endDate.diff(startDate, 'minute') < 60 ? startDate.add(60, 'minutes') : endDate;
  }

  private lookupListEvent(
    castedSource: KokuDto.CalendarListSourceConfigDto,
    eventPayload: never,
  ): EventApi | null | undefined {
    if (castedSource.idPath === undefined) {
      throw new Error(`Missing idPath`);
    }
    const eventId = get(eventPayload, castedSource.idPath);
    if (eventId === undefined) {
      throw new Error(`${castedSource.idPath} cannot be resolved`);
    }

    return this.calendarInstance.calendarComponent()?.getApi().getEventById(`${castedSource.id}/${eventId}`);
  }
}

export class CalendarHolidaySourcePlugin implements CalendarPlugin {
  constructor(
    private readonly calendarInstance: CalendarComponent,
    private readonly httpClient: HttpClient,
  ) {}

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarHolidaySourcePlugin',
    };
  }

  private userRegion: KokuDto.KokuUserRegionDto | null = null;

  provideEventSourceFactory(
    currentSource: KokuDto.AbstractCalendarListSourceConfigDto,
  ): CalendarPluginEventSourceFactory | void {
    if (currentSource?.['@type'] !== 'holiday') {
      return;
    }

    const castedSource = currentSource as KokuDto.CalendarHolidaySourceConfigDto;
    const generateEventItem = (item: HolidaysTypes.Holiday): EventInput =>
      this.createHolidayEventItem(castedSource, item);

    return {
      generateEventSource: () => this.generateHolidayEventSource(castedSource, generateEventItem),
      generateEventItem,
      lookupEvent: (eventPayload: HolidaysTypes.Holiday) => this.lookupHolidayEvent(castedSource, eventPayload),
    };
  }

  private createHolidayEventItem(
    castedSource: KokuDto.CalendarHolidaySourceConfigDto,
    item: HolidaysTypes.Holiday,
  ): EventInput {
    return {
      id: `${castedSource.id}/${item.start}`,
      title: item.name,
      start: item.start,
      end: item.end,
      allDay: true,
      editable: false,
      display: 'background',
      className: 'calendar-item',
      extendedProps: { kokuColor: castedSource.sourceItemColor },
      item,
    };
  }

  private generateHolidayEventSource(
    castedSource: KokuDto.CalendarHolidaySourceConfigDto,
    generateEventItem: (item: HolidaysTypes.Holiday) => EventInput,
  ): EventSourceInput {
    return {
      id: castedSource.id,
      events: (arg, successCallback, failureCallback) => {
        this.loadUserRegion(
          (regionResult) => successCallback(this.createHolidayEvents(regionResult, arg, generateEventItem)),
          failureCallback,
        );
      },
    };
  }

  private loadUserRegion(
    successCallback: (regionResult: KokuDto.KokuUserRegionDto) => void,
    failureCallback: (error: any) => void,
  ): void {
    if (this.userRegion) {
      successCallback(this.userRegion);
      return;
    }

    this.httpClient.get<KokuDto.KokuUserRegionDto>('/services/users/users/@self/region').subscribe({
      next: (regionResult) => {
        this.userRegion = regionResult;
        successCallback(regionResult);
      },
      error: (err) => {
        failureCallback(err);
      },
    });
  }

  private createHolidayEvents(
    regionResult: KokuDto.KokuUserRegionDto,
    arg: { start: Date; end: Date },
    generateEventItem: (item: HolidaysTypes.Holiday) => EventInput,
  ): EventInput[] {
    if (!regionResult.country) {
      return [];
    }

    const holidays = this.createHolidays(regionResult);
    const results: EventInput[] = [];
    for (const currentYear of this.rangeOfYears(arg.start.getFullYear(), arg.end.getFullYear())) {
      for (const holiday of holidays.getHolidays(currentYear)) {
        results.push(generateEventItem(holiday));
      }
    }
    return results;
  }

  private createHolidays(regionResult: KokuDto.KokuUserRegionDto): Holidays {
    if (regionResult.state) {
      return new Holidays(regionResult.country!, regionResult.state);
    }
    return new Holidays(regionResult.country!);
  }

  private rangeOfYears(startYear: number, endYear: number): number[] {
    return new Array<number>(endYear - startYear + 1).fill(startYear).map((year, index) => year + index);
  }

  private lookupHolidayEvent(
    castedSource: KokuDto.CalendarHolidaySourceConfigDto,
    eventPayload: HolidaysTypes.Holiday,
  ): EventApi | null | undefined {
    return this.calendarInstance.calendarComponent()?.getApi().getEventById(`${castedSource.id}/${eventPayload.start}`);
  }
}
export const CALENDAR_PLUGIN_PROVIDERS = [
  {
    provide: CALENDAR_PLUGIN,
    useFactory: (): ((calendarInstance: CalendarComponent) => CalendarPlugin) => (calendarInstance) =>
      new CalendarInteractionPlugin(calendarInstance),
    multi: true,
  },
  {
    provide: CALENDAR_PLUGIN,
    useFactory: (): ((calendarInstance: CalendarComponent) => CalendarPlugin) => (calendarInstance) =>
      new CalendarGlobalEventPlugin(calendarInstance),
    multi: true,
  },
  {
    provide: CALENDAR_PLUGIN,
    useFactory: (): ((calendarInstance: CalendarComponent) => CalendarPlugin) => () =>
      new CalendarUserSelectActionPlugin(inject(HttpClient), inject(ToastService), inject(ModalService)),
    multi: true,
  },
  {
    provide: CALENDAR_PLUGIN,
    useFactory: (): ((calendarInstance: CalendarComponent) => CalendarPlugin) => (calendarInstance) =>
      new CalendarListSourcePlugin(calendarInstance, inject(HttpClient), inject(ToastService), inject(ModalService)),
    multi: true,
  },
  {
    provide: CALENDAR_PLUGIN,
    useFactory: (): ((calendarInstance: CalendarComponent) => CalendarPlugin) => (calendarInstance) =>
      new CalendarHolidaySourcePlugin(calendarInstance, inject(HttpClient)),
    multi: true,
  },
];
