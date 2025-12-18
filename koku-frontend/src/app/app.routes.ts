import {Routes} from '@angular/router';
import {FORMULAR_CONTENT_SETUP} from './formular-binding/registry';
import {ListComponent} from './list/list.component';
import {LIST_CONTENT_SETUP} from './list-binding/registry';
import {UnsavedChangesPreventionGuard} from './navi/UnsavedChangesPreventionGuard';
import {CalendarComponent} from './calendar/calendar.component';
import {CALENDAR_CONTENT_SETUP} from './calendar-binding/registry';
import {CHART_CONTENT_SETUP} from './chart-binding/registry';
import {DASHBOARD_CONTENT_SETUP} from './dashboard-binding/registry';


export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./page-skeleton/page-skeleton.component').then(m => m.PageSkeletonComponent),
    children: [
      {
        path: 'calendar',
        title: 'Kalender',
        data: {
          naviIcon: 'CALENDAR',
        },
        children: [{
          path: '**',
          component: CalendarComponent,
          data: {
            config: {
              calendarActions: [{
                '@type': 'open-routed-content',
                route: 'new/customer-appointment',
                icon: 'PLUS',
                title: 'Neues Ereignis hinzufügen',
                id: 'create-new-appointment'
              }, {
                '@type': 'select-user',
                title: 'Bedienung auswählen',
                id: 'select-user',
              }],
              globalEventListeners: [
                {
                  '@type': 'replace-via-payload',
                  eventName: 'customer-appointment-created',
                  sourceId: 'customer-appointments',
                } as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
                {
                  '@type': 'replace-via-payload',
                  eventName: 'customer-appointment-updated',
                  sourceId: 'customer-appointments',
                } as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
                {
                  '@type': 'replace-via-payload',
                  eventName: 'user-appointment-created',
                  sourceId: 'user-appointments',
                } as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
                {
                  '@type': 'replace-via-payload',
                  eventName: 'user-appointment-updated',
                  sourceId: 'user-appointments',
                } as KokuDto.CalendarReplaceItemViaPayloadGlobalEventListenerDto,
              ] as KokuDto.AbstractCalendarGlobalEventListenerDto[],
              calendarClickAction: {
                '@type': 'open-routed-content',
                route: 'new/customer-appointment',
              } as KokuDto.CalendarOpenRoutedContentClickActionDto,
              routedContents: [
                {
                  '@type': 'routed-inline-content',
                  route: 'customers/:customerId',
                  itemId: ':customerId',
                  inlineContent: {
                    '@type': 'header',
                    sourceUrl: 'services/customers/customers/:customerId/summary',
                    titlePath: 'fullName',
                    globalEventListeners: [{
                      '@type': 'event-payload',
                      eventName: 'customer-updated',
                      idPath: 'id',
                      titleValuePath: 'fullName',
                    } as KokuDto.CalendarEventPayloadHeaderInlineContentGlobalEventListenersDto],
                    content: {
                      '@type': 'dock',
                      content: [{
                        id: 'information',
                        route: 'information',
                        icon: 'INFORMATION_CIRCLE',
                        title: 'Bearbeiten',
                        content: {
                          '@type': 'formular',
                          formularUrl: 'services/customers/customers/form',
                          sourceUrl: 'services/customers/customers/:customerId',
                          submitMethod: 'PUT',
                          maxWidthInPx: 800,
                          onSaveEvents: [{
                            '@type': 'propagate-global-event',
                            eventName: 'customer-updated',
                          } as KokuDto.CalendarFormularInlineContentAfterSavePropagateGlobalEventDto],
                        } as KokuDto.CalendarFormularInlineContentDto
                      } as KokuDto.CalendarInlineDockContentItemDto, {
                        id: 'appointments',
                        route: 'appointments',
                        icon: 'CALENDAR',
                        title: 'Termine',
                        content: {
                          '@type': 'list',
                          listUrl: 'services/customers/customers/appointments/list',
                          sourceUrl: 'services/customers/customers/:customerId/appointments/query',
                          title: 'Termine',
                        } as KokuDto.CalendarListInlineContentDto
                      } as KokuDto.CalendarInlineDockContentItemDto]
                    }
                  } as KokuDto.CalendarHeaderInlineContentDto
                } as KokuDto.CalendarRoutedContentDto,
                {
                  '@type': 'routed-inline-content',
                  route: 'users/:userId',
                  itemId: ':userId',
                  inlineContent: {
                    '@type': 'header',
                    sourceUrl: 'services/users/users/:userId/summary',
                    titlePath: 'summary',
                    content: {
                      '@type': 'list',
                      listUrl: 'services/users/users/appointments/list',
                      sourceUrl: 'services/users/users/:userId/appointments/query',
                      title: 'Termine',
                    } as KokuDto.CalendarListInlineContentDto
                  } as KokuDto.CalendarHeaderInlineContentDto
                } as KokuDto.CalendarRoutedContentDto,
                {
                  '@type': 'routed-inline-content',
                  route: 'new',
                  globalEventListeners: [
                    {
                      '@type': 'close',
                      eventName: 'customer-appointment-created'
                    },
                    {
                      '@type': 'close',
                      eventName: 'user-appointment-created'
                    }
                  ],
                  inlineContent: {
                    '@type': 'header',
                    title: 'Neuer Termin',
                    content: {
                      '@type': 'dock',
                      content: [
                        {
                          id: 'customer-appointment',
                          route: 'customer-appointment',
                          icon: 'INFORMATION_CIRCLE',
                          title: 'Kundentermin',
                          content: {
                            '@type': 'formular',
                            formularUrl: 'services/customers/customers/appointments/form',
                            submitUrl: 'services/customers/customers/appointments',
                            submitMethod: 'POST',
                            maxWidthInPx: 800,
                            onSaveEvents: [{
                              '@type': 'propagate-global-event',
                              eventName: 'customer-appointment-created',
                            } as KokuDto.CalendarFormularInlineContentAfterSavePropagateGlobalEventDto
                            ],
                            sourceOverrides: [
                              {
                                sourcePath: 'date',
                                value: 'SELECTION_START_DATE'
                              } as KokuDto.CalendarFormularSourceOverrideDto,
                              {
                                sourcePath: 'time',
                                value: 'SELECTION_START_TIME'
                              } as KokuDto.CalendarFormularSourceOverrideDto,
                            ]
                          } as KokuDto.CalendarFormularInlineContentDto
                        } as KokuDto.CalendarInlineDockContentItemDto,
                        {
                          id: 'user-appointment',
                          route: 'user-appointment',
                          icon: 'INFORMATION_CIRCLE',
                          title: 'Privater Termin',
                          content: {
                            '@type': 'formular',
                            formularUrl: 'services/users/users/appointments/form',
                            submitUrl: 'services/users/users/appointments',
                            submitMethod: 'POST',
                            maxWidthInPx: 800,
                            onSaveEvents: [{
                              '@type': 'propagate-global-event',
                              eventName: 'user-appointment-created',
                            } as KokuDto.CalendarFormularInlineContentAfterSavePropagateGlobalEventDto
                            ],
                            sourceOverrides: [
                              {
                                sourcePath: 'startDate',
                                value: 'SELECTION_START_DATE'
                              } as KokuDto.CalendarFormularSourceOverrideDto,
                              {
                                sourcePath: 'startTime',
                                value: 'SELECTION_START_TIME'
                              } as KokuDto.CalendarFormularSourceOverrideDto,
                              {
                                sourcePath: 'endDate',
                                value: 'SELECTION_END_DATE'
                              } as KokuDto.CalendarFormularSourceOverrideDto,
                              {
                                sourcePath: 'endTime',
                                value: 'SELECTION_END_TIME',
                                offsetValue: 1,
                                offsetUnit: 'HOUR'
                              } as KokuDto.CalendarFormularSourceOverrideDto
                            ]
                          } as KokuDto.CalendarFormularInlineContentDto
                        } as KokuDto.CalendarInlineDockContentItemDto
                      ]
                    } as KokuDto.CalendarDockInlineContentDto
                  } as KokuDto.CalendarHeaderInlineContentDto
                } as KokuDto.CalendarRoutedContentDto
              ],
              listSources: [
                {
                  '@type': 'list',
                  id: 'customer-birthday',
                  name: 'Geburtstage',
                  sourceUrl: '/services/customers/customers/query',
                  idPath: 'id',
                  startDateFieldSelectionPath: 'birthday',
                  endDateFieldSelectionPath: 'birthday',
                  searchOperatorHint: 'YEARLY_RECURRING',
                  displayTextFieldSelectionPath: 'fullNameWithOnFirstNameBasis',
                  additionalFieldSelectionPaths: ['id'],
                  editable: false,
                  sourceItemText: 'Geburtstag',
                  sourceItemColor: 'YELLOW',
                  deletedFieldSelectionPath: 'deleted',
                  clickAction: {
                    '@type': 'open-routed-content',
                    route: 'customers/:customerId/information',
                    params: [{
                      '@type': 'item-value',
                      param: ':customerId',
                      valuePath: 'id'
                    } as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto]
                  } as KokuDto.CalendarOpenRoutedContentItemClickAction
                } as KokuDto.CalendarListSourceConfigDto,
                {
                  '@type': 'list',
                  id: 'customer-appointments',
                  name: 'Kundentermine',
                  sourceUrl: '/services/customers/customers/appointments/query',
                  idPath: 'id',
                  startDateFieldSelectionPath: 'date',
                  endDateFieldSelectionPath: 'approximatelyEndDate',
                  startTimeFieldSelectionPath: 'time',
                  endTimeFieldSelectionPath: 'approximatelyEndTime',
                  userIdFieldSelectionPath: 'userId',
                  displayTextFieldSelectionPath: 'customerName',
                  additionalFieldSelectionPaths: ['customerId', 'id', 'version'],
                  sourceItemText: 'Kundentermin',
                  sourceItemColor: 'BLUE',
                  deletedFieldSelectionPath: 'deleted',
                  clickAction: {
                    '@type': 'open-routed-content',
                    route: 'customers/:customerId/appointments/:appointmentId',
                    params: [{
                      '@type': 'item-value',
                      param: ':customerId',
                      valuePath: 'customerId'
                    } as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto, {
                      '@type': 'item-value',
                      param: ':appointmentId',
                      valuePath: 'id'
                    } as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto]
                  } as KokuDto.CalendarOpenRoutedContentItemClickAction,
                  dropAction: {
                    '@type': 'call-http',
                    url: '/services/customers/customers/appointments/:appointmentId',
                    method: 'PUT',
                    startDatePath: 'date',
                    startTimePath: 'time',
                    urlParams: [
                      {
                        '@type': 'item-value',
                        param: ':appointmentId',
                        valuePath: 'id'
                      } as KokuDto.CalendarCallHttpItemActionItemValueParamDto
                    ],
                    valueMapping: {
                      'version': 'version'
                    },
                    successEvents: [{
                      '@type': 'propagate-global-event',
                      eventName: 'customer-appointment-updated',
                    } as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto],
                  } as KokuDto.CalendarCallHttpItemDropAction
                } as KokuDto.CalendarListSourceConfigDto,
                {
                  '@type': 'list',
                  id: 'user-appointments',
                  name: 'Private Termine',
                  sourceUrl: '/services/users/users/appointments/query',
                  idPath: 'id',
                  startDateFieldSelectionPath: 'startDate',
                  endDateFieldSelectionPath: 'endDate',
                  startTimeFieldSelectionPath: 'startTime',
                  endTimeFieldSelectionPath: 'endTime',
                  userIdFieldSelectionPath: 'userId',
                  displayTextFieldSelectionPath: 'description',
                  additionalFieldSelectionPaths: ['userId', 'id', 'version'],
                  sourceItemText: 'Privater Termin',
                  sourceItemColor: 'GREEN',
                  deletedFieldSelectionPath: 'deleted',
                  clickAction: {
                    '@type': 'open-routed-content',
                    route: 'users/:userId/appointments/:appointmentId',
                    params: [
                      {
                        '@type': 'item-value',
                        param: ':appointmentId',
                        valuePath: 'id'
                      } as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto,
                      {
                        '@type': 'item-value',
                        param: ':userId',
                        valuePath: 'userId'
                      } as KokuDto.ItemValueCalendarOpenRoutedContentItemParamDto,
                    ]
                  } as KokuDto.CalendarOpenRoutedContentItemClickAction,
                  dropAction: {
                    '@type': 'call-http',
                    url: '/services/users/users/appointments/:appointmentId',
                    method: 'PUT',
                    startDatePath: 'startDate',
                    startTimePath: 'startTime',
                    endDatePath: 'endDate',
                    endTimePath: 'endTime',
                    urlParams: [
                      {
                        '@type': 'item-value',
                        param: ':appointmentId',
                        valuePath: 'id'
                      } as KokuDto.CalendarCallHttpItemActionItemValueParamDto,
                    ],
                    valueMapping: {
                      'version': 'version'
                    },
                    successEvents: [{
                      '@type': 'propagate-global-event',
                      eventName: 'user-appointment-updated',
                    } as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto],
                  } as KokuDto.CalendarCallHttpItemDropAction,
                  resizeAction: {
                    '@type': 'call-http',
                    url: '/services/users/users/appointments/:appointmentId',
                    method: 'PUT',
                    endDatePath: 'endDate',
                    endTimePath: 'endTime',
                    urlParams: [
                      {
                        '@type': 'item-value',
                        param: ':appointmentId',
                        valuePath: 'id'
                      } as KokuDto.CalendarCallHttpItemActionItemValueParamDto,
                    ],
                    valueMapping: {
                      'version': 'version'
                    },
                    successEvents: [{
                      '@type': 'propagate-global-event',
                      eventName: 'user-appointment-updated',
                    } as KokuDto.CalendarCallHttpItemActionPropagateGlobalEventSuccessEventDto],
                  } as KokuDto.CalendarCallHttpItemResizeAction
                } as KokuDto.CalendarListSourceConfigDto,
                {
                  '@type': 'holiday',
                  id: 'holidays',
                  name: 'Feiertage',
                  sourceItemColor: 'RED',
                } as KokuDto.CalendarHolidaySourceConfigDto,
              ] as KokuDto.CalendarListSourceConfigDto[]
            },
            contentSetup: CALENDAR_CONTENT_SETUP,
            parentRoutePath: '/calendar',
          },
          canDeactivate: [UnsavedChangesPreventionGuard]
        }],
      },
      {
        path: 'files',
        title: 'Dateiübersicht',
        data: {
          naviIcon: 'DOCUMENT',
        },
        children: [{
          path: '**',
          component: ListComponent,
          title: 'Dateiübersicht',
          data: {
            listUrl: '/services/files/files/list',
            sourceUrl: '/services/files/files/query',
            contentSetup: LIST_CONTENT_SETUP,
            parentRoutePath: '/files'
          },
          canDeactivate: [UnsavedChangesPreventionGuard]
        }],
      },
      {
        path: 'manage',
        title: 'Stammdaten',
        data: {
          naviIcon: 'SQUARES_2X2'
        },
        children: [
          {
            loadComponent: () => import('./navi-tabbar/navi-tabbar.component').then(m => m.NaviTabbarComponent),
            path: '',
            children: [
              {
                path: 'customers',
                title: 'Kunden',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Kunden',
                  data: {
                    listUrl: '/services/customers/customers/list',
                    sourceUrl: '/services/customers/customers/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/manage/customers'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }],
              },
              {
                path: 'products',
                title: 'Produkte',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Produkte',
                  data: {
                    listUrl: '/services/products/products/list',
                    sourceUrl: '/services/products/products/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/manage/products'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }],
              },
              {
                path: 'productmanufacturers',
                title: 'Produkthersteller',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Produkthersteller',
                  data: {
                    listUrl: '/services/products/productmanufacturers/list',
                    sourceUrl: '/services/products/productmanufacturers/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/manage/productmanufacturers'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }],
              },
              {
                path: 'activitysteps',
                title: 'Behandlungsschritte',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Behandlungsschritte',
                  data: {
                    listUrl: '/services/activities/activitysteps/list',
                    sourceUrl: '/services/activities/activitysteps/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/manage/activitysteps'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }],
              },
              {
                path: 'activities',
                title: 'Tätigkeiten',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Tätigkeiten',
                  data: {
                    listUrl: '/services/activities/activities/list',
                    sourceUrl: '/services/activities/activities/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/manage/activities'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }],
              },
              {
                path: 'promotions',
                title: 'Aktionen',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Aktionen',
                  data: {
                    listUrl: '/services/promotions/promotions/list',
                    sourceUrl: '/services/promotions/promotions/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/manage/promotions'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }],
              },
              {
                path: '',
                redirectTo: 'customers',
                pathMatch: 'full'
              },
            ]
          }
        ]
      },
      {
        path: 'administration',
        title: 'Administration',
        children: [
          {
            loadComponent: () => import('./navi-tabbar/navi-tabbar.component').then(m => m.NaviTabbarComponent),
            path: '',
            children: [
              {
                path: 'users',
                title: 'Nutzer',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Nutzer',
                  data: {
                    listUrl: '/services/users/users/list',
                    sourceUrl: '/services/users/users/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/administration/users'
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }]
              },
              {
                path: 'documents',
                title: 'Dokumente',
                children: [{
                  path: '**',
                  component: ListComponent,
                  title: 'Dokumente',
                  data: {
                    listUrl: '/services/documents/documents/list',
                    sourceUrl: '/services/documents/documents/query',
                    contentSetup: LIST_CONTENT_SETUP,
                    parentRoutePath: '/administration/documents',
                  },
                  canDeactivate: [UnsavedChangesPreventionGuard]
                }]
              },
              {
                path: '',
                redirectTo: 'users',
                pathMatch: 'full'
              },
            ]
          },
        ],
        data: {
          naviIcon: 'ADJUSTMENTS_HORIZONTAL'
        }
      },
      {
        path: 'statistics',
        title: 'Statistik',
        data: {
          naviIcon: 'CHART_BAR'
        },
        children: [
          {
            loadComponent: () => import('./navi-tabbar/navi-tabbar.component').then(m => m.NaviTabbarComponent),
            path: '',
            children: [
              {
                path: 'dashboard',
                loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
                title: 'Übersicht',
                data: {
                  dashboardUrl: '/services/customers/customers/dashboard',
                  contentSetup: DASHBOARD_CONTENT_SETUP
                }
              },
              {
                loadComponent: () => import('./chart/chart.component').then(m => m.ChartComponent),
                path: 'revenues',
                title: 'Umsätze',
                data: {
                  chartUrl: '/services/customers/appointments/statistics',
                  contentSetup: CHART_CONTENT_SETUP,
                }
              },
              {
                loadComponent: () => import('./chart/chart.component').then(m => m.ChartComponent),
                path: 'products',
                title: 'Produkte',
                data: {
                  chartUrl: '/services/customers/products/statistics',
                  contentSetup: CHART_CONTENT_SETUP,
                }
              },
              {
                loadComponent: () => import('./chart/chart.component').then(m => m.ChartComponent),
                path: 'activities',
                title: 'Tätigkeiten',
                data: {
                  chartUrl: '/services/customers/activities/statistics',
                  contentSetup: CHART_CONTENT_SETUP,
                }
              },
              {
                loadComponent: () => import('./chart/chart.component').then(m => m.ChartComponent),
                path: 'customers',
                title: 'Kunden',
                data: {
                  chartUrl: '/services/customers/customers/statistics',
                  contentSetup: CHART_CONTENT_SETUP,
                }
              },
              {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
              },
            ]
          }
        ]
      },
      {
        path: 'welcome',
        loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent),
        title: 'Willkommen',
        data: {
          dashboardUrl: '/services/users/users/welcome',
          contentSetup: DASHBOARD_CONTENT_SETUP,
          hideInNav: true,
        }
      },
      {
        loadComponent: () => import('./formular/formular.component').then(m => m.FormularComponent),
        path: 'myprofile',
        title: 'Mein Profil',
        data: {
          naviAlign: 'bottom',
          formularUrl: '/services/users/users/form',
          sourceUrl: '/services/users/users/@self',
          submitMethod: 'PUT',
          contentSetup: FORMULAR_CONTENT_SETUP,
          maxWidth: '900px',
          naviDivider: 'after',
          naviIcon: 'USER'
        },
        canDeactivate: [UnsavedChangesPreventionGuard]
      },
      {
        path: 'logout',
        loadComponent: () => import('./logout/logout.component').then(m => m.LogoutComponent),
        title: 'Logout',
        data: {
          naviAlign: 'bottom',
          naviIcon: 'ARROW_RIGHT_END_ON_RECTANGLE'
        },
      },
      {
        path: '',
        redirectTo: 'welcome',
        pathMatch: 'full'
      },
    ]
  },
];
