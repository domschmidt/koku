import { signal } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';
import { CALENDAR_CONTENT_SETUP } from './registry';

const baseContent = {
  formularUrl: '/forms/:id',
  sourceUrl: '/source/:id',
  submitUrl: '/submit/:id',
  listUrl: '/list/:id',
  maxWidthInPx: 600,
  content: [],
  title: 'Title',
  titlePath: 'name',
};

describe('calendar content registry contract', () => {
  it('renders every modal and inline content type', async () => {
    for (const [type, factory] of Object.entries(CALENDAR_CONTENT_SETUP.modalContentRegistry)) {
      const close = vi.fn();
      const recipe = factory!({
        instance: {} as any,
        content: signal({ '@type': type, ...baseContent } as any),
        modal: { uid: 1, urlSegments: { ':id': '42' }, parentRoutePath: '/calendar', close, update: vi.fn() },
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      recipe.outputs?.['closeRequested']?.(undefined);
      expect(close).toHaveBeenCalled();
    }
    for (const [type, factory] of Object.entries(CALENDAR_CONTENT_SETUP.inlineContentRegistry)) {
      const recipe = factory!({
        content: signal({ '@type': type, ...baseContent } as any),
        loading: signal(false),
        contentSetup: signal(CALENDAR_CONTENT_SETUP),
        urlSegments: signal({ ':id': '42' }),
        queryParams: signal({}),
        parentRoutePath: signal('/calendar'),
        buttonDockOutlet: signal(undefined),
        close: vi.fn(),
        openRoutedContent: vi.fn(),
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      for (const output of Object.values(recipe.outputs ?? {})) output(['details']);
    }
  });

  it('applies every supported selection offset and source value', () => {
    const values = [
      'SELECTION_START_DATE',
      'SELECTION_END_DATE',
      'SELECTION_START_TIME',
      'SELECTION_END_TIME',
      'SELECTION_START_DATETIME',
      'SELECTION_END_DATETIME',
    ];
    const units = ['SECOND', 'MINUTE', 'HOUR', 'DAY', 'WEEK', 'MONTH', 'YEAR'];
    const recipe = CALENDAR_CONTENT_SETUP.inlineContentRegistry['formular']!({
      content: signal({
        '@type': 'formular',
        ...baseContent,
        sourceOverrides: units.map((unit, index) => ({
          sourcePath: `offsets.${unit}`,
          value: values[index % values.length],
          offsetUnit: unit,
          offsetValue: 1,
        })),
      } as any),
      loading: signal(false),
      contentSetup: signal(CALENDAR_CONTENT_SETUP),
      urlSegments: signal({ ':id': '42' }),
      queryParams: signal({
        selectionStartDate: '2026-07-13',
        selectionEndDate: '2026-07-14',
        selectionStartTime: '09:00',
        selectionEndTime: '10:00',
        selectionStartDateTime: '2026-07-13T09:00',
        selectionEndDateTime: '2026-07-14T10:00',
      }),
      parentRoutePath: signal('/calendar'),
      buttonDockOutlet: signal(undefined),
      close: vi.fn(),
      openRoutedContent: vi.fn(),
    });
    expect(recipe.inputs?.()['sourceOverrides']).toHaveLength(7);
  });

  it('renders and executes both calendar action types', async () => {
    const selectUser = vi.fn();
    const opened = vi.fn();
    const actions: Record<string, any> = {
      'open-routed-content': { '@type': 'open-routed-content', route: 'appointments/new', title: 'New' },
      'select-user': { '@type': 'select-user', title: 'User', loading: true },
    };
    for (const [type, factory] of Object.entries(CALENDAR_CONTENT_SETUP.actionRegistry)) {
      const recipe = factory!({
        action: signal(actions[type]),
        contentSetup: signal(CALENDAR_CONTENT_SETUP),
        openRoutedContent: opened,
        getPluginApi: <T>() => ({ selectUser }) as T,
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      expect(recipe.inputs?.()).toEqual(expect.objectContaining({ title: actions[type].title }));
      recipe.outputs?.['clicked']?.(undefined);
    }
    expect(opened).toHaveBeenCalledWith(['appointments', 'new']);
    expect(selectUser).toHaveBeenCalled();
  });

  it('handles incomplete, invalid and unknown selection overrides', () => {
    const createRecipe = (sourceOverrides: any[], queryParams: Record<string, any> = {}) =>
      CALENDAR_CONTENT_SETUP.inlineContentRegistry['formular']!({
        content: signal({ '@type': 'formular', ...baseContent, sourceOverrides } as any),
        loading: signal(false),
        contentSetup: signal(CALENDAR_CONTENT_SETUP),
        urlSegments: signal(null),
        queryParams: signal(queryParams),
        parentRoutePath: signal(''),
        buttonDockOutlet: signal(undefined),
        close: vi.fn(),
        openRoutedContent: vi.fn(),
      });

    expect(
      createRecipe(
        [{}, { sourcePath: 'unknown', value: 'UNKNOWN' }, { sourcePath: 'invalid', value: 'SELECTION_START_DATE' }],
        { selectionStartDate: 'not-a-date' },
      ).inputs?.()['sourceOverrides'],
    ).toEqual([
      { path: 'unknown', value: undefined },
      { path: 'invalid', value: expect.any(String) },
    ]);
    expect(() =>
      createRecipe([{ sourcePath: 'date', value: 'SELECTION_START_DATE', offsetUnit: 'UNKNOWN', offsetValue: 1 }], {
        selectionStartDate: '2026-01-01',
      }).inputs?.(),
    ).toThrow('Unknown offset type');
  });

  it('uses custom modal close handlers', () => {
    const onCloseRequested = vi.fn();
    const recipe = CALENDAR_CONTENT_SETUP.modalContentRegistry['dock']!({
      instance: {} as any,
      content: signal({ '@type': 'dock', content: [] } as any),
      modal: { uid: 1, close: vi.fn(), update: vi.fn(), onCloseRequested },
    });
    recipe.outputs?.['closeRequested']?.(undefined);
    expect(onCloseRequested).toHaveBeenCalled();
  });
});
