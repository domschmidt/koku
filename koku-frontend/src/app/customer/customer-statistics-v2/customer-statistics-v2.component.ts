import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'customer-statistics',
  templateUrl: './customer-statistics-v2.component.html',
  styleUrls: ['./customer-statistics-v2.component.scss']
})
export class CustomerStatisticsV2Component implements OnInit{

  customerId: number | undefined;

  constructor(
    private readonly route: ActivatedRoute,
    ) {
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.customerId = Number(params.get('customerId'));
    });
  }
}
