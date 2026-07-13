import { Component, input, computed } from '@angular/core';
import { ListContentSetup, ListItemSetup } from '../list.component';
import { get } from '../../utils/get';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';
@Component({
  selector: '[list-item-preview],list-item-preview',
  imports: [KokuDynamicHostDirective],
  templateUrl: './list-item-preview.component.html',
})
export class ListItemPreviewComponent {
  register = input.required<ListItemSetup>();
  contentSetup = input.required<ListContentSetup>();
  previewRecipe = createStableRecipe({
    identity: () => {
      const preview = this.register().preview;
      return {
        preview,
        factory: preview
          ? requireRecipeFactory(this.contentSetup().previewRegistry, preview['@type'], 'list preview')
          : undefined,
      };
    },
    equal: (previous, current) => this.samePreviewIdentity(previous, current),
    create: ({ preview, factory }) =>
      preview && factory
        ? factory({
            register: this.register,
            preview: computed(() => this.register().preview || preview),
            value: computed(() => get(this.register().source(), preview.valuePath || '', '')),
          })
        : null,
  });

  private samePreviewIdentity(
    previous: { preview: KokuDto.AbstractListViewItemPreviewDto | undefined; factory: unknown },
    current: { preview: KokuDto.AbstractListViewItemPreviewDto | undefined; factory: unknown },
  ): boolean {
    return previous.preview === current.preview && previous.factory === current.factory;
  }
}
