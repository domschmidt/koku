import { booleanAttribute, Component, input, output, computed } from '@angular/core';
import { ListItemSetup } from '../../../list/list.component';
import { get } from '../../../utils/get';
import { ListItemActionComponent } from '../../../list/list-item-action/list-item-action.component';
import { KokuDynamicHostDirective } from '../../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../../dynamic-host/recipe.utils';
@Component({
  selector: '[http-call-action],http-call-action',
  imports: [KokuDynamicHostDirective],
  templateUrl: './condition-action.component.html',
  styleUrl: './condition-action.component.css',
})
export class ConditionActionComponent {
  value = input.required<KokuDto.ListViewConditionalItemValueActionDto>();
  register = input.required<ListItemSetup>();
  parent = input.required<ListItemActionComponent>();
  loading = input(false, { transform: booleanAttribute });
  clicked = output<MouseEvent>();
  actionRecipe = createStableRecipe({
    identity: () => {
      const value = this.value();
      const action = this.matchesPositively(value, this.register()) ? value.positiveAction : value.negativeAction;
      if (!action) {
        throw new Error('Conditional list action has no matching action');
      }
      const parent = this.parent();
      return {
        action,
        parent,
        factory: requireRecipeFactory(parent.contentSetup().actionRegistry, action['@type'], 'list action'),
      };
    },
    equal: (previous, current) =>
      previous.action === current.action && previous.parent === current.parent && previous.factory === current.factory,
    create: ({ action, parent, factory }) =>
      factory({
        action: computed(() => action),
        register: this.register,
        listRegister: parent.listRegister,
        contentSetup: parent.contentSetup,
        urlSegments: parent.urlSegments,
        parent,
      }),
  });
  matchesPositively(value: KokuDto.ListViewConditionalItemValueActionDto, listItemSetup: ListItemSetup) {
    let result = false;
    const currentValue = get(listItemSetup.source(), value.compareValuePath || '', null);
    for (const currentPositiveCompareValue of value.expectedValues || []) {
      if (currentValue == currentPositiveCompareValue) {
        result = true;
        break;
      }
    }
    return result;
  }
}
