import {
  Component,
  DestroyRef,
  inject,
  InjectionToken,
  input,
  OnDestroy,
  Signal,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular';

import deLocale from '@fullcalendar/core/locales/de';
import dayGridPlugin from '@fullcalendar/daygrid';
import rrulePlugin from '@fullcalendar/rrule';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { IconComponent } from '../icon/icon.component';
import { NavigationEnd, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { ModalService } from '../modal/modal.service';
import { ModalContentSetup, RenderedModalType } from '../modal/modal.type';
import { deepEqual } from '../utils/deepEqual';
import { CalendarOptions, EventInput, EventSourceInput } from '@fullcalendar/core/index.js';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { UNIQUE_REF_GENERATOR } from '../utils/uniqueRef';
import { KeyValuePipe, NgClass } from '@angular/common';
import { DateInputFieldComponent } from '../fields/input/date-input-field.component';
import { MonthInputFieldComponent } from '../fields/input/month-input-field.component';
import { WeekInputFieldComponent } from '../fields/input/week-input-field.component';
import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import advancedFormat from 'dayjs/plugin/advancedFormat';
import { parseIsoWeekValue } from '../fields/input/temporal-input.utils';
import { DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';
import { OutletDirective } from '../portal/outlet.directive';
import { CalendarActionRendererComponent } from './action-renderer/calendar-action-renderer.component';
import { colorBorderClass, colorValue } from '../utils/color.utils';
import { childRouteSegments, matchRouteSegments, replaceRouteSegments, resolvedRoutePath } from '../utils/route.utils';

dayjs.extend(isoWeek);
dayjs.extend(advancedFormat);

export interface CalendarInlineContentRenderContext {
  content: Signal<KokuDto.AbstractCalendarInlineContentDto>;
  loading: Signal<boolean>;
  contentSetup: Signal<CalendarContentSetup>;
  urlSegments: Signal<Record<string, string> | null>;
  buttonDockOutlet: Signal<OutletDirective | undefined>;
  parentRoutePath: Signal<string>;
  queryParams: Signal<Record<string, any>>;
  close(): void;
  openRoutedContent(routes: string[]): void;
}

export type CalendarInlineContentRecipeFactory = (context: CalendarInlineContentRenderContext) => DynamicRenderRecipe;

export interface CalendarActionRenderContext {
  action: Signal<KokuDto.AbstractCalendarActionDto>;
  contentSetup: Signal<CalendarContentSetup>;
  openRoutedContent(routes: string[]): void;
  getPluginApi<T = any>(id: string): T | undefined;
}

export type CalendarActionRecipeFactory = (context: CalendarActionRenderContext) => DynamicRenderRecipe;

export interface CalendarContentSetup {
  inlineContentRegistry: Partial<Record<string, CalendarInlineContentRecipeFactory>>;
  actionRegistry: Partial<Record<string, CalendarActionRecipeFactory>>;
  modalContentRegistry: Partial<ModalContentSetup>;
}

export interface CalendarInlineItem {
  content: KokuDto.AbstractCalendarInlineContentDto;
  id: string | null;
  parentRoutePath?: string;
  urlSegments: Record<string, string>;
}

export interface RenderedCalendarInlineItem extends CalendarInlineItem {
  modalRef: RenderedModalType;
}

export interface CalendarContext {
  selectionStartDate: string;
  selectionStartTime: string;
  selectionStartDateTime: string;
  selectionEndDate: string;
  selectionEndTime: string;
  selectionEndDateTime: string;
}

export interface DateSelection {
  selectionStart: Date;
  selectionEnd: Date;
  allDay: boolean;
}

export interface CalendarPluginInstanceDetails {
  id: string;
  api?: any;
}

export interface CalendarPlugin {
  init(): CalendarPluginInstanceDetails;

  destroy?(): void;

  onDateSelect?(dateClickInfo: DateSelection): void;

  onRoutedInlineContentOpened?(castedRouteContent: KokuDto.CalendarRoutedContentDto): void;

  onRoutedInlineContentClose?(content: RenderedCalendarInlineItem | null): void;

  afterConfigLoaded?(config: KokuDto.CalendarConfigDto): void;

  provideEventSourceFactory?(
    currentSource: KokuDto.AbstractCalendarListSourceConfigDto,
  ): CalendarPluginEventSourceFactory | void;

  initCalendarAction?(
    currentCalendarAction: KokuDto.AbstractCalendarActionDto,
    updateCb: (updatedAction: KokuDto.AbstractCalendarActionDto) => void,
  ): void;
}

export type CalendarPluginFactory = (instance: CalendarComponent) => CalendarPlugin;

export const CALENDAR_PLUGIN = new InjectionToken<CalendarPluginFactory | CalendarPluginFactory[]>('Calendar Plugins');

export type CalendarViewMode = 'WEEK' | 'DAY' | 'MONTH';

export interface CalendarPluginEventSourceFactory {
  generateEventSource(): EventSourceInput;

  generateEventItem(item: Record<string, any>): EventInput;

  lookupEvent(eventPayload: any): any;
}

interface CalendarPersistedSettings {
  hiddenSources?: string[];
  viewMode?: CalendarViewMode;
}

@Component({
  selector: 'calendar',
  host: { '[attr.data-testid]': "'calendar'" },
  imports: [
    FullCalendarModule,
    IconComponent,
    KeyValuePipe,
    NgClass,
    DateInputFieldComponent,
    MonthInputFieldComponent,
    WeekInputFieldComponent,
    CalendarActionRendererComponent,
  ],
  templateUrl: './calendar.component.html',
  standalone: true,
})
export class CalendarComponent implements OnDestroy {
  readonly colorBorderClass = colorBorderClass;
  readonly destroyRef = inject(DestroyRef);
  readonly router = inject(Router);
  readonly modalService = inject(ModalService);

  readonly calendarPluginsConfig = inject(CALENDAR_PLUGIN, {
    optional: true,
  });

  readonly componentRef = UNIQUE_REF_GENERATOR.generate();

  readonly calendarComponent = viewChild<FullCalendarComponent>('calendar');

  config = input.required<KokuDto.CalendarConfigDto>();
  urlSegments = input<Record<string, string> | null>(null);
  contentSetup = input.required<CalendarContentSetup>();
  parentRoutePath = input<string>('');

  activeContent = signal<RenderedCalendarInlineItem | null>(null);
  viewMode = signal<CalendarViewMode>('MONTH');
  currentStartDate = signal<Date>(new Date());
  loading = signal(false);
  hiddenSources = signal<Set<string>>(new Set<string>());
  private readonly pluginInstances: Record<string, { instance: CalendarPlugin; api: any }>;

  private routerUrlSubscription: Subscription | undefined;

  calendarActions = signal<Record<string, KokuDto.AbstractCalendarActionDto>>({});
  readonly openRoutedContentFromAction = (routes: string[]) => this.openRoutedContent(routes);
  readonly getPluginApiFromAction = <T = any>(id: string) => this.getPluginApi(id) as T | undefined;
  fullCalendarOptions = signal<CalendarOptions>({
    plugins: [dayGridPlugin, interactionPlugin, timeGridPlugin, rrulePlugin],
    height: 'auto',
    allDaySlot: true,
    locale: deLocale,
    headerToolbar: false,
    eventDidMount: ({ el, event }) => {
      el.dataset['testid'] = `calendar-event-${event.id.replace(/[^a-zA-Z0-9]+/g, '-')}`;
      const configuredColor = event.extendedProps['kokuColor'];
      const color = typeof configuredColor === 'string' ? configuredColor : undefined;
      el.style.setProperty('--koku-event-color', colorValue(color));
      el.style.setProperty('--koku-event-background-color', colorValue(color, 300));
    },
    eventResize: (args) => {
      const handler = args.event.extendedProps['onResizeHandler'];
      if (typeof handler === 'function') {
        if (args.event.end) {
          handler({
            newEnd: args.event.end,
            allDay: args.event.allDay,
            revert: args.revert,
            item: args.event.extendedProps['item'],
            setLoading: (loading: boolean) => {
              const classnameSnapshot = args.event.classNames.filter(
                (className) => className !== 'calendar-item--loading',
              );
              args.event.setProp(
                'classNames',
                loading ? [...classnameSnapshot, 'calendar-item--loading'] : classnameSnapshot,
              );
            },
          });
        }
      }
    },
    editable: true,
    eventClick: (args) => {
      const handler = args.event.extendedProps['onClickHandler'];
      if (typeof handler === 'function') {
        handler({
          item: args.event.extendedProps['item'],
        });
      }
    },
    eventDrop: (args) => {
      const handler = args.event.extendedProps['onDropHandler'];
      if (typeof handler === 'function') {
        if (args.event.start) {
          handler({
            newStart: args.event.start,
            newEnd: args.event.end,
            allDay: args.event.allDay,
            revert: args.revert,
            item: args.event.extendedProps['item'],
            setLoading: (loading: boolean) => {
              const classnameSnapshot = args.event.classNames.filter(
                (className) => className !== 'calendar-item--loading',
              );
              args.event.setProp(
                'classNames',
                loading ? [...classnameSnapshot, 'calendar-item--loading'] : classnameSnapshot,
              );
            },
          });
        }
      }
    },
    datesSet: (args) => {
      this.currentStartDate.set(args.view.currentStart);
      switch (args.view.type) {
        case 'dayGridMonth': {
          this.viewMode.set('MONTH');
          break;
        }
        case 'timeGridWeek': {
          this.viewMode.set('WEEK');
          break;
        }
        case 'timeGridDay': {
          this.viewMode.set('DAY');
          break;
        }
      }
    },
    nowIndicator: true,
    businessHours: {
      daysOfWeek: [1, 2, 3, 4, 5], // Monday - Thursday
      startTime: '08:00:00', // a start time (10am in this example)
      endTime: '20:00:00', // an end time (6pm in this example)
    },
    selectable: true,
    loading: (isLoading) => {
      this.loading.set(isLoading);
    },
  });

  registeredEventSourceFactories: Record<string, CalendarPluginEventSourceFactory> = {};
  constructor() {
    this.pluginInstances = this.createPluginInstances();

    toObservable(this.config).subscribe((config) => {
      this.applyConfig(config);
    });
  }

  private applyConfig(config: KokuDto.CalendarConfigDto): void {
    this.restorePersistedSettings();
    this.configureEventSources(config);
    this.notifyPluginsAfterConfigLoaded(config);
    this.calendarActions.set(this.createCalendarActionIndex(config.calendarActions));
    this.initCalendarActions(config.calendarActions);
    this.bindRoutedContent(config);
  }

  private restorePersistedSettings(): void {
    let persistedCalendarSettings: CalendarPersistedSettings;
    try {
      persistedCalendarSettings = JSON.parse(
        localStorage.getItem(`calendar-settings-${this.config().id}`) || '{}',
      ) as CalendarPersistedSettings;
    } catch {
      persistedCalendarSettings = {};
    }
    this.hiddenSources.set(new Set(persistedCalendarSettings.hiddenSources || []));
    this.viewMode.set(persistedCalendarSettings.viewMode || 'MONTH');
  }

  private configureEventSources(config: KokuDto.CalendarConfigDto): void {
    this.fullCalendarOptions.set({
      ...this.fullCalendarOptions(),
      eventSources: this.createEventSources(config),
      initialView: this.getView(this.viewMode()),
      dateClick: (dateClickInfo) => {
        this.notifyPluginsAboutDateSelection(dateClickInfo.date, dateClickInfo.date, dateClickInfo.allDay);
      },
      select: (dateSelectInfo) => {
        this.notifyPluginsAboutDateSelection(dateSelectInfo.start, dateSelectInfo.end, dateSelectInfo.allDay);
      },
    });
  }

  private createEventSources(config: KokuDto.CalendarConfigDto): EventSourceInput[] {
    const eventSources: EventSourceInput[] = [];
    for (const currentSource of config.listSources || []) {
      this.addEventSourceFactories(currentSource, eventSources);
    }
    return eventSources;
  }

  private addEventSourceFactories(
    currentSource: KokuDto.AbstractCalendarListSourceConfigDto,
    eventSources: EventSourceInput[],
  ): void {
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      const eventSourceFactory = currentPluginInstance.instance.provideEventSourceFactory?.(currentSource);
      if (eventSourceFactory && currentSource.id) {
        this.registeredEventSourceFactories[currentSource.id] = eventSourceFactory;
        if (!this.hiddenSources().has(currentSource.id)) {
          eventSources.push(eventSourceFactory.generateEventSource());
        }
      }
    }
  }

  private notifyPluginsAboutDateSelection(selectionStart: Date, selectionEnd: Date, allDay: boolean): void {
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      currentPluginInstance.instance.onDateSelect?.({
        selectionStart,
        selectionEnd,
        allDay,
      });
    }
  }

  private notifyPluginsAfterConfigLoaded(config: KokuDto.CalendarConfigDto): void {
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      currentPluginInstance.instance.afterConfigLoaded?.(config);
    }
  }

  private createCalendarActionIndex(
    calendarActions: KokuDto.AbstractCalendarActionDto[] | undefined,
  ): Record<string, KokuDto.AbstractCalendarActionDto> {
    const calendarActionIndex: Record<string, KokuDto.AbstractCalendarActionDto> = {};
    for (const currentCalendarAction of calendarActions || []) {
      const currentCalendarActionId = currentCalendarAction.id;
      if (!currentCalendarActionId) {
        throw new Error('Missing calendar action id');
      }
      if (calendarActionIndex[currentCalendarActionId]) {
        throw new Error(`Duplicated calendar action id ${currentCalendarActionId}`);
      }
      calendarActionIndex[currentCalendarActionId] = currentCalendarAction;
    }
    return calendarActionIndex;
  }

  private initCalendarActions(calendarActions: KokuDto.AbstractCalendarActionDto[] | undefined): void {
    for (const currentCalendarAction of calendarActions || []) {
      const currentCalendarActionId = currentCalendarAction.id;
      if (currentCalendarActionId) {
        this.initCalendarAction(currentCalendarAction, currentCalendarActionId);
      }
    }
  }

  private initCalendarAction(currentCalendarAction: KokuDto.AbstractCalendarActionDto, actionId: string): void {
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      currentPluginInstance.instance.initCalendarAction?.(currentCalendarAction, (updatedAction) => {
        this.calendarActions.set({
          ...this.calendarActions(),
          [actionId]: updatedAction,
        });
      });
    }
  }

  private bindRoutedContent(config: KokuDto.CalendarConfigDto): void {
    this.routerUrlSubscription?.unsubscribe();
    this.routerUrlSubscription = this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((evnt) => {
      if (evnt instanceof NavigationEnd) {
        this.applyRoutedContent(config);
      }
    });
    this.applyRoutedContent(config);
  }

  private applyRoutedContent(config: KokuDto.CalendarConfigDto): void {
    const routedContentMatch = this.findRoutedContent(config.routedContents);
    if (!routedContentMatch) {
      this.closeInlineContentWithoutRouteChange().subscribe();
      return;
    }

    this.openMatchedRoutedContent(routedContentMatch.content, routedContentMatch.segmentMapping);
  }

  private findRoutedContent(routedContents: KokuDto.AbstractCalendarRoutedContentDto[] | undefined): {
    content: KokuDto.AbstractCalendarRoutedContentDto;
    segmentMapping: Record<string, string>;
  } | null {
    const segments = childRouteSegments(this.router.url, this.parentRoutePath());
    for (const currentRoutedContent of routedContents || []) {
      const segmentMapping = matchRouteSegments(currentRoutedContent.route, segments);
      if (segmentMapping) {
        return { content: currentRoutedContent, segmentMapping };
      }
    }
    return null;
  }

  private openMatchedRoutedContent(
    currentRoutedContent: KokuDto.AbstractCalendarRoutedContentDto,
    segmentMapping: Record<string, string>,
  ): void {
    if (currentRoutedContent['@type'] !== 'routed-inline-content') {
      return;
    }

    const castedRouteContent = currentRoutedContent as KokuDto.CalendarRoutedContentDto;
    if (!castedRouteContent.inlineContent) {
      return;
    }

    const itemId = castedRouteContent.itemId
      ? replaceRouteSegments(castedRouteContent.itemId, segmentMapping)
      : undefined;
    if (!this.shouldOpenRoutedInlineContent(castedRouteContent, itemId, segmentMapping)) {
      return;
    }

    this.openInlineContent({
      content: castedRouteContent.inlineContent,
      id: itemId || null,
      parentRoutePath: resolvedRoutePath(this.parentRoutePath(), castedRouteContent.route, {
        ...segmentMapping,
        ...this.routeSegmentsSnapshot(),
      }),
      urlSegments: segmentMapping,
    });
    this.notifyPluginsAboutRoutedInlineContent(castedRouteContent);
  }

  private shouldOpenRoutedInlineContent(
    castedRouteContent: KokuDto.CalendarRoutedContentDto,
    itemId: string | undefined,
    segmentMapping: Record<string, string>,
  ): boolean {
    return (
      (itemId !== undefined && this.activeContent()?.id !== itemId) ||
      this.activeContent()?.content !== castedRouteContent.inlineContent ||
      !deepEqual(this.activeContent()?.urlSegments, segmentMapping)
    );
  }

  private notifyPluginsAboutRoutedInlineContent(castedRouteContent: KokuDto.CalendarRoutedContentDto): void {
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      currentPluginInstance.instance.onRoutedInlineContentOpened?.(castedRouteContent);
    }
  }

  private createPluginInstances(): Record<string, { instance: CalendarPlugin; api: any }> {
    const pluginInstances: Record<string, { instance: CalendarPlugin; api: any }> = {};
    for (const currentPlugin of this.resolvePluginFactories()) {
      const newPluginInstance = currentPlugin(this);
      const pluginInstanceDetails = newPluginInstance.init();
      if (pluginInstances[pluginInstanceDetails.id]) {
        throw new Error(`Duplicated calendar plugin id ${pluginInstanceDetails.id}`);
      }
      pluginInstances[pluginInstanceDetails.id] = {
        instance: newPluginInstance,
        api: pluginInstanceDetails.api,
      };
    }
    return pluginInstances;
  }

  private resolvePluginFactories(): CalendarPluginFactory[] {
    if (Array.isArray(this.calendarPluginsConfig)) {
      return this.calendarPluginsConfig;
    }
    if (this.calendarPluginsConfig) {
      return [this.calendarPluginsConfig];
    }
    return [];
  }

  private routeSegmentsSnapshot(): Record<string, string> {
    return this.urlSegments() ?? {};
  }

  dateChanged(value: string | null) {
    if (!value) {
      return;
    }
    const parsedDate = this.viewMode() === 'WEEK' ? parseIsoWeekValue(value) : dayjs(value);
    this.calendarComponent()?.getApi().gotoDate(parsedDate.toDate());
  }

  formatDate(date: Date, viewMode: CalendarViewMode) {
    const d = dayjs(date);
    switch (viewMode) {
      case 'WEEK': {
        const isoWeekNumber = d.isoWeek(); // Woche 1-53
        const isoWeekYear = d.isoWeekYear(); // Jahr der ISO-Woche
        return `${isoWeekYear}-W${String(isoWeekNumber).padStart(2, '0')}`;
      }

      case 'MONTH':
        return d.format('YYYY-MM'); // yyyy-MM
      case 'DAY':
        return d.format('YYYY-MM-DD'); // yyyy-MM-dd
    }
    return '';
  }

  changeViewMode(mode: CalendarViewMode) {
    this.calendarComponent()?.getApi().changeView(this.getView(mode));
    this.persistCalendarSettings();
  }

  getView(mode: CalendarViewMode) {
    switch (mode) {
      case 'DAY': {
        return 'timeGridDay';
      }
      case 'WEEK': {
        return 'timeGridWeek';
      }
      default: {
        return 'dayGridMonth';
      }
    }
  }

  openInlineContent(item: CalendarInlineItem) {
    const activeContentSnapshot = this.activeContent();
    const activeModalRef = activeContentSnapshot?.modalRef;
    if (activeModalRef) {
      const closeModal = this.createInlineModalCloseHandler(activeModalRef);
      this.modalService.update(activeModalRef, {
        dynamicContent: item.content,
        urlSegments: item.urlSegments,
        dynamicContentSetup: this.contentSetup().modalContentRegistry,
        fullscreen: true,
        parentRoutePath: item.parentRoutePath,
        clickOutside: closeModal,
        onCloseRequested: closeModal,
      });
    } else {
      const newModal = this.modalService.add({
        dynamicContent: item.content,
        urlSegments: item.urlSegments,
        dynamicContentSetup: this.contentSetup().modalContentRegistry,
        fullscreen: true,
        parentRoutePath: item.parentRoutePath,
        clickOutside: () => this.closeInlineModal(newModal),
        onCloseRequested: () => this.closeInlineModal(newModal),
      });
      this.activeContent.set({
        ...item,
        modalRef: newModal,
      });
    }
  }

  private createInlineModalCloseHandler(modalRef: RenderedModalType): () => void {
    return () => this.closeInlineModal(modalRef);
  }

  private closeInlineModal(modalRef: RenderedModalType): void {
    this.closeInlineContent().subscribe((success) => {
      if (success) {
        modalRef.close();
      }
    });
  }

  openRoutedContent(routes: string[], context?: CalendarContext) {
    this.router.navigate(
      [...this.parentRoutePath().split('/'), ...routes].filter((value) => value !== ''),
      {
        queryParams: context,
      },
    );
  }

  closeInlineContent() {
    return new Observable<boolean>((observer) => {
      this.router
        .navigateByUrl(`/${resolvedRoutePath(this.parentRoutePath(), undefined, this.urlSegments())}`)
        .then((success) => {
          if (success) {
            this.closeActiveInlineContentWithPluginNotification();
          }
          observer.next(success);
          observer.complete();
        });
    });
  }

  private closeInlineContentWithoutRouteChange() {
    return new Observable<boolean>((observer) => {
      this.closeActiveInlineContentSilently();
      observer.next(true);
      observer.complete();
    });
  }

  private closeActiveInlineContentWithPluginNotification(): void {
    const activeContentSnapshot = this.activeContent();
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      currentPluginInstance.instance.onRoutedInlineContentClose?.(activeContentSnapshot);
    }
    this.closeActiveInlineContentSilently();
  }

  private closeActiveInlineContentSilently(): void {
    const activeContentSnapshot = this.activeContent();
    if (activeContentSnapshot?.modalRef) {
      this.modalService.close(activeContentSnapshot.modalRef);
    }
    this.activeContent.set(null);
  }

  ngOnDestroy(): void {
    this.clearGlobalEventListeners();
    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
      currentPluginInstance.instance.destroy?.();
    }
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }

  getPluginApi(pluginId: string) {
    const pluginInstance = this.pluginInstances[pluginId];
    if (!pluginInstance) {
      throw new Error(`Plugin id not found ${pluginId}`);
    }
    return pluginInstance.api;
  }

  sourceToggled(id: string) {
    this.hiddenSources.update((setInst) => {
      if (setInst.has(id)) {
        setInst.delete(id);
        this.calendarComponent()
          ?.getApi()
          .addEventSource(this.registeredEventSourceFactories[id].generateEventSource());
      } else {
        setInst.add(id);
        this.calendarComponent()?.getApi().getEventSourceById(id)?.remove();
      }
      return setInst;
    });
    this.persistCalendarSettings();
  }

  private persistCalendarSettings() {
    if (this.config().id) {
      localStorage.setItem(
        `calendar-settings-${this.config().id}`,
        JSON.stringify({
          hiddenSources: Array.from(this.hiddenSources()),
          viewMode: this.viewMode(),
        } as CalendarPersistedSettings),
      );
    }
  }
}
