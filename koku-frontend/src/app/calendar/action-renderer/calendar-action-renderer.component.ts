import { Component, input } from '@angular/core';
import { CalendarContentSetup } from '../calendar.component';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';

@Component({
  selector: '[calendar-action-renderer],calendar-action-renderer',
  imports: [KokuDynamicHostDirective],
  templateUrl: './calendar-action-renderer.component.html',
})
export class CalendarActionRendererComponent {
  action = input.required<KokuDto.AbstractCalendarActionDto>();
  contentSetup = input.required<CalendarContentSetup>();
  openRoutedContent = input.required<(routes: string[]) => void>();
  getPluginApi = input.required<<T = any>(id: string) => T | undefined>();

  recipe = createStableRecipe({
    identity: () => {
      const action = this.action();
      if (!action.id) {
        throw new Error('Calendar action requires a stable id');
      }
      return {
        id: action.id,
        factory: requireRecipeFactory(this.contentSetup().actionRegistry, action['@type'], 'calendar action'),
      };
    },
    equal: (previous, current) => previous.id === current.id && previous.factory === current.factory,
    create: ({ factory }) =>
      factory({
        action: this.action,
        contentSetup: this.contentSetup,
        openRoutedContent: this.openRoutedContent(),
        getPluginApi: this.getPluginApi(),
      }),
  });
}
