import { Component, input } from '@angular/core';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';
import type { ListItemActionComponent } from './list-item-action.component';

@Component({
  selector: 'list-action-renderer',
  imports: [KokuDynamicHostDirective],
  templateUrl: './list-action-renderer.component.html',
})
export class ListActionRendererComponent {
  action = input.required<KokuDto.AbstractListViewItemActionDto>();
  parent = input.required<ListItemActionComponent>();

  recipe = createStableRecipe({
    identity: () => {
      const action = this.action();
      const parent = this.parent();
      return {
        action,
        parent,
        factory: requireRecipeFactory(parent.contentSetup().actionRegistry, action['@type'], 'list action'),
      };
    },
    equal: (previous, current) =>
      previous.action === current.action && previous.parent === current.parent && previous.factory === current.factory,
    create: ({ factory, parent }) =>
      factory({
        action: this.action,
        register: parent.register,
        listRegister: parent.listRegister,
        contentSetup: parent.contentSetup,
        urlSegments: parent.urlSegments,
        parent,
      }),
  });
}
