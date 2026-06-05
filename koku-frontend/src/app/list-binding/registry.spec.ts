import { signal } from '@angular/core';
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
});
