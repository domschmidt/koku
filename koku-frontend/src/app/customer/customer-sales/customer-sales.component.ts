import {Component, Input, OnInit} from '@angular/core';
import {CustomerService} from "../customer.service";
import {MatDialog} from "@angular/material/dialog";
import {Observable, Subscription} from "rxjs";

@Component({
  selector: 'customer-sales',
  templateUrl: './customer-sales.component.html'
})
export class CustomerSalesComponent implements OnInit {
  @Input('customerId') customerId: number | undefined;
  customerSales: KokuDto.CustomerSalesDto[] | undefined;
  @Input() customerAppointmentsChanged: Observable<undefined> | undefined;
  private eventsSubscription: Subscription | undefined;

  constructor(public customerService: CustomerService,
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
    if (this.customerId) {
      this.customerService.getCustomerSales(this.customerId).subscribe((apiResponse) => {
        this.customerSales = apiResponse;
      });
    }
  }

  trackByFn(index: number) {
    return index;
  }

}
