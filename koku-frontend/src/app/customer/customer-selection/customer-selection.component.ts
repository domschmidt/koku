import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Observable, Subject} from "rxjs";
import {CustomerService} from "../customer.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {
  CustomerInfoDialogData,
  CustomerInfoDialogResponseData
} from "../customer-info-dialog/customer-info-dialog.component";
import {CustomerCreateDialogComponent} from "../customer-create-dialog/customer-create-dialog.component";


export interface CustomerSelectionComponentData {
}

export interface CustomerSelectionComponentResponseData {
  customer: KokuDto.CustomerDto;
}

@Component({
  selector: 'customer-selection',
  templateUrl: './customer-selection.component.html',
  styleUrls: ['./customer-selection.component.scss']
})
export class CustomerSelectionComponent {

  customers$: Observable<KokuDto.CustomerDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public dialogRef: MatDialogRef<CustomerSelectionComponent>,
              public customerService: CustomerService) {
    this.customers$ = this.customerService.getCustomers();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.customerService.getCustomers(debouncedValue));
  }

  selectCustomer(customer: KokuDto.CustomerDto) {
    const dialogData: CustomerSelectionComponentResponseData = {
      customer
    };
    this.dialogRef.close(dialogData);
  }

  trackByFn(index: number, item: KokuDto.CustomerDto) {
    return item.id;
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

  addNewCustomer() {
    const dialogData: CustomerInfoDialogData = {};
    const dialogRef = this.dialog.open(CustomerCreateDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });

    dialogRef.afterClosed().subscribe((result: CustomerInfoDialogResponseData) => {
      if (result && result.customer) {
        const dialogData: CustomerSelectionComponentResponseData = {
          customer: result.customer
        };
        this.dialogRef.close(dialogData);
      }
    });
  }

}
