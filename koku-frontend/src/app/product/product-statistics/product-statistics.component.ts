import {Component, Input} from '@angular/core';

@Component({
  selector: 'product-statistics',
  templateUrl: './product-statistics.component.html',
  styleUrls: ['./product-statistics.component.scss']
})
export class ProductStatisticsComponent {

  @Input('productId') productId: number | undefined;

}
