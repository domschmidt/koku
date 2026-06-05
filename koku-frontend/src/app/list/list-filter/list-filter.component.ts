import { Component, input, output, computed } from '@angular/core';
import { ListContentSetup } from '../list.component';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import ListViewFilterContentDto = KokuDto.ListViewFilterContentDto;
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';
@Component({
  selector: '[list-filter],list-filter',
  imports: [KokuDynamicHostDirective],
  templateUrl: './list-filter.component.html',
})
export class ListFilterComponent {
  contentSetup = input.required<ListContentSetup>();
  filter = input.required<ListViewFilterContentDto>();
  filterChanged = output<KokuDto.QueryPredicate[]>();
  filterRecipe = createStableRecipe({
    identity: () => {
      const filter = this.filter();
      if (!filter.id || !filter.filterDefinition) {
        throw new Error('List filter requires an id and filter definition');
      }
      return {
        id: filter.id,
        filterDefinition: filter.filterDefinition,
        recipeDefinition: requireRecipeFactory(
          this.contentSetup().filterRegistry,
          filter.filterDefinition['@type'],
          'list filter',
        ),
      };
    },
    equal: (previous, current) =>
      previous.id === current.id &&
      previous.filterDefinition === current.filterDefinition &&
      previous.recipeDefinition === current.recipeDefinition,
    create: ({ filterDefinition, recipeDefinition }) =>
      recipeDefinition.createRecipe({
        filter: this.filter,
        filterDefinition: computed(() => this.filter().filterDefinition || filterDefinition),
        emit: (predicates: KokuDto.QueryPredicate[]) => this.filterChanged.emit(predicates),
      }),
  });
}
