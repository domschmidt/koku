import {Component, OnInit} from '@angular/core';
import {CustomerService} from "../customer.service";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'customer-name',
  templateUrl: './customer-name.component.html',
  styleUrls: ['./customer-name.component.scss']
})
export class CustomerNameComponent implements OnInit {
  customerId: number | undefined;
  customer: KokuDto.CustomerDto | undefined;

  constructor(public customerService: CustomerService,
              private readonly route: ActivatedRoute
  ) {
  }

  ngOnInit(): void {
    this.loadName();
  }

  private loadName() {
    this.route.paramMap.subscribe((params) => {
      this.customerId = Number(params.get('customerId'));
      if (this.customerId) {
        this.customerService.getCustomer(this.customerId).subscribe((apiResponse) => {
          this.customer = apiResponse;
        });
      }
    });
  }

}
