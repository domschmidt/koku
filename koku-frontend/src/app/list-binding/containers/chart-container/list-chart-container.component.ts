import {Component, input} from '@angular/core';
import {ChartComponent, ChartContentSetup} from '../../../chart/chart.component';

@Component({
  selector: '[list-inline-chart-container],list-inline-chart-container',
  imports: [
    ChartComponent
  ],
  templateUrl: './list-chart-container.component.html',
  styleUrl: './list-chart-container.component.css'
})
export class ListChartContainerComponent {

  chartUrl = input.required<string>();
  urlSegments = input<{ [key: string]: string } | null>(null);
  chartContentSetup = input.required<ChartContentSetup>();

}
