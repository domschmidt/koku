import {Component} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {Observable, Subject} from "rxjs";
import {CustomerService} from "./customer.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {
  CustomerInfoDialogComponent,
  CustomerInfoDialogData
} from "./customer-info-dialog/customer-info-dialog.component";

@Component({
  selector: 'customer',
  templateUrl: './customer.component.html',
  styleUrls: ['./customer.component.scss']
})
export class CustomerComponent {

  customers$: Observable<KokuDto.CustomerDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public customerService: CustomerService) {
    this.customers$ = this.customerService.getCustomers();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.customerService.getCustomers(debouncedValue));
  }

  openCustomerDetails(customer: KokuDto.CustomerDto) {
    const dialogData: CustomerInfoDialogData = {
      customerId: customer.id || 0,
    };
    this.dialog.open(CustomerInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.CustomerDto) {
    return item.id;
  }

  addNewCustomer() {
    const dialogData: CustomerInfoDialogData = {};
    this.dialog.open(CustomerInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }
}
