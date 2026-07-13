import { BehaviorSubject, of, throwError } from 'rxjs';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { describe, expect, it, vi } from 'vitest';
import {
  CalendarGlobalEventPlugin,
  CalendarHolidaySourcePlugin,
  CalendarInteractionPlugin,
  CalendarListSourcePlugin,
  CalendarUserSelectActionPlugin,
  CALENDAR_PLUGIN_PROVIDERS,
} from './calendar.plugins';
import { GLOBAL_EVENT_BUS } from './events/global-events';
import { ToastService } from './toast/toast.service';
import { ModalService } from './modal/modal.service';

const eventApi = () => ({
  allDay: false,
  classNames: ['calendar-item'],
  remove: vi.fn(),
  setAllDay: vi.fn(),
  setDates: vi.fn(),
  setStart: vi.fn(),
  setEnd: vi.fn(),
  setProp: vi.fn(),
  setExtendedProp: vi.fn(),
});

const calendarHarness = () => {
  const existingEvent = eventApi();
  const eventSource = { refetch: vi.fn() };
  const api = {
    refetchEvents: vi.fn(),
    getEventSourceById: vi.fn(() => eventSource),
    getEventById: vi.fn(() => existingEvent),
    addEvent: vi.fn(() => eventApi()),
  };
  const calendar = {
    config: signal<any>(undefined),
    openRoutedContent: vi.fn(),
    closeInlineContent: vi.fn(() => of(undefined)),
    calendarComponent: vi.fn(() => ({ getApi: () => api })),
    registeredEventSourceFactories: {} as Record<string, any>,
    getPluginApi: vi.fn(),
  };
  return { calendar, api, existingEvent, eventSource };
};

describe('CalendarInteractionPlugin', () => {
  it('opens configured routed content with a complete selection context', () => {
    const { calendar } = calendarHarness();
    const plugin = new CalendarInteractionPlugin(calendar as any);
    expect(plugin.init()).toEqual({ id: 'CalendarInteractionPlugin' });
    plugin.onDateSelect({} as any);
    calendar.config.set({
      calendarClickAction: { '@type': 'open-routed-content', route: 'appointments/new' },
    });
    plugin.onDateSelect({
      selectionStart: new Date('2026-07-13T09:30:00'),
      selectionEnd: new Date('2026-07-13T10:45:00'),
    } as any);
    expect(calendar.openRoutedContent).toHaveBeenCalledWith(
      ['appointments', 'new'],
      expect.objectContaining({
        selectionStartDate: '2026-07-13',
        selectionStartTime: '09:30',
        selectionEndTime: '10:45',
      }),
    );
    calendar.config.set({ calendarClickAction: { '@type': 'unknown' } });
    expect(() => plugin.onDateSelect({} as any)).toThrow('Unknown ClickAction Type unknown');
  });
});

describe('CalendarUserSelectActionPlugin', () => {
  it('loads the current user, updates actions and switches users through the modal', () => {
    const users: Record<string, any> = {
      '/services/users/users/@self': { id: 1, avatarBase64: 'self' },
      '/services/users/users/2': { id: 2, avatarBase64: 'second' },
    };
    const http = { get: vi.fn((url: string) => of(users[url])) };
    const toast = { add: vi.fn() };
    const modal = { uid: 1, close: vi.fn(), update: vi.fn() };
    let modalConfig: any;
    const modalService = {
      add: vi.fn((config) => {
        modalConfig = config;
        return modal;
      }),
    };
    const plugin = new CalendarUserSelectActionPlugin(http as any, toast as any, modalService as any);
    const details = plugin.init();
    const updates: any[] = [];
    plugin.initCalendarAction({ '@type': 'other' } as any, (action) => updates.push(action));
    plugin.initCalendarAction({ '@type': 'select-user', title: 'User' } as any, (action) => updates.push(action));
    expect(updates).toEqual([
      expect.objectContaining({ loading: true }),
      expect.objectContaining({ loading: false, imgBase64: 'self' }),
    ]);
    expect((details.api as any).selectedUserDetails.value.id).toBe(1);
    (details.api as any).selectUser();
    modalConfig.clickOutside();

    plugin.selectUser();
    GLOBAL_EVENT_BUS.propagateGlobalEvent('user-selected', { id: 2 });
    expect(http.get).toHaveBeenCalledWith('/services/users/users/2');
    expect(modal.close).toHaveBeenCalled();
    modalConfig.clickOutside();
    modalConfig.onCloseRequested();
    plugin.destroy();
    GLOBAL_EVENT_BUS.removeGlobalEventListener(plugin.componentRef);
  });

  it('reports failures while loading the current or selected user and replaces action subscriptions', () => {
    const http = { get: vi.fn(() => throwError(() => new Error('failed'))) };
    const toast = { add: vi.fn() };
    let modalConfig: any;
    const modal = { close: vi.fn() };
    const plugin = new CalendarUserSelectActionPlugin(
      http as any,
      toast as any,
      { add: vi.fn((config) => ((modalConfig = config), modal)) } as any,
    );
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Laden der Nutzerinformationen', 'error');
    const first = vi.fn();
    const second = vi.fn();
    plugin.initCalendarAction({ '@type': 'select-user' } as any, first);
    plugin.initCalendarAction({ '@type': 'select-user' } as any, second);
    plugin.selectUser();
    GLOBAL_EVENT_BUS.propagateGlobalEvent('user-selected', { id: 9 });
    expect(toast.add).toHaveBeenCalledTimes(2);
    modalConfig.clickOutside();
    modalConfig.onCloseRequested();
    expect(modal.close).toHaveBeenCalledTimes(2);
    plugin.destroy();
    GLOBAL_EVENT_BUS.removeGlobalEventListener(plugin.componentRef);
  });
});

describe('CalendarGlobalEventPlugin', () => {
  it('refreshes, updates, deletes and inserts events from global payloads', () => {
    vi.useFakeTimers();
    const { calendar, api, existingEvent } = calendarHarness();
    const generated = {
      title: 'Updated',
      display: 'auto',
      className: 'calendar-item',
      start: '2026-07-13T09:00',
      end: '2026-07-13T10:30',
      allDay: true,
    };
    const factory = {
      lookupEvent: vi.fn(() => existingEvent),
      generateEventItem: vi.fn(() => generated),
    };
    calendar.registeredEventSourceFactories['appointments'] = factory;
    const plugin = new CalendarGlobalEventPlugin(calendar as any);
    expect(plugin.init()).toEqual({ id: 'CalendarGlobalEventPlugin' });
    plugin.afterConfigLoaded({
      globalEventListeners: [
        { '@type': 'refresh', eventName: 'refresh' },
        { '@type': 'replace-via-payload', eventName: 'updated', sourceId: 'appointments' },
        {
          '@type': 'replace-via-payload',
          eventName: 'deleted',
          sourceId: 'appointments',
          deletedPath: 'deleted',
          deletedExpression: true,
        },
      ],
    } as any);
    GLOBAL_EVENT_BUS.propagateGlobalEvent('refresh', {});
    GLOBAL_EVENT_BUS.propagateGlobalEvent('updated', { id: 1 });
    expect(api.refetchEvents).toHaveBeenCalled();
    expect(existingEvent.setAllDay).toHaveBeenCalledWith(true);
    expect(existingEvent.setDates).toHaveBeenCalled();
    expect(existingEvent.setExtendedProp).toHaveBeenCalledWith('item', { id: 1 });
    vi.runAllTimers();

    GLOBAL_EVENT_BUS.propagateGlobalEvent('deleted', { id: 1, deleted: true });
    expect(existingEvent.remove).toHaveBeenCalled();

    (factory.lookupEvent as any).mockReturnValue(undefined);
    GLOBAL_EVENT_BUS.propagateGlobalEvent('updated', { id: 2 });
    expect(api.addEvent).toHaveBeenCalledWith(generated, 'appointments');

    plugin.onRoutedInlineContentOpened({
      globalEventListeners: [{ '@type': 'close', eventName: 'close-inline' }],
    } as any);
    GLOBAL_EVENT_BUS.propagateGlobalEvent('close-inline', {});
    expect(calendar.closeInlineContent).toHaveBeenCalled();
    plugin.onRoutedInlineContentClose({ id: 'details' } as any);
    plugin.destroy();
    vi.useRealTimers();
  });

  it('validates listener/source contracts and handles recurrence and partial date updates', () => {
    vi.useFakeTimers();
    const { calendar, api, existingEvent } = calendarHarness();
    const plugin = new CalendarGlobalEventPlugin(calendar as any);
    expect(() => plugin.afterConfigLoaded({ globalEventListeners: [{}] } as any)).toThrow('Missing eventName');
    expect(() => plugin.onRoutedInlineContentOpened({ globalEventListeners: [{}] } as any)).toThrow(
      'Missing eventName',
    );
    plugin.onRoutedInlineContentOpened({ globalEventListeners: [{ eventName: 'bad', '@type': 'bad' }] } as any);
    expect(() => GLOBAL_EVENT_BUS.propagateGlobalEvent('bad', {})).toThrow('Global event listener failed: bad');
    expect(() => (plugin as any).handleGlobalEventListener({ '@type': 'bad' }, {})).toThrow('Unknown event Listener');
    expect(() => (plugin as any).replaceEventViaPayload({}, {})).toThrow('Missing sourceId');
    api.getEventSourceById.mockReturnValueOnce(undefined as any);
    expect(() => (plugin as any).replaceEventViaPayload({ sourceId: 'missing' }, {})).toThrow('cannot be resolved');
    expect(() => (plugin as any).replaceEventViaPayload({ sourceId: 'appointments' }, {})).toThrow(
      'EventSourceFactory',
    );

    calendar.registeredEventSourceFactories['appointments'] = {
      lookupEvent: vi.fn(() => existingEvent),
      generateEventItem: vi
        .fn()
        .mockReturnValueOnce({ rrule: { freq: 'yearly' } })
        .mockReturnValueOnce({ start: '2026-07-14' })
        .mockReturnValueOnce({ end: '2026-07-15' }),
    };
    (plugin as any).replaceEventViaPayload({ sourceId: 'appointments' }, {});
    expect(existingEvent.remove).toHaveBeenCalled();
    (plugin as any).replaceEventViaPayload({ sourceId: 'appointments' }, {});
    expect(existingEvent.setStart).toHaveBeenCalledWith('2026-07-14', { maintainDuration: true });
    (plugin as any).replaceEventViaPayload({ sourceId: 'appointments' }, {});
    expect(existingEvent.setEnd).toHaveBeenCalledWith('2026-07-15');
    expect(() => (plugin as any).flashEvent(null)).not.toThrow();
    vi.runAllTimers();
    plugin.destroy();
    vi.useRealTimers();
  });
});

describe('CalendarListSourcePlugin', () => {
  it('queries events and executes click, drop and resize actions', () => {
    const { calendar, api } = calendarHarness();
    const response = { saved: true };
    const http = {
      post: vi.fn(() =>
        of({
          results: [
            {
              values: {
                id: 7,
                title: 'Visit',
                startDate: '2026-07-13',
                startTime: '09:00',
                endDate: '2026-07-13',
                endTime: '09:30',
                deleted: false,
              },
            },
          ],
        }),
      ),
      request: vi.fn(() => of(response)),
    };
    const toast = { add: vi.fn() };
    const plugin = new CalendarListSourcePlugin(calendar as any, http as any, toast as any, {} as any);
    expect(plugin.init()).toEqual({ id: 'CalendarListSourcePlugin' });
    const sourceConfig = {
      id: 'appointments',
      '@type': 'list',
      sourceUrl: '/appointments/query',
      idPath: 'id',
      startDateFieldSelectionPath: 'startDate',
      startTimeFieldSelectionPath: 'startTime',
      endDateFieldSelectionPath: 'endDate',
      endTimeFieldSelectionPath: 'endTime',
      displayTextFieldSelectionPath: 'title',
      deletedFieldSelectionPath: 'deleted',
      additionalFieldSelectionPaths: ['id'],
      sourceItemText: 'Customer',
      sourceItemColor: 'SUCCESS',
      clickAction: {
        '@type': 'open-routed-content',
        route: 'appointments/:id',
        params: [{ '@type': 'item-value', param: ':id', valuePath: 'id' }],
      },
      dropAction: {
        '@type': 'call-http',
        method: 'PUT',
        url: '/appointments/:id',
        urlParams: [{ '@type': 'item-value', param: ':id', valuePath: 'id' }],
        startDatePath: 'startDate',
        startTimePath: 'startTime',
        endDatePath: 'endDate',
        endTimePath: 'endTime',
        valueMapping: { id: 'id' },
        successEvents: [{ '@type': 'propagate-global-event', eventName: 'appointment-saved' }],
      },
      resizeAction: {
        '@type': 'call-http',
        method: 'PUT',
        url: '/appointments/:id',
        urlParams: [{ '@type': 'item-value', param: ':id', valuePath: 'id' }],
        endDatePath: 'endDate',
        endTimePath: 'endTime',
      },
    } as any;
    const factory = plugin.provideEventSourceFactory(sourceConfig)!;
    const item = {
      id: 7,
      title: 'Visit',
      startDate: '2026-07-13',
      startTime: '09:00',
      endDate: '2026-07-13',
      endTime: '09:30',
    };
    const event = factory.generateEventItem(item);
    expect(event).toEqual(
      expect.objectContaining({
        id: 'appointments/7',
        title: 'Customer\nVisit',
        allDay: false,
        editable: true,
      }),
    );
    expect(new Date(event.end as Date).getHours()).toBe(10);
    (event['onClickHandler'] as any)({ item });
    expect(calendar.openRoutedContent).toHaveBeenCalledWith(['appointments', '7']);

    const propagated = vi.fn();
    const dispose = GLOBAL_EVENT_BUS.addGlobalEventListener('calendar-list-spec', 'appointment-saved', propagated);
    const drop = {
      item,
      newStart: new Date('2026-07-14T11:00:00'),
      newEnd: new Date('2026-07-14T12:00:00'),
      revert: vi.fn(),
      setLoading: vi.fn(),
    };
    (event['onDropHandler'] as any)(drop);
    (event['onResizeHandler'] as any)({ ...drop, newEnd: new Date('2026-07-14T13:00:00') });
    expect(http.request).toHaveBeenCalledTimes(2);
    expect(drop.setLoading).toHaveBeenCalledWith(true);
    expect(toast.add).toHaveBeenCalledWith('Erfolgreich gespeichert', 'success');
    expect(propagated).toHaveBeenCalledWith(response);
    dispose();

    const generatedSource = factory.generateEventSource() as any;
    const success = vi.fn();
    generatedSource.events({ start: new Date('2026-07-01'), end: new Date('2026-08-01') }, success, vi.fn());
    expect(http.post).toHaveBeenCalledWith('/appointments/query', expect.objectContaining({ limit: 100, page: 0 }));
    expect(success).toHaveBeenCalledWith([expect.objectContaining({ id: 'appointments/7' })]);
    factory.lookupEvent({ id: 7 } as never);
    expect(api.getEventById).toHaveBeenCalledWith('appointments/7');
    expect(plugin.provideEventSourceFactory({ '@type': 'unknown' } as any)).toBeUndefined();
    plugin.destroy();
  });

  it('handles confirmation errors and validates mutation contracts', () => {
    const { calendar } = calendarHarness();
    let modalConfig: any;
    const modalRef = { close: vi.fn() };
    const modalService = {
      add: vi.fn((config: any) => {
        modalConfig = config;
        return modalRef;
      }),
      close: vi.fn(),
    };
    const http = { request: vi.fn(() => of({ confirmed: true })), post: vi.fn() };
    const toast = { add: vi.fn() };
    const plugin = new CalendarListSourcePlugin(calendar as any, http as any, toast as any, modalService as any);
    const subject = {
      next: vi.fn(),
      error: vi.fn(),
      complete: vi.fn(),
    };
    const businessError = {
      '@type': 'business-error-with-confirmation-message',
      headline: 'Confirm',
      confirmationMessage: 'Proceed?',
      closeOnClickOutside: true,
      buttons: [
        { '@type': 'close-button', text: 'Cancel' },
        {
          '@type': 'send-to-different-endpoint-button',
          text: 'Proceed',
          endpointMethod: 'PATCH',
          endpointUrl: '/force',
          showLoadingAnimation: true,
          showDisabledState: true,
        },
      ],
    };
    (plugin as any).openBusinessErrorModal(businessError, subject, 'PUT', '/normal', { id: 1 }, new Error('original'));
    expect(modalConfig.buttons).toHaveLength(2);
    modalConfig.clickOutside();
    expect(modalService.close).toHaveBeenCalledWith(modalRef);
    expect(subject.complete).toHaveBeenCalled();

    modalConfig.buttons[0].onClick();
    expect(subject.error).toHaveBeenCalled();
    const endpointButton = modalConfig.buttons[1];
    endpointButton.onClick(new Event('click'), {}, endpointButton);
    expect(http.request).toHaveBeenCalledWith('PATCH', '/force', { body: { id: 1 } });
    expect(endpointButton.loading).toBe(true);
    expect(endpointButton.disabled).toBe(true);
    expect(subject.next).toHaveBeenCalledWith({ confirmed: true });

    (plugin as any).httpClient = { request: vi.fn(() => throwError(() => new Error('force failed'))) };
    endpointButton.onClick(new Event('click'), {}, endpointButton);
    expect(endpointButton.loading).toBe(false);
    expect(endpointButton.disabled).toBe(false);
    expect(subject.error).toHaveBeenCalledWith(expect.objectContaining({ message: 'force failed' }));

    expect(() =>
      (plugin as any).createBusinessErrorButton({ '@type': 'unknown' }, subject, 'PUT', '/', {}, {}, () => modalRef),
    ).toThrow('Unknown button type');
    const ordinaryError = new Error('ordinary');
    const ordinaryObserver = { error: vi.fn() };
    (plugin as any).handleBusinessError(ordinaryError, 'PUT', '/', {}).subscribe(ordinaryObserver);
    expect(ordinaryObserver.error).toHaveBeenCalledWith(ordinaryError);
    (plugin as any)
      .handleBusinessError({ error: businessError }, 'PUT', '/normal', { id: 1 })
      .subscribe({ next: vi.fn(), error: vi.fn() });
    expect(modalService.add).toHaveBeenCalledTimes(2);

    const replace = (plugin as any).replaceItemValueParams.bind(plugin);
    expect(() => replace('/', [{ '@type': 'item-value', valuePath: 'id' }], { id: 1 })).toThrow('Missing param');
    expect(() => replace('/', [{ '@type': 'unknown', param: ':id' }], { id: 1 })).toThrow('Unknown param type');
    expect(() => replace('/', [{ '@type': 'item-value', param: ':id' }], { id: 1 })).toThrow('Missing valuePath');
    expect(() => (plugin as any).propagateSuccessEvents([{ '@type': 'unknown' }], {})).toThrow('Unknown event type');
    expect(() => (plugin as any).propagateSuccessEvents([{ '@type': 'propagate-global-event' }], {})).toThrow(
      'Missing eventName',
    );
    expect(() => (plugin as any).handleItemClick({}, {})).not.toThrow();
    expect(() => (plugin as any).handleItemClick({ clickAction: { '@type': 'unknown' } }, {})).toThrow(
      'Unknown item click action',
    );
    expect(() =>
      (plugin as any).handleItemClick({ clickAction: { '@type': 'open-routed-content' } }, {}),
    ).not.toThrow();
    expect(() => (plugin as any).addStartDatePredicate({}, {}, new Set(), {}, undefined)).not.toThrow();

    const mutationEvent = { revert: vi.fn(), setLoading: vi.fn(), item: {}, newStart: new Date(), newEnd: new Date() };
    (plugin as any).handleDrop({}, mutationEvent);
    (plugin as any).handleDrop({ dropAction: { '@type': 'unknown' } }, mutationEvent);
    (plugin as any).handleResize({}, mutationEvent);
    (plugin as any).handleResize({ resizeAction: { '@type': 'unknown' } }, mutationEvent);
    expect(mutationEvent.revert).toHaveBeenCalledTimes(4);
    expect(() => (plugin as any).executeDropAction({}, mutationEvent)).toThrow('Missing method');
    expect(() => (plugin as any).executeDropAction({ method: 'PUT' }, mutationEvent)).toThrow('Missing startDatePath');
    expect(() => (plugin as any).executeDropAction({ method: 'PUT', startDatePath: 'start' }, mutationEvent)).toThrow(
      'Missing startTimePath',
    );
    expect(() => (plugin as any).executeResizeAction({}, mutationEvent)).toThrow('Missing method');
    expect(() => (plugin as any).executeResizeAction({ method: 'PUT' }, mutationEvent)).toThrow('Missing endDatePath');
    expect(() => (plugin as any).executeResizeAction({ method: 'PUT', endDatePath: 'end' }, mutationEvent)).toThrow(
      'Missing endTimePath',
    );

    (plugin as any).httpClient = { request: vi.fn(() => throwError(() => new Error('failed'))) };
    (plugin as any).executeCalendarHttpAction('PUT', '/', {}, undefined, mutationEvent);
    expect(mutationEvent.setLoading).toHaveBeenLastCalledWith(false);
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Fehler'), 'error');
  });

  it('validates list source ids, item dates and event lookup values', () => {
    const { calendar } = calendarHarness();
    const plugin = new CalendarListSourcePlugin(calendar as any, {} as any, {} as any, {} as any);
    expect(() => (plugin as any).requireListSourceId({})).toThrow('Expected source id');
    expect(() => (plugin as any).resolveEventDateParts({}, undefined, undefined, 'start')).toThrow(
      'Missing startDateFieldSelectionPath',
    );
    expect(() => (plugin as any).lookupListEvent({}, {})).toThrow('Missing idPath');
    expect(() => (plugin as any).lookupListEvent({ idPath: 'id' }, {})).toThrow('id cannot be resolved');
    expect(() => (plugin as any).addSelectedUserPredicate({}, 'user.id', {})).toThrow('user id required');
    expect((plugin as any).createRecurringRule({ searchOperatorHint: 'NORMAL' }, {})).toBeUndefined();
    expect(
      (plugin as any).createRecurringRule(
        { searchOperatorHint: 'YEARLY_RECURRING', startDateFieldSelectionPath: 'date' },
        { date: '2026-01-01' },
      ),
    ).toEqual(expect.objectContaining({ dtstart: '2026-01-01' }));
  });

  it('queries selected-user sources, refetches on user changes and forwards query failures', () => {
    const selectedUserDetails = new BehaviorSubject<any>({ id: 4 });
    const { calendar, eventSource } = calendarHarness();
    calendar.getPluginApi.mockReturnValue({ selectedUserDetails });
    const failure = new Error('query failed');
    const http = { post: vi.fn(() => throwError(() => failure)) };
    const plugin = new CalendarListSourcePlugin(calendar as any, http as any, {} as any, {} as any);
    const source = {
      '@type': 'list',
      id: 'selected',
      sourceUrl: '/appointments',
      idPath: 'id',
      userIdFieldSelectionPath: 'userId',
      startDateFieldSelectionPath: 'date',
      displayTextFieldSelectionPath: 'title',
    } as any;
    const generated = plugin.provideEventSourceFactory(source)!.generateEventSource() as any;
    const failed = vi.fn();
    generated.events({ start: new Date('2026-01-01'), end: new Date('2026-02-01') }, vi.fn(), failed);
    expect(http.post).toHaveBeenCalledWith(
      '/appointments',
      expect.objectContaining({ fieldPredicates: expect.objectContaining({ userId: expect.any(Object) }) }),
    );
    expect(failed).toHaveBeenCalledWith(failure);
    selectedUserDetails.next({ id: 5 });
    expect(eventSource.refetch).toHaveBeenCalled();
    plugin.destroy();

    const noUrl = { ...source, id: 'no-url', sourceUrl: undefined, userIdFieldSelectionPath: undefined };
    const noUrlEvents = plugin.provideEventSourceFactory(noUrl)!.generateEventSource() as any;
    expect(() => noUrlEvents.events({ start: new Date(), end: new Date() }, vi.fn(), vi.fn())).not.toThrow();
  });
});

describe('CalendarHolidaySourcePlugin', () => {
  it('loads and caches the user region while generating and looking up holidays', () => {
    const { calendar, api } = calendarHarness();
    const http = { get: vi.fn(() => of({ country: 'DE', state: 'HE' })) };
    const plugin = new CalendarHolidaySourcePlugin(calendar as any, http as any);
    expect(plugin.init()).toEqual({ id: 'CalendarHolidaySourcePlugin' });
    const factory = plugin.provideEventSourceFactory({
      '@type': 'holiday',
      id: 'holidays',
      sourceItemColor: 'WARNING',
    } as any)!;
    const generatedSource = factory.generateEventSource() as any;
    const first = vi.fn();
    generatedSource.events({ start: new Date('2026-01-01'), end: new Date('2026-12-31') }, first, vi.fn());
    expect(first).toHaveBeenCalled();
    expect(first.mock.calls[0][0][0]).toEqual(
      expect.objectContaining({ allDay: true, editable: false, display: 'background' }),
    );
    const second = vi.fn();
    generatedSource.events({ start: new Date('2027-01-01'), end: new Date('2027-12-31') }, second, vi.fn());
    expect(http.get).toHaveBeenCalledOnce();
    const holiday = first.mock.calls[0][0][0].item;
    factory.lookupEvent(holiday);
    expect(api.getEventById).toHaveBeenCalledWith(`holidays/${holiday.start}`);
    expect(plugin.provideEventSourceFactory({ '@type': 'list' } as any)).toBeUndefined();
  });

  it('forwards region failures and returns no events for users without a country', () => {
    const { calendar } = calendarHarness();
    const failure = new Error('region failed');
    const failing = new CalendarHolidaySourcePlugin(
      calendar as any,
      { get: vi.fn(() => throwError(() => failure)) } as any,
    );
    const source = { '@type': 'holiday', id: 'holidays' } as any;
    const failed = vi.fn();
    (failing.provideEventSourceFactory(source)!.generateEventSource() as any).events(
      { start: new Date('2026-01-01'), end: new Date('2026-12-31') },
      vi.fn(),
      failed,
    );
    expect(failed).toHaveBeenCalledWith(failure);

    const empty = new CalendarHolidaySourcePlugin(calendar as any, { get: vi.fn(() => of({})) } as any);
    const success = vi.fn();
    (empty.provideEventSourceFactory(source)!.generateEventSource() as any).events(
      { start: new Date('2026-01-01'), end: new Date('2026-12-31') },
      success,
      vi.fn(),
    );
    expect(success).toHaveBeenCalledWith([]);
    expect(() => (empty as any).createHolidays({ country: 'DE' })).not.toThrow();
  });
});

describe('CALENDAR_PLUGIN_PROVIDERS', () => {
  it('constructs every configured plugin factory', () => {
    TestBed.configureTestingModule({
      providers: [
        { provide: HttpClient, useValue: { get: vi.fn(() => of({})) } },
        { provide: ToastService, useValue: { add: vi.fn() } },
        { provide: ModalService, useValue: { add: vi.fn() } },
      ],
    });
    const { calendar } = calendarHarness();
    const plugins = CALENDAR_PLUGIN_PROVIDERS.map((provider) =>
      TestBed.runInInjectionContext(() => provider.useFactory()(calendar as any)),
    );

    expect(plugins).toHaveLength(CALENDAR_PLUGIN_PROVIDERS.length);
    for (const plugin of plugins) {
      expect(plugin).toBeDefined();
      plugin.destroy?.();
    }
  });
});
