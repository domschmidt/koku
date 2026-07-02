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

export interface CalendarPluginEventSourceFactory {
  generateEventSource(): EventSourceInput;

  generateEventItem(item: Record<string, any>): EventInput;

  lookupEvent(eventPayload: any): any;
}

interface CalendarPersistedSettings {
  hiddenSources?: string[];
  viewMode?: 'WEEK' | 'DAY' | 'MONTH';
}

@Component({
  selector: 'calendar',
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
  styleUrl: './calendar.component.css',
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
  viewMode = signal<'WEEK' | 'DAY' | 'MONTH'>('MONTH');
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
              const classnameSnapshot = [...args.event.classNames];
              if (loading) {
                classnameSnapshot.push('calendar-item--loading');
                args.event.setProp('classNames', classnameSnapshot);
              } else {
                classnameSnapshot.splice(classnameSnapshot.indexOf('calendar-item--loading'), 1);
                args.event.setProp('classNames', classnameSnapshot);
              }
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
              const classnameSnapshot = [...args.event.classNames];
              if (loading) {
                classnameSnapshot.push('calendar-item--loading');
                args.event.setProp('classNames', classnameSnapshot);
              } else {
                if (classnameSnapshot.indexOf('calendar-item--loading') >= 0) {
                  classnameSnapshot.splice(classnameSnapshot.indexOf('calendar-item--loading'), 1);
                  args.event.setProp('classNames', classnameSnapshot);
                }
              }
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
    toObservable(this.config).subscribe((config) => {
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

      const eventSources: EventSourceInput[] = [];
      for (const currentSource of config.listSources || []) {
        for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
          const eventSourceFactory: CalendarPluginEventSourceFactory | void =
            currentPluginInstance.instance.provideEventSourceFactory?.(currentSource);
          if (eventSourceFactory && currentSource.id) {
            this.registeredEventSourceFactories[currentSource.id] = eventSourceFactory;
            if (!this.hiddenSources().has(currentSource.id)) {
              eventSources.push(eventSourceFactory.generateEventSource());
            }
          }
        }
      }
      this.fullCalendarOptions.set({
        ...this.fullCalendarOptions(),
        eventSources: eventSources,
        initialView: this.getView(this.viewMode()),
        dateClick: (dateClickInfo) => {
          for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
            currentPluginInstance.instance.onDateSelect?.({
              selectionStart: dateClickInfo.date,
              selectionEnd: dateClickInfo.date,
              allDay: dateClickInfo.allDay,
            });
          }
        },
        select: (dateSelectInfo) => {
          for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
            currentPluginInstance.instance.onDateSelect?.({
              selectionStart: dateSelectInfo.start,
              selectionEnd: dateSelectInfo.end,
              allDay: dateSelectInfo.allDay,
            });
          }
        },
      });

      for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
        currentPluginInstance.instance.afterConfigLoaded?.(config);
      }

      const calendarActions: Record<string, KokuDto.AbstractCalendarActionDto> = {};
      for (const currentCalendarAction of config.calendarActions || []) {
        const currentCalendarActionId = currentCalendarAction.id;
        if (currentCalendarActionId) {
          if (calendarActions[currentCalendarActionId]) {
            throw new Error(`Duplicated calendar action id ${currentCalendarActionId}`);
          }
          calendarActions[currentCalendarActionId] = currentCalendarAction;
        } else {
          throw new Error('Missing calendar action id');
        }
      }
      this.calendarActions.set(calendarActions);
      for (const currentCalendarAction of config.calendarActions || []) {
        const currentCalendarActionId = currentCalendarAction.id;
        if (currentCalendarActionId) {
          for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
            currentPluginInstance.instance.initCalendarAction?.(
              currentCalendarAction,
              (updatedAction: KokuDto.AbstractCalendarActionDto) => {
                this.calendarActions.set({
                  ...this.calendarActions(),
                  [currentCalendarActionId]: updatedAction,
                });
              },
            );
          }
        }
      }

      if (this.routerUrlSubscription) {
        this.routerUrlSubscription.unsubscribe();
      }

      const afterNavigationUrlChange = () => {
        const segments = this.router.url
          .split('/')
          .filter((value) => value !== '')
          .slice(
            this.parentRoutePath()
              .split('/')
              .filter((value) => value !== '').length,
          );
        let routedContentFound = false;
        for (const currentRoutedContent of config.routedContents || []) {
          if (currentRoutedContent.route) {
            let failedLookup = false;
            let segmentIdx = 0;
            const segmentMapping: Record<string, string> = {};
            for (const currentRoutePathToMatch of currentRoutedContent.route.split('/')) {
              const currentSegment = segments[segmentIdx++];
              if (!currentSegment) {
                failedLookup = true;
                break;
              }
              const currentSegmentPath = currentSegment;
              if (currentRoutePathToMatch.indexOf(':') === 0) {
                segmentMapping[currentRoutePathToMatch] = currentSegmentPath;
              } else if (currentRoutePathToMatch !== currentSegmentPath) {
                failedLookup = true;
                break;
              }
            }
            if (!failedLookup) {
              routedContentFound = true;
              if (currentRoutedContent['@type'] === 'routed-inline-content') {
                const castedRouteContent = currentRoutedContent as KokuDto.CalendarRoutedContentDto;
                if (castedRouteContent.inlineContent) {
                  let itemId = castedRouteContent.itemId;
                  if (itemId) {
                    for (const [segment, value] of Object.entries(segmentMapping || {})) {
                      itemId = itemId.replace(segment, value);
                    }
                  }

                  if (
                    (itemId !== undefined && this.activeContent()?.id !== itemId) ||
                    this.activeContent()?.content !== castedRouteContent.inlineContent ||
                    !deepEqual(this.activeContent()?.urlSegments, segmentMapping)
                  ) {
                    this.openInlineContent({
                      content: castedRouteContent.inlineContent,
                      id: itemId || null,
                      parentRoutePath: [
                        ...(this.parentRoutePath() + '/' + castedRouteContent.route).split('/').map(
                          (value) =>
                            ({
                              ...segmentMapping,
                              ...this.urlSegments(),
                            })[value] || value,
                        ),
                      ]
                        .filter((value) => value !== '')
                        .join('/'),
                      urlSegments: segmentMapping,
                    });

                    for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
                      currentPluginInstance.instance.onRoutedInlineContentOpened?.(castedRouteContent);
                    }
                  }
                }
                break;
              }
            }
          }
        }
        if (!routedContentFound) {
          this.closeInlineContent(true).subscribe();
        }
      };

      this.routerUrlSubscription = this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((evnt) => {
        if (evnt instanceof NavigationEnd) {
          afterNavigationUrlChange();
        }
      });
      afterNavigationUrlChange();
    });

    const pluginInstances: Record<string, { instance: CalendarPlugin; api: any }> = {};
    let pluginsConfig = this.calendarPluginsConfig;
    if (pluginsConfig) {
      if (!Array.isArray(pluginsConfig)) {
        pluginsConfig = [pluginsConfig];
      }
      for (const currentPlugin of pluginsConfig || []) {
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
    }
    this.pluginInstances = pluginInstances;
  }

  dateChanged(value: string | null) {
    if (!value) {
      return;
    }
    const parsedDate = this.viewMode() === 'WEEK' ? parseIsoWeekValue(value) : dayjs(value);
    this.calendarComponent()?.getApi().gotoDate(parsedDate.toDate());
  }

  formatDate(date: Date, viewMode: 'WEEK' | 'DAY' | 'MONTH') {
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

  changeViewMode(mode: 'WEEK' | 'DAY' | 'MONTH') {
    this.calendarComponent()?.getApi().changeView(this.getView(mode));
    this.persistCalendarSettings();
  }

  getView(mode: 'WEEK' | 'DAY' | 'MONTH') {
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
    if (activeContentSnapshot && activeContentSnapshot.modalRef) {
      this.modalService.update(activeContentSnapshot.modalRef, {
        dynamicContent: item.content,
        urlSegments: item.urlSegments,
        dynamicContentSetup: this.contentSetup().modalContentRegistry,
        fullscreen: true,
        parentRoutePath: item.parentRoutePath,
        clickOutside: () => {
          this.closeInlineContent().subscribe((success) => {
            if (success) {
              activeContentSnapshot.modalRef.close();
            }
          });
        },
        onCloseRequested: () => {
          this.closeInlineContent().subscribe((success) => {
            if (success) {
              activeContentSnapshot.modalRef.close();
            }
          });
        },
      });
    } else {
      const newModal = this.modalService.add({
        dynamicContent: item.content,
        urlSegments: item.urlSegments,
        dynamicContentSetup: this.contentSetup().modalContentRegistry,
        fullscreen: true,
        parentRoutePath: item.parentRoutePath,
        clickOutside: () => {
          this.closeInlineContent().subscribe((success) => {
            if (success) {
              newModal.close();
            }
          });
        },
        onCloseRequested: () => {
          this.closeInlineContent().subscribe((success) => {
            if (success) {
              newModal.close();
            }
          });
        },
      });
      this.activeContent.set({
        ...item,
        modalRef: newModal,
      });
    }
  }

  openRoutedContent(routes: string[], context?: CalendarContext) {
    this.router.navigate(
      [...this.parentRoutePath().split('/'), ...routes].filter((value) => value !== ''),
      {
        queryParams: context,
      },
    );
  }

  closeInlineContent(skipRouteChange = false) {
    return new Observable<boolean>((observer) => {
      if (!skipRouteChange) {
        this.router
          .navigate([
            ...this.parentRoutePath()
              .split('/')
              .map((value) => (this.urlSegments() || {})[value] || value),
          ])
          .then((success) => {
            if (success) {
              const activeContentSnapshot = this.activeContent();
              for (const currentPluginInstance of Object.values(this.pluginInstances || {})) {
                currentPluginInstance.instance.onRoutedInlineContentClose?.(activeContentSnapshot);
              }
              if (activeContentSnapshot?.modalRef) {
                this.modalService.close(activeContentSnapshot?.modalRef);
              }
              this.activeContent.set(null);
            }
            observer.next(success);
            observer.complete();
          });
      } else {
        const activeContentSnapshot = this.activeContent();
        if (activeContentSnapshot?.modalRef) {
          this.modalService.close(activeContentSnapshot?.modalRef);
        }
        this.activeContent.set(null);
        observer.next(true);
        observer.complete();
      }
    });
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
