import {AfterViewInit, Component, Inject, Output, ViewChild, ViewContainerRef} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {MatTabGroup} from "@angular/material/tabs";
import {CustomerService} from "../customer.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {
  CustomerAppointmentDetailsComponent,
  CustomerAppointmentDetailsData
} from "../customer-appointment-details/customer-appointment-details.component";
import {Subject} from "rxjs";

export interface CustomerInfoDialogData {
  customerId?: number;
}

export interface CustomerInfoDialogResponseData {
  customer?: KokuDto.CustomerDto;
}

@Component({
  selector: 'customer-info-dialog',
  templateUrl: './customer-info-dialog.component.html'
})
export class CustomerInfoDialogComponent implements AfterViewInit {
  customerId: number | undefined;
  createMode: boolean;
  customerTabs: {
    label: 'Info' | 'Termine' | 'Verkauf' | 'Documents' | 'Statistik';
    showInCreateMode: boolean;
  }[];
  activeTabIndex: number = 0;
  customer: KokuDto.CustomerDto | undefined;
  @ViewChild('tabHost', {read: ViewContainerRef}) tabHost: ViewContainerRef | undefined;
  @ViewChild('tabGroup', {read: MatTabGroup}) tabGroup: MatTabGroup | undefined;
  @Output() afterAppointmentChanged: Subject<undefined> = new Subject<undefined>();
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerInfoDialogData,
              public dialogRef: MatDialogRef<CustomerInfoDialogComponent>,
              public dialog: MatDialog,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              private readonly customerService: CustomerService) {
    this.customerId = data.customerId;
    if (this.customerId) {
      this.customerService.getCustomer(this.customerId).subscribe((customer) => {
        this.customer = customer;
      })
    }
    this.createMode = this.customerId === undefined;
    this.customerTabs = [{
      label: 'Info',
      showInCreateMode: true
    }, {
      label: 'Termine',
      showInCreateMode: false
    }, {
      label: 'Verkauf',
      showInCreateMode: false
    }, {
      label: 'Documents',
      showInCreateMode: false
    }, {
      label: 'Statistik',
      showInCreateMode: false
    }];
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => {
      this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
        this.dialogRef.close();
      });
    });
    this.dialogRef.keydownEvents().subscribe((event) => {
      if (event.key === 'Escape') {
        this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
          this.dialogRef.close();
        });
      }
    });
  }

  onCustomerSaved($event: KokuDto.CustomerDto) {
    const initials = ($event.firstName ? $event.firstName.trim().substring(0, 1) : '')
      + ($event.lastName ? $event.lastName.trim().substring(0, 1) : '');
    const dialogResult: CustomerInfoDialogResponseData = {
      customer: {
        ...$event,
        initials
      }
    };
    this.dialogRef.close(dialogResult);
  }

  delete(customer: KokuDto.CustomerDto) {
    const dialogData: AlertDialogData = {
      headline: 'Kunde Löschen',
      message: `Wollen Sie den Kunden mit dem Namen ${customer.firstName} ${customer.lastName} wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          if (this.customer) {
            this.customerService.deleteCustomer(this.customer).subscribe(() => {
              dialogRef.close();
              this.dialogRef.close();
            }, () => {
              button.loading = false;
            });
          }
        }
      }]
    };

    this.dialog.open(AlertDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  formularDirty(dirty: boolean) {
    this.dirty = dirty;
  }

  ngAfterViewInit(): void {
    if (this.tabGroup) {
      this.tabGroup.selectedIndexChange.subscribe((newTabIndex: number) => {
        if (newTabIndex !== this.activeTabIndex) {
          this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
            this.activeTabIndex = newTabIndex;
            this.dirty = false;
          }, () => {
            if (this.tabGroup) {
              this.tabGroup.selectedIndex = this.activeTabIndex;
            }
          });
        }
      })
    }
  }

  addNewCustomerAppointment(customer: KokuDto.CustomerDto) {
    const dialogData: CustomerAppointmentDetailsData = {
      customer: customer
    };
    const newCustomerAppointmentDialog = this.dialog.open(CustomerAppointmentDetailsComponent, {
      data: dialogData,
      autoFocus: false,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
    newCustomerAppointmentDialog.afterClosed().subscribe(() => {
      this.afterAppointmentChanged.next(undefined);
    });
  }
}
