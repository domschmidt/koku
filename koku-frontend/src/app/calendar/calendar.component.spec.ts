import { TestBed } from '@angular/core/testing';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { ModalService } from '../modal/modal.service';
import { CALENDAR_PLUGIN, CalendarComponent, CalendarPlugin } from './calendar.component';

describe('CalendarComponent', () => {
  it('orchestrates plugins, sources, routes, modals and calendar interactions', async () => {
    const routerEvents = new Subject<unknown>();
    const router = {
      url: '/calendar/customers/42',
      events: routerEvents,
      navigate: vi.fn(() => Promise.resolve(true)),
      navigateByUrl: vi.fn(() => Promise.resolve(true)),
    };
    const modal = { close: vi.fn() };
    const modalService = {
      add: vi.fn(() => modal),
      update: vi.fn(),
      close: vi.fn(),
    };
    const eventSource = { id: 'customers', events: [] };
    const eventFactory = {
      generateEventSource: vi.fn(() => eventSource),
      generateEventItem: vi.fn(),
      lookupEvent: vi.fn(),
    };
    const plugin: CalendarPlugin = {
      init: vi.fn(() => ({ id: 'test-plugin', api: { ready: true } })),
      destroy: vi.fn(),
      onDateSelect: vi.fn(),
      onRoutedInlineContentOpened: vi.fn(),
      onRoutedInlineContentClose: vi.fn(),
      afterConfigLoaded: vi.fn(),
      provideEventSourceFactory: vi.fn(() => eventFactory),
      initCalendarAction: vi.fn((action, update) => update({ ...action, text: 'Updated' } as any)),
    };
    const pluginFactory = vi.fn(() => plugin);
    const config = {
      id: 'main',
      listSources: [{ '@type': 'http', id: 'customers' }],
      calendarActions: [{ '@type': 'button', id: 'create', text: 'Create' }],
      routedContents: [
        {
          '@type': 'routed-inline-content',
          route: 'customers/:id',
          itemId: ':id',
          inlineContent: { '@type': 'formular' },
        },
      ],
    } as any;
    localStorage.setItem('calendar-settings-main', JSON.stringify({ hiddenSources: [], viewMode: 'WEEK' }));

    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        { provide: Router, useValue: router },
        { provide: ModalService, useValue: modalService },
        { provide: CALENDAR_PLUGIN, useValue: pluginFactory },
      ],
    })
      .overrideComponent(CalendarComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarComponent);
    fixture.componentRef.setInput('config', config);
    fixture.componentRef.setInput('contentSetup', {
      inlineContentRegistry: {},
      actionRegistry: {},
      modalContentRegistry: {},
    });
    fixture.componentRef.setInput('parentRoutePath', '/calendar');
    fixture.componentRef.setInput('urlSegments', { tenant: 'one' });
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(pluginFactory).toHaveBeenCalledWith(component);
    expect(plugin.afterConfigLoaded).toHaveBeenCalledWith(config);
    expect(eventFactory.generateEventSource).toHaveBeenCalled();
    expect(component.fullCalendarOptions().initialView).toBe('timeGridWeek');
    expect(component.calendarActions()['create'].text).toBe('Updated');
    expect(component.getPluginApi('test-plugin')).toEqual({ ready: true });
    expect(component.getPluginApiFromAction('test-plugin')).toEqual({ ready: true });
    component.openRoutedContentFromAction(['customers', '45']);
    expect(() => component.getPluginApi('missing')).toThrow('Plugin id not found missing');
    expect(component.activeContent()?.id).toBe('42');
    expect(modalService.add).toHaveBeenCalled();
    expect(plugin.onRoutedInlineContentOpened).toHaveBeenCalled();

    const options = component.fullCalendarOptions();
    options.dateClick?.({ date: new Date(2026, 6, 13), allDay: true } as any);
    options.select?.({ start: new Date(2026, 6, 13), end: new Date(2026, 6, 14), allDay: false } as any);
    expect(plugin.onDateSelect).toHaveBeenCalledTimes(2);
    options.loading?.(true);
    expect(component.loading()).toBe(true);

    component.openInlineContent({
      content: { '@type': 'list' } as any,
      id: '43',
      parentRoutePath: '/calendar/customers/43',
      urlSegments: { id: '43' },
    });
    expect(modalService.update).toHaveBeenCalledWith(modal, expect.objectContaining({ fullscreen: true }));
    component.openRoutedContent(['customers', '44'], { selectionStartDate: '2026-07-13' } as any);
    expect(router.navigate).toHaveBeenCalledWith(
      ['calendar', 'customers', '44'],
      expect.objectContaining({ queryParams: expect.any(Object) }),
    );

    router.url = '/calendar';
    routerEvents.next(new NavigationEnd(1, '/calendar/customers/42', '/calendar'));
    expect(modalService.close).toHaveBeenCalledWith(modal);
    expect(component.activeContent()).toBeNull();

    component.openInlineContent({ content: { '@type': 'formular' } as any, id: null, urlSegments: {} });
    const latestModalSetup = (modalService.add.mock.calls as unknown as [any][]).at(-1)?.[0];
    const closeRequested = latestModalSetup.onCloseRequested as () => void;
    latestModalSetup.clickOutside();
    closeRequested();
    await Promise.resolve();
    expect(modal.close).toHaveBeenCalled();
    expect(plugin.onRoutedInlineContentClose).toHaveBeenCalled();

    expect(component.formatDate(new Date(2026, 6, 13), 'DAY')).toBe('2026-07-13');
    expect(component.formatDate(new Date(2026, 6, 13), 'MONTH')).toBe('2026-07');
    expect(component.formatDate(new Date(2026, 0, 1), 'WEEK')).toMatch(/^2026-W\d{2}$/);
    expect(component.formatDate(new Date(2026, 0, 1), 'UNKNOWN' as any)).toBe('');
    expect(component.getView('DAY')).toBe('timeGridDay');
    expect(component.getView('WEEK')).toBe('timeGridWeek');
    expect(component.getView('MONTH')).toBe('dayGridMonth');

    const api = {
      gotoDate: vi.fn(),
      changeView: vi.fn(),
      addEventSource: vi.fn(),
      getEventSourceById: vi.fn(() => ({ remove: vi.fn() })),
    };
    (component.calendarComponent as any) = () => ({ getApi: () => api });
    component.viewMode.set('DAY');
    component.dateChanged('2026-07-13');
    component.viewMode.set('WEEK');
    component.dateChanged('2026-W29');
    component.dateChanged(null);
    component.changeViewMode('DAY');
    expect(api.gotoDate).toHaveBeenCalledTimes(2);
    expect(api.changeView).toHaveBeenCalledWith('timeGridDay');

    component.sourceToggled('customers');
    expect(api.getEventSourceById).toHaveBeenCalledWith('customers');
    component.sourceToggled('customers');
    expect(api.addEventSource).toHaveBeenCalledWith(eventSource);
    expect(JSON.parse(localStorage.getItem('calendar-settings-main') || '{}')).toEqual(
      expect.objectContaining({ viewMode: 'WEEK' }),
    );

    const el = document.createElement('div');
    const setProp = vi.spyOn(el.style, 'setProperty');
    options.eventDidMount?.({
      el,
      event: { id: 'customer 42', extendedProps: { kokuColor: 'red' } },
    } as any);
    expect(el.dataset['testid']).toBe('calendar-event-customer-42');
    expect(setProp).toHaveBeenCalled();

    const event = {
      start: new Date(),
      end: new Date(),
      allDay: false,
      classNames: [] as string[],
      extendedProps: {
        item: { id: 42 },
        onClickHandler: vi.fn(),
        onDropHandler: vi.fn(),
        onResizeHandler: vi.fn(),
      },
      setProp: vi.fn(),
    };
    options.eventClick?.({ event } as any);
    options.eventDrop?.({ event, revert: vi.fn() } as any);
    options.eventResize?.({ event, revert: vi.fn() } as any);
    expect(event.extendedProps.onClickHandler).toHaveBeenCalled();
    expect(event.extendedProps.onDropHandler).toHaveBeenCalled();
    expect(event.extendedProps.onResizeHandler).toHaveBeenCalled();
    const setLoading = event.extendedProps.onDropHandler.mock.calls[0][0].setLoading;
    event.classNames = ['calendar-item--loading'];
    setLoading(true);
    setLoading(false);
    const resizeSetLoading = event.extendedProps.onResizeHandler.mock.calls[0][0].setLoading;
    event.classNames = ['calendar-item--loading'];
    resizeSetLoading(true);
    resizeSetLoading(false);
    expect(event.setProp).toHaveBeenCalledTimes(4);

    const internal = component as any;
    expect(() => internal.openMatchedRoutedContent({ '@type': 'unknown' }, {})).not.toThrow();
    expect(() => internal.openMatchedRoutedContent({ '@type': 'routed-inline-content' }, {})).not.toThrow();
    internal.openMatchedRoutedContent(config.routedContents[0], { ':id': '42' });
    internal.openMatchedRoutedContent(config.routedContents[0], { ':id': '42' });

    options.datesSet?.({ view: { type: 'dayGridMonth', currentStart: new Date(2026, 6, 1) } } as any);
    options.datesSet?.({ view: { type: 'timeGridWeek', currentStart: new Date(2026, 6, 6) } } as any);
    options.datesSet?.({ view: { type: 'timeGridDay', currentStart: new Date(2026, 6, 13) } } as any);
    expect(component.viewMode()).toBe('DAY');

    const removeListener = vi.spyOn(GLOBAL_EVENT_BUS, 'removeGlobalEventListener');
    fixture.destroy();
    expect(removeListener).toHaveBeenCalledWith(component.componentRef);
    expect(plugin.destroy).toHaveBeenCalled();
    vi.restoreAllMocks();
  });

  it('rejects duplicate plugin and action identifiers and tolerates malformed settings', async () => {
    localStorage.setItem('calendar-settings-errors', '{broken');
    const duplicatePlugin = () => ({ init: () => ({ id: 'duplicate' }) });
    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        { provide: Router, useValue: { url: '/', events: new Subject(), navigateByUrl: vi.fn() } },
        { provide: ModalService, useValue: { add: vi.fn(), close: vi.fn() } },
        { provide: CALENDAR_PLUGIN, useValue: [duplicatePlugin, duplicatePlugin] },
      ],
    })
      .overrideComponent(CalendarComponent, { set: { template: '' } })
      .compileComponents();
    expect(() => TestBed.createComponent(CalendarComponent)).toThrow('Duplicated calendar plugin id duplicate');

    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        { provide: Router, useValue: { url: '/', events: new Subject(), navigateByUrl: vi.fn() } },
        { provide: ModalService, useValue: { add: vi.fn(), close: vi.fn() } },
      ],
    })
      .overrideComponent(CalendarComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarComponent);
    fixture.componentRef.setInput('contentSetup', { inlineContentRegistry: {}, actionRegistry: {} });
    fixture.componentRef.setInput('config', { id: 'errors', calendarActions: [] });
    fixture.detectChanges();
    const createIndex = (fixture.componentInstance as any).createCalendarActionIndex.bind(fixture.componentInstance);
    expect(() => createIndex([{ id: 'same' }, { id: 'same' }])).toThrow('Duplicated calendar action id same');
    expect(() => createIndex([{}])).toThrow('Missing calendar action id');
  });
});
