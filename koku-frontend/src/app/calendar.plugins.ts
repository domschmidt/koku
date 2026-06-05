import { inject } from '@angular/core';
import { BehaviorSubject, filter, Observable, Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ModalService } from './modal/modal.service';
import { ModalButtonType, ModalType } from './modal/modal.type';
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
import { EventInput, EventSourceInput } from '@fullcalendar/core';
import { Frequency, Options } from 'rrule/dist/esm/types';
import { LIST_CONTENT_SETUP } from './list-binding/registry';
import { CALENDAR_CONTENT_SETUP } from './calendar-binding/registry';

dayjs.extend(dayOfYear);

export class CalendarInteractionPlugin implements CalendarPlugin {
  constructor(private calendarInstance: CalendarComponent) {}

  init(): CalendarPluginInstanceDetails {
    return {
      id: 'CalendarInteractionPlugin',
    };
  }

  onDateSelect(dateClickInfo: DateSelection): void {
    const calendarConfig = this.calendarInstance.config();
    if (calendarConfig !== undefined) {
      const clickAction = calendarConfig.calendarClickAction;
      if (clickAction) {
        switch (clickAction['@type']) {
          case 'open-routed-content': {
            const castedClickAction = clickAction as KokuDto.CalendarOpenRoutedContentClickActionDto;
            if (castedClickAction.route) {
              let context: CalendarContext | undefined = undefined;
              if (dateClickInfo && dateClickInfo.selectionStart) {
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

export interface CalendarUserSelectActionPluginApi {
  selectedUserDetails: BehaviorSubject<KokuDto.KokuUserDto | null>;
  selectUser(): void;
}

export class CalendarUserSelectActionPlugin implements CalendarPlugin {
  private selectedUserDetails = new BehaviorSubject<KokuDto.KokuUserDto | null>(null);
  private selectedUserSubscription: Subscription | undefined;
  componentRef = UNIQUE_REF_GENERATOR.generate();

  constructor(
    private httpClient: HttpClient,
    private toastService: ToastService,
    private modalService: ModalService,
  ) {
    this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/@self').subscribe(
      (userResult) => {
        this.selectedUserDetails.next(userResult);
      },
      () => {
        this.toastService.add('Fehler beim Laden der Nutzerinformationen', 'error');
      },
    );
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
      this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/' + payload.id).subscribe(
        (userResult) => {
          this.selectedUserDetails.next(userResult);
          newModal.close();
        },
        () => {
          this.toastService.add('Fehler beim Laden der Nutzerinformationen', 'error');
        },
      );
    });
  }

  initCalendarAction(
    currentCalendarAction: KokuDto.AbstractCalendarActionDto,
    updateCb: (updatedAction: KokuDto.AbstractCalendarActionDto) => void,
  ): void {
    switch (currentCalendarAction['@type']) {
      case 'select-user': {
        updateCb({
          ...currentCalendarAction,
          loading: true,
        });

        if (this.selectedUserSubscription) {
          this.selectedUserSubscription.unsubscribe();
        }
        this.selectedUserSubscription = this.selectedUserDetails
          .pipe(filter((value) => value !== null))
          .subscribe((selectedUserDetails) => {
            updateCb({
              ...currentCalendarAction,
              loading: false,
              imgBase64: selectedUserDetails.avatarBase64,
            });
          });

        break;
      }
    }
  }
}

export class CalendarGlobalEventPlugin implements CalendarPlugin {
  componentRef = UNIQUE_REF_GENERATOR.generate();
  componentRefInlineContent = UNIQUE_REF_GENERATOR.generate();

  constructor(private calendarInstance: CalendarComponent) {}

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
        switch (currentListener['@type']) {
          case 'close': {
            this.calendarInstance.closeInlineContent().subscribe();
            break;
          }
          default: {
            throw new Error(`Unexpected listener Type: ${currentListener['@type']}`);
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
      GLOBAL_EVENT_BUS.addGlobalEventListener(
        String(this.componentRef),
        currentEventListener.eventName,
        (eventPayload) => {
          switch (currentEventListener['@type']) {
            case 'refresh': {
              this.calendarInstance.calendarComponent()?.getApi().refetchEvents();
              break;
            }
            case 'replace-via-payload': {
              const castedEventListener =
                currentEventListener as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto;

              if (castedEventListener.sourceId === undefined) {
                throw new Error(`Missing sourceId`);
              }

              const eventSource = this.calendarInstance
                .calendarComponent()
                ?.getApi()
                .getEventSourceById(castedEventListener.sourceId);
              if (!eventSource) {
                throw new Error(`eventSource ${castedEventListener.sourceId} cannot be resolved`);
              }
              const calendarSourceFactory =
                this.calendarInstance.registeredEventSourceFactories[castedEventListener.sourceId];
              if (!calendarSourceFactory) {
                throw new Error(`EventSourceFactory ${castedEventListener.sourceId} missing`);
              }

              let lookedUpEvent = calendarSourceFactory.lookupEvent(eventPayload);
              let toBeDeleted = false;
              if (castedEventListener.deletedPath && castedEventListener.deletedExpression !== undefined) {
                toBeDeleted =
                  get(eventPayload, castedEventListener.deletedPath) === castedEventListener.deletedExpression;
              }
              if (toBeDeleted) {
                if (lookedUpEvent) {
                  lookedUpEvent.remove();
                }
              } else {
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
                      lookedUpEvent.setStart(newEvent.start, { maintainDuration: true });
                    }
                    if (newEvent.end !== undefined) {
                      lookedUpEvent.setEnd(newEvent.end, { maintainDuration: true });
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
              }

              break;
            }
            default: {
              throw new Error(`Unknown event Listener Type: ${currentEventListener['@type']}`);
            }
          }
        },
      );
    }
  }
}

export class CalendarListSourcePlugin implements CalendarPlugin {
  private userDetailsSubscriptions: Record<string, Subscription> = {};

  constructor(
    private calendarInstance: CalendarComponent,
    private httpClient: HttpClient,
    private toastService: ToastService,
    private modalService: ModalService,
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

  private callHttpEndpount(method: 'POST' | 'PUT' | 'GET' | 'DELETE', url: string, requestBody: any) {
    return this.httpClient
      .request(method, url, {
        body: requestBody,
      })
      .pipe(
        catchError((error) => {
          return new Observable((subscriber) => {
            if (error.error && error.error['@type'] === 'business-exception-with-confirmation-message') {
              const castedError = error.error as KokuDto.KokuBusinessExceptionWithConfirmationMessageDto;
              const buttons: ModalButtonType[] = [];
              for (const buttonCfg of castedError.buttons || []) {
                switch (buttonCfg['@type']) {
                  case 'close-button':
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
                        subscriber.error(error);
                      },
                    });
                    break;
                  case 'send-to-different-endpoint-button': {
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

                        this.callHttpEndpount(
                          castedButtonCfg.endpointMethod || method,
                          castedButtonCfg.endpointUrl || url,
                          requestBody,
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
                          },
                        });
                      },
                    });
                    break;
                  }
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
                },
              });
            }
          });
        }),
      );
  }

  provideEventSourceFactory(
    currentSource: KokuDto.AbstractCalendarListSourceConfigDto,
  ): CalendarPluginEventSourceFactory | void {
    if (currentSource && currentSource['@type'] === 'list') {
      const castedSource = currentSource as KokuDto.CalendarListSourceConfigDto;

      const generateEventItem = (item: Record<string, any>): EventInput => {
        let rrule: Partial<Options> | undefined = undefined;
        if (castedSource.searchOperatorHint === 'YEARLY_RECURRING') {
          rrule = {
            freq: Frequency.YEARLY,
            dtstart: get(item, castedSource.startDateFieldSelectionPath || ''),
          };
        }
        let prefix = '';
        if (castedSource.sourceItemText) {
          prefix = castedSource.sourceItemText + '\n';
        }
        const text = `${prefix}${get(item, castedSource.displayTextFieldSelectionPath || '')}`;
        if (castedSource.startDateFieldSelectionPath === undefined) {
          throw new Error(`Missing startDateFieldSelectionPath`);
        }
        const start = [get(item, castedSource.startDateFieldSelectionPath)];
        if (castedSource.startTimeFieldSelectionPath !== undefined) {
          start.push(get(item, castedSource.startTimeFieldSelectionPath));
        }
        if (castedSource.endDateFieldSelectionPath === undefined) {
          throw new Error(`Missing endDateFieldSelectionPath`);
        }
        const end = [get(item, castedSource.endDateFieldSelectionPath)];
        if (castedSource.endTimeFieldSelectionPath !== undefined) {
          end.push(get(item, castedSource.endTimeFieldSelectionPath));
        }

        const date1 = dayjs(start.join('T'));
        const date2 = dayjs(end.join('T'));
        let calculatedEndDate;
        const diffMinutes = date2.diff(date1, 'minute');
        if (diffMinutes < 60) {
          calculatedEndDate = date1.add(60, 'minutes');
        } else {
          calculatedEndDate = date2;
        }

        return {
          id: `${castedSource.id}/${get(item, castedSource.idPath || '')}`,
          title: text,
          display: text,
          start: start.join('T'),
          end: calculatedEndDate.toDate(),
          rrule: rrule,
          allDay: !(castedSource.startTimeFieldSelectionPath && castedSource.endTimeFieldSelectionPath),
          className: 'calendar-item',
          extendedProps: { kokuColor: castedSource.sourceItemColor },
          item: item,
          editable: castedSource.editable !== false,
          onClickHandler: (event: { item: Record<string, never> }) => {
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
                      case 'item-value': {
                        const castedCurrentParam =
                          currentParam as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto;
                        if (castedCurrentParam.valuePath === undefined) {
                          throw new Error(`Missing valuePath`);
                        }
                        url = url.replaceAll(
                          currentParam.param,
                          String(
                            get(
                              {
                                ...event.item,
                              },
                              castedCurrentParam.valuePath,
                            ),
                          ),
                        );
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`);
                      }
                    }
                  }

                  if (castedActionType.route) {
                    this.calendarInstance.openRoutedContent(url.split('/'));
                  }
                  break;
                }
                default: {
                  throw new Error(`Unknown item click action type ${castedSource.clickAction['@type']}`);
                }
              }
            }
          },
          onDropHandler: (event: {
            item: Record<string, never>;
            newStart: Date;
            newEnd: Date;
            allDay: boolean;
            revert: () => void;
            setLoading: (loading: boolean) => void;
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
                      case 'item-value': {
                        const castedCurrentParam =
                          currentParam as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto;
                        if (castedCurrentParam.valuePath === undefined) {
                          throw new Error(`Missing valuePath`);
                        }
                        url = url.replaceAll(
                          currentParam.param,
                          String(
                            get(
                              {
                                ...event.item,
                              },
                              castedCurrentParam.valuePath,
                            ),
                          ),
                        );
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`);
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
                  set(requestBody, castedActionType.startDatePath, dayjs(event.newStart).format('YYYY-MM-DD'));
                  set(requestBody, castedActionType.startTimePath, dayjs(event.newStart).format('HH:mm'));
                  if (castedActionType.endDatePath) {
                    set(requestBody, castedActionType.endDatePath, dayjs(event.newEnd).format('YYYY-MM-DD'));
                  }
                  if (castedActionType.endTimePath) {
                    set(requestBody, castedActionType.endTimePath, dayjs(event.newEnd).format('HH:mm'));
                  }

                  for (const [source, target] of Object.entries(castedActionType.valueMapping || {})) {
                    set(requestBody, target, get(event.item, source, null));
                  }

                  event.setLoading(true);
                  this.callHttpEndpount(castedActionType.method, url, requestBody).subscribe({
                    next: (response) => {
                      event.setLoading(false);
                      for (const currentEvent of castedActionType.successEvents || []) {
                        switch (currentEvent['@type']) {
                          case 'propagate-global-event': {
                            const castedEvent =
                              currentEvent as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto;
                            if (!castedEvent.eventName) {
                              throw new Error(`Missing eventName`);
                            }
                            GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, response);
                            break;
                          }
                          default: {
                            throw new Error(`Unknown event type ${currentEvent['@type']}`);
                          }
                        }
                      }
                      this.toastService.add(`Erfolgreich gespeichert`, 'success');
                    },
                    error: () => {
                      event.setLoading(false);
                      event.revert();
                      this.toastService.add(
                        `Es ist ein Fehler bei der Anfrage aufgetreten. Versuche es später erneut!`,
                        'error',
                      );
                    },
                  });
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
            item: Record<string, never>;
            newEnd: Date;
            allDay: boolean;
            revert: () => void;
            setLoading: (loading: boolean) => void;
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
                      case 'item-value': {
                        const castedCurrentParam =
                          currentParam as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto;
                        if (castedCurrentParam.valuePath === undefined) {
                          throw new Error(`Missing valuePath`);
                        }
                        url = url.replaceAll(
                          currentParam.param,
                          String(
                            get(
                              {
                                ...event.item,
                              },
                              castedCurrentParam.valuePath,
                            ),
                          ),
                        );
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`);
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
                  set(requestBody, castedActionType.endDatePath, dayjs(event.newEnd).format('YYYY-MM-DD'));
                  set(requestBody, castedActionType.endTimePath, dayjs(event.newEnd).format('HH:mm'));

                  for (const [source, target] of Object.entries(castedActionType.valueMapping || {})) {
                    set(requestBody, target, get(event.item, source, null));
                  }

                  event.setLoading(true);
                  this.callHttpEndpount(castedActionType.method, url, requestBody).subscribe({
                    next: (response) => {
                      event.setLoading(false);
                      for (const currentEvent of castedActionType.successEvents || []) {
                        switch (currentEvent['@type']) {
                          case 'propagate-global-event': {
                            const castedEvent =
                              currentEvent as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto;
                            if (!castedEvent.eventName) {
                              throw new Error(`Missing eventName`);
                            }
                            GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, response);
                            break;
                          }
                          default: {
                            throw new Error(`Unknown event type ${currentEvent['@type']}`);
                          }
                        }
                      }
                      this.toastService.add(`Erfolgreich gespeichert`, 'success');
                    },
                    error: () => {
                      event.setLoading(false);
                      event.revert();
                      this.toastService.add(
                        `Es ist ein Fehler bei der Anfrage aufgetreten. Versuche es später erneut!`,
                        'error',
                      );
                    },
                  });
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
      };

      const generateEventSource = (): EventSourceInput => {
        return {
          id: castedSource.id,
          events: (arg, successCallback, failureCallback) => {
            const loadList = (fieldSelection: Set<string>, fieldPredicates: Record<string, KokuDto.ListFieldQuery>) => {
              const query: KokuDto.ListQuery = {
                fieldSelection: [...fieldSelection],
                limit: 100,
                page: 0,
                fieldPredicates: fieldPredicates,
              };

              if (castedSource.sourceUrl) {
                this.httpClient.post<KokuDto.ListPage>(castedSource.sourceUrl, query).subscribe(
                  (result) => {
                    const results: EventInput[] = [];

                    for (const currentListItem of result.results || []) {
                      results.push(generateEventItem(currentListItem.values || {}));
                    }

                    successCallback(results);
                  },
                  (err) => {
                    failureCallback(err);
                  },
                );
              }
            };

            const fieldSelection: Set<string> = new Set<string>();
            const fieldPredicates: Record<string, KokuDto.ListFieldQuery> = {};

            const isRecurring = castedSource.searchOperatorHint === 'YEARLY_RECURRING';

            const startAndEndDOYOrGroupIdentifier =
              isRecurring && dayjs(arg.start).format('MM-DD') > dayjs(arg.end).format('MM-DD')
                ? 'startGTend'
                : undefined;

            if (castedSource.startDateFieldSelectionPath) {
              fieldSelection.add(castedSource.startDateFieldSelectionPath);
              fieldPredicates[castedSource.startDateFieldSelectionPath] = {
                predicates: [
                  {
                    searchExpression: dayjs(arg.end).format('YYYY-MM-DD'),
                    searchOperator: 'LESS_OR_EQ',
                    searchOperatorHint: castedSource.searchOperatorHint,
                    orGroupIdentifier: startAndEndDOYOrGroupIdentifier,
                  },
                  ...((fieldPredicates[castedSource.startDateFieldSelectionPath] || {}).predicates || []),
                ],
              };
            }
            if (castedSource.endDateFieldSelectionPath) {
              fieldSelection.add(castedSource.endDateFieldSelectionPath);
              fieldPredicates[castedSource.endDateFieldSelectionPath] = {
                predicates: [
                  {
                    searchExpression: dayjs(arg.start).format('YYYY-MM-DD'),
                    searchOperator: 'GREATER_OR_EQ',
                    searchOperatorHint: castedSource.searchOperatorHint,
                    orGroupIdentifier: startAndEndDOYOrGroupIdentifier,
                  },
                  ...((fieldPredicates[castedSource.endDateFieldSelectionPath] || {}).predicates || []),
                ],
              };
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
                    negate: true,
                  },
                  ...((fieldPredicates[castedSource.deletedFieldSelectionPath] || {}).predicates || []),
                ],
              };
            }
            for (const currentAdditionalFieldSelectionPath of castedSource.additionalFieldSelectionPaths || []) {
              fieldSelection.add(currentAdditionalFieldSelectionPath);
            }
            const userIdFieldSelectionPath = castedSource.userIdFieldSelectionPath;
            if (userIdFieldSelectionPath) {
              fieldSelection.add(userIdFieldSelectionPath);

              const calendarActionPluginApi = this.calendarInstance.getPluginApi(
                'CalendarUserSelectActionPlugin',
              ) as CalendarUserSelectActionPluginApi;

              let firstCall = true;
              const sourceId = castedSource.id;
              if (sourceId === undefined) {
                throw new Error('Expected source id');
              }
              const oldSubscription = this.userDetailsSubscriptions[sourceId];
              if (oldSubscription) {
                oldSubscription.unsubscribe();
              }
              this.userDetailsSubscriptions[sourceId] = calendarActionPluginApi.selectedUserDetails
                .pipe(filter((value) => value !== null))
                .subscribe((userDetails) => {
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
                        ...((fieldPredicates[userIdFieldSelectionPath] || {}).predicates || []),
                      ],
                    };
                    loadList(fieldSelection, fieldPredicates);
                  } else {
                    this.calendarInstance.calendarComponent()?.getApi().getEventSourceById(sourceId)?.refetch();
                  }
                  firstCall = false;
                });
            } else {
              loadList(fieldSelection, fieldPredicates);
            }
          },
        };
      };
      const lookupEvent = (eventPayload: never): any => {
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
        lookupEvent,
      };
    }
  }
}

export class CalendarHolidaySourcePlugin implements CalendarPlugin {
  constructor(
    private calendarInstance: CalendarComponent,
    private httpClient: HttpClient,
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
          className: 'calendar-item',
          extendedProps: { kokuColor: castedSource.sourceItemColor },
          item: item,
        };
      };

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
            };

            if (this.userRegion) {
              afterRegionLoaded(this.userRegion);
            } else {
              this.httpClient.get<KokuDto.KokuUserRegionDto>('/services/users/users/@self/region').subscribe(
                (regionResult) => {
                  this.userRegion = regionResult;
                  afterRegionLoaded(regionResult);
                },
                (err) => {
                  failureCallback(err);
                },
              );
            }
          },
        };
      };
      const lookupEvent = (eventPayload: HolidaysTypes.Holiday): any => {
        return this.calendarInstance
          .calendarComponent()
          ?.getApi()
          .getEventById(`${castedSource.id}/${eventPayload.start}`);
      };

      return {
        generateEventSource,
        generateEventItem,
        lookupEvent,
      };
    }
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
