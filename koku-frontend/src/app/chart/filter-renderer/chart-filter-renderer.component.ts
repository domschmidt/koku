import { Component, input, output } from '@angular/core';
import { ChartFilterRegistry } from '../chart.component';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';

@Component({
  selector: '[chart-filter-renderer],chart-filter-renderer',
  imports: [KokuDynamicHostDirective],
  templateUrl: './chart-filter-renderer.component.html',
})
export class ChartFilterRendererComponent {
  content = input.required<KokuDto.AbstractChartFilterDto>();
  loading = input<boolean>(false);
  filterRegistry = input.required<ChartFilterRegistry>();

  filterValueChanged = output<string | number | boolean>();

  filterRecipe = createStableRecipe({
    identity: () => {
      const content = this.content();
      return {
        content,
        factory: requireRecipeFactory(this.filterRegistry(), content['@type'], 'chart filter'),
      };
    },
    equal: (previous, current) => previous.content === current.content && previous.factory === current.factory,
    create: ({ factory }) =>
      factory({
        content: this.content,
        loading: this.loading,
        emit: (value: string | number | boolean) => this.filterValueChanged.emit(value),
      }),
  });
}
