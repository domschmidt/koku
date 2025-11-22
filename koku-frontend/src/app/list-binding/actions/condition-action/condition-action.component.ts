import {booleanAttribute, Component, input, output} from '@angular/core';
import {ListItemSetup} from '../../../list/list.component';
import {get} from '../../../utils/get';
import {ListItemActionComponent} from '../../../list/list-item-action/list-item-action.component';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';


@Component({
  selector: '[http-call-action],http-call-action',
  imports: [
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './condition-action.component.html',
  styleUrl: './condition-action.component.css'
})
export class ConditionActionComponent {

  value = input.required<KokuDto.ListViewConditionalItemValueActionDto>();
  register = input.required<ListItemSetup>();
  parent = input.required<ListItemActionComponent>();
  loading = input(false, {transform: booleanAttribute});

  onClick = output<MouseEvent>();

  matchesPositively(
    value: KokuDto.ListViewConditionalItemValueActionDto,
    listItemSetup: ListItemSetup
  ) {
    let result = false;
    let currentValue = get(listItemSetup.source(), value.compareValuePath || '', null)
    for (const currentPositiveCompareValue of value.expectedValues || []) {
      if (currentValue == currentPositiveCompareValue) {
        result = true;
        break;
      }
    }
    return result;
  }
}
