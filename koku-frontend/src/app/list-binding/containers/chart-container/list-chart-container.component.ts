import { Component, input } from '@angular/core';
import { ChartComponent, ChartFilterRegistry } from '../../../chart/chart.component';

@Component({
  selector: '[list-inline-chart-container],list-inline-chart-container',
  host: { class: 'flex w-full flex-col overflow-auto' },
  imports: [ChartComponent],
  templateUrl: './list-chart-container.component.html',
})
export class ListChartContainerComponent {
  chartUrl = input.required<string>();
  urlSegments = input<Record<string, string> | null>(null);
  filterRegistry = input.required<ChartFilterRegistry>();
}
