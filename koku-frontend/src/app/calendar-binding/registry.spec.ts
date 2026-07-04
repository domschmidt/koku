import { signal } from '@angular/core';
import { CALENDAR_CONTENT_SETUP } from './registry';

describe('calendar content recipes', () => {
  it('maps selection query params to formular source overrides', () => {
    const factory = CALENDAR_CONTENT_SETUP.inlineContentRegistry['formular']!;
    const closed: string[] = [];
    const openedRoutes: string[][] = [];
    const recipe = factory({
      content: signal({
        '@type': 'formular',
        formularUrl: '/appointments/:appointmentId/form',
        sourceUrl: '/appointments/:appointmentId',
        maxWidthInPx: 640,
        contentOverrides: [
          {
            '@type': 'route-based-override',
            alias: 'appointmentId',
            routeParam: ':appointmentId',
            disabled: true,
          },
        ],
        sourceOverrides: [
          {
            sourcePath: 'startDate',
            value: 'SELECTION_START_DATE',
            offsetUnit: 'DAY',
            offsetValue: 1,
          },
          {
            sourcePath: 'startTime',
            value: 'SELECTION_START_TIME',
          },
        ],
      } as unknown as KokuDto.CalendarFormularInlineContentDto),
      loading: signal(false),
      contentSetup: signal(CALENDAR_CONTENT_SETUP),
      urlSegments: signal({ ':appointmentId': '99' }),
      buttonDockOutlet: signal(undefined),
      parentRoutePath: signal('/calendar'),
      queryParams: signal({
        selectionStartDate: '2026-07-03',
        selectionStartTime: '09:30',
      }),
      close: () => closed.push('closed'),
      openRoutedContent: (routes) => openedRoutes.push(routes),
    });

    expect(recipe.inputs?.()).toEqual(
      expect.objectContaining({
        formularUrl: '/appointments/99/form',
        sourceUrl: '/appointments/99',
        submitUrl: '/appointments/99',
        maxWidth: '640px',
        contentOverrides: [{ alias: 'appointmentId', disabled: true, value: '99' }],
        sourceOverrides: [
          { path: 'startDate', value: '2026-07-04' },
          { path: 'startTime', value: '09:30' },
        ],
      }),
    );

    recipe.outputs?.['closeRequested'](undefined);
    recipe.outputs?.['openRoutedContentRequested'](['99', 'details']);

    expect(closed).toEqual(['closed']);
    expect(openedRoutes).toEqual([['99', 'details']]);
  });

  it('opens routed calendar actions through the action context', () => {
    const factory = CALENDAR_CONTENT_SETUP.actionRegistry['open-routed-content']!;
    const openedRoutes: string[][] = [];
    const recipe = factory({
      action: signal({
        '@type': 'open-routed-content',
        route: 'appointments/:appointmentId',
        title: 'Open',
      } as KokuDto.CalendarOpenRoutedContentActionDto),
      contentSetup: signal(CALENDAR_CONTENT_SETUP),
      openRoutedContent: (routes) => openedRoutes.push(routes),
      getPluginApi: () => undefined,
    });

    expect(recipe.inputs?.()).toEqual(
      expect.objectContaining({
        title: 'Open',
        loading: false,
      }),
    );

    recipe.outputs?.['clicked'](undefined);

    expect(openedRoutes).toEqual([['appointments', ':appointmentId']]);
  });
});
