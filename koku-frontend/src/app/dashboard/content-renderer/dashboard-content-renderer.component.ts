import { Component, input } from '@angular/core';
import { DashboardContent, DashboardContentRegistry } from '../dashboard.component';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';

@Component({
  selector: '[dashboard-content-renderer],dashboard-content-renderer',
  imports: [KokuDynamicHostDirective],
  templateUrl: './dashboard-content-renderer.component.html',
})
export class DashboardContentRendererComponent {
  content = input.required<DashboardContent>();
  contentRegistry = input.required<DashboardContentRegistry>();

  recipe = createStableRecipe({
    identity: () => {
      const content = this.content();
      if (!content.id) {
        throw new Error('Dashboard content requires a stable id');
      }
      return {
        id: content.id,
        factory: requireRecipeFactory(this.contentRegistry(), content['@type'], 'dashboard'),
      };
    },
    equal: (previous, current) => previous.id === current.id && previous.factory === current.factory,
    create: ({ factory }) =>
      factory({
        content: this.content,
        contentRegistry: this.contentRegistry,
      }),
  });
}
