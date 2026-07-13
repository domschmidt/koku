import { signal } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';
import { BUSINESS_RULES_CONTENT_SETUP } from './registry';

const content = {
  formularUrl: '/forms/:id',
  sourceUrl: '/source/:id',
  submitUrl: '/submit/:id',
  maxWidthInPx: 500,
  content: [],
  title: 'Title',
  titlePath: 'name',
  onSaveEvents: [{ '@type': 'open-routed-inline-formular', route: 'details/:id' }],
};

describe('business-rule content registry contract', () => {
  it('renders every inline recipe and forwards routing outputs', async () => {
    const close = vi.fn();
    const open = vi.fn();
    for (const [type, factory] of Object.entries(BUSINESS_RULES_CONTENT_SETUP.contentRegistry)) {
      const recipe = factory!({
        content: signal({ '@type': type, ...content } as any),
        loading: signal(false),
        contentSetup: signal(BUSINESS_RULES_CONTENT_SETUP),
        urlSegments: signal({ ':id': '42' }),
        parentRoutePath: signal('/rules'),
        buttonDockOutlet: signal(undefined),
        close,
        openRoutedContent: open,
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      recipe.outputs?.['closeRequested']?.(undefined);
      recipe.outputs?.['openRoutedContentRequested']?.(['details']);
      recipe.outputs?.['saved']?.({ id: 42 });
    }
    expect(close).toHaveBeenCalled();
    expect(open).toHaveBeenCalled();
  });

  it('renders every modal recipe and honors custom close handlers', async () => {
    for (const [type, factory] of Object.entries(BUSINESS_RULES_CONTENT_SETUP.modalContentRegistry)) {
      const close = vi.fn();
      const onCloseRequested = type === 'dock' ? vi.fn() : undefined;
      const recipe = factory!({
        instance: {} as any,
        content: signal({ '@type': type, ...content } as any),
        modal: {
          uid: 1,
          urlSegments: { ':id': '42' },
          parentRoutePath: '/rules',
          close,
          update: vi.fn(),
          onCloseRequested,
        },
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      recipe.inputs?.();
      recipe.outputs?.['saved']?.({});
      recipe.outputs?.['closeRequested']?.(undefined);
      if (recipe.outputs?.['closeRequested']) expect(onCloseRequested ?? close).toHaveBeenCalled();
    }
  });
});
