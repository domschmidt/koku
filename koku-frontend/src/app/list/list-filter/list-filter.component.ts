import { Component, input, output } from '@angular/core';
import { ListContentSetup } from '../list.component';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective } from 'ng-dynamic-component';
import ListViewFilterContentDto = KokuDto.ListViewFilterContentDto;

@Component({
  selector: '[list-filter],list-filter',
  imports: [SignalComponentIoModule, DynamicIoDirective, ComponentOutletInjectorModule, DynamicComponent],
  templateUrl: './list-filter.component.html',
})
export class ListFilterComponent {
  contentSetup = input.required<ListContentSetup>();
  filter = input.required<ListViewFilterContentDto>();
  onFilterChange = output<KokuDto.QueryPredicate[]>();
}
