import {Component, Inject} from '@angular/core';
import {
  CustomerInfoDialogComponent,
  CustomerInfoDialogData,
  CustomerInfoDialogResponseData
} from "../customer-info-dialog/customer-info-dialog.component";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";

@Component({
  selector: 'customer-create-dialog',
  templateUrl: './customer-create-dialog.component.html',
  styleUrls: ['./customer-create-dialog.component.scss']
})
export class CustomerCreateDialogComponent {

  customerId: number | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerInfoDialogData,
              private readonly dialog: MatDialog,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public dialogRef: MatDialogRef<CustomerInfoDialogComponent>) {
    this.customerId = data.customerId;
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => {
      this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
        this.dialogRef.close();
      });
    })
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

  formularDirty(dirty: boolean) {
    this.dirty = dirty;
  }

}
