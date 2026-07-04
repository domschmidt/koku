import { signal } from '@angular/core';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { LIST_CONTENT_SETUP } from './registry';

describe('list content recipes', () => {
  it('does not pass container inputs to the barcode capture component', () => {
    const factory = LIST_CONTENT_SETUP.inlineContentRegistry['barcode']!;
    const recipe = factory({
      content: signal({ '@type': 'barcode' } as KokuDto.ListViewBarcodeContentDto),
      loading: signal(false),
      urlSegments: signal(null),
      parentRoutePath: signal(''),
      buttonDockOutlet: signal(undefined),
      context: signal(undefined),
      close: () => undefined,
      openRoutedContent: () => undefined,
    });

    expect(recipe.inputs).toBeUndefined();
  });

  it('resolves formular routes, width and route-based overrides from inline context', () => {
    const factory = LIST_CONTENT_SETUP.inlineContentRegistry['formular']!;
    const closed: string[] = [];
    const openedRoutes: string[][] = [];
    const recipe = factory({
      content: signal({
        '@type': 'formular',
        formularUrl: '/customers/:customerId/form',
        sourceUrl: '/customers/:customerId',
        maxWidthInPx: 0,
        contentOverrides: [
          {
            '@type': 'route-based-override',
            alias: 'customerId',
            routeParam: ':customerId',
            disabled: true,
          },
        ],
      } as unknown as KokuDto.ListViewFormularContentDto),
      loading: signal(false),
      urlSegments: signal({ ':customerId': '42' }),
      parentRoutePath: signal('/customers'),
      buttonDockOutlet: signal(undefined),
      context: signal({ source: 'list' }),
      close: () => closed.push('closed'),
      openRoutedContent: (routes) => openedRoutes.push(routes),
    });

    expect(recipe.inputs?.()).toEqual(
      expect.objectContaining({
        formularUrl: '/customers/42/form',
        sourceUrl: '/customers/42',
        submitUrl: '/customers/42',
        maxWidth: '0px',
        contentOverrides: [{ alias: 'customerId', disabled: true, value: '42' }],
        context: { source: 'list' },
      }),
    );

    recipe.outputs?.['closeRequested'](undefined);
    recipe.outputs?.['openRoutedContentRequested'](['42', 'appointments']);

    expect(closed).toEqual(['closed']);
    expect(openedRoutes).toEqual([['42', 'appointments']]);
  });

  it('propagates document form submit events through the global event bus', () => {
    const factory = LIST_CONTENT_SETUP.inlineContentRegistry['document-form']!;
    const payloads: any[] = [];
    GLOBAL_EVENT_BUS.addGlobalEventListener('list-registry-spec', 'document-created', (payload) =>
      payloads.push(payload),
    );
    const recipe = factory({
      content: signal({
        '@type': 'document-form',
        documentUrl: '/documents/:documentId',
        submitUrl: '/documents/:documentId/render',
        onSubmitEvents: [
          {
            '@type': 'propagate-global-event',
            eventName: 'document-created',
          },
        ],
      } as unknown as KokuDto.ListViewDocumentFormContentDto),
      loading: signal(false),
      urlSegments: signal({ ':documentId': 'abc' }),
      parentRoutePath: signal('/documents'),
      buttonDockOutlet: signal(undefined),
      context: signal(undefined),
      close: () => undefined,
      openRoutedContent: () => undefined,
    });

    expect(recipe.inputs?.()).toEqual(
      expect.objectContaining({
        documentUrl: '/documents/abc',
        submitUrl: '/documents/abc/render',
      }),
    );

    recipe.outputs?.['submitted']({ id: 'rendered-file' });

    expect(payloads).toEqual([{ id: 'rendered-file' }]);
    GLOBAL_EVENT_BUS.removeGlobalEventListener('list-registry-spec');
  });
});
