import {Component, Input, OnInit} from '@angular/core';
import {CustomerService} from "../customer.service";
import {MatDialog} from "@angular/material/dialog";
import {Observable, Subscription} from "rxjs";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'customer-sales',
  templateUrl: './customer-sales-v2.component.html'
})
export class CustomerSalesV2Component implements OnInit {
  customerId: number | undefined;
  customerSales: KokuDto.CustomerSalesDto[] | undefined;
  @Input() customerAppointmentsChanged: Observable<undefined> | undefined;
  private eventsSubscription: Subscription | undefined;

  constructor(public customerService: CustomerService,
              private readonly route: ActivatedRoute,
              private _matDialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.loadSales();
    if (this.customerAppointmentsChanged) {
      this.eventsSubscription = this.customerAppointmentsChanged.subscribe(() => this.loadSales());
    }
  }

  private loadSales() {
    this.route.paramMap.subscribe((params) => {
      this.customerId = Number(params.get('customerId'));
      if (this.customerId) {
        this.customerService.getCustomerSales(this.customerId).subscribe((apiResponse) => {
          this.customerSales = apiResponse;
        });
      }
    });
  }

  trackByFn(index: number) {
    return index;
  }

}
