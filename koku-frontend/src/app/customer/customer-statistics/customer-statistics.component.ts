import {Component, Input} from '@angular/core';

@Component({
  selector: 'customer-statistics',
  templateUrl: './customer-statistics.component.html',
  styleUrls: ['./customer-statistics.component.scss']
})
export class CustomerStatisticsComponent {

  @Input('customerId') customerId: number | undefined;

}
