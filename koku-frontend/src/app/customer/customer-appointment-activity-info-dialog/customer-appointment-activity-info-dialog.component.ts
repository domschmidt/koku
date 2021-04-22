import {Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {CustomerAppointmentService} from "../../customer-appointment.service";

export interface CustomerAppointmentActivityInfoDialogComponentData {
  activity: KokuDto.CustomerAppointmentActivityDto;
}

export interface CustomerAppointmentActivityInfoDialogComponentResponseData {
  activity: KokuDto.CustomerAppointmentActivityDto;
}

@Component({
  selector: 'customer-appointment-sold-product-info-dialog',
  templateUrl: './customer-appointment-activity-info-dialog.component.html',
  styleUrls: ['./customer-appointment-activity-info-dialog.component.scss']
})
export class CustomerAppointmentActivityInfoDialogComponent {

  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  takeUsualPrice: boolean;
  @ViewChild('form') ngForm: NgForm | undefined;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerAppointmentActivityInfoDialogComponentData,
              public dialogRef: MatDialogRef<CustomerAppointmentActivityInfoDialogComponentData>,
              public dialog: MatDialog,
              public customerAppointmentService: CustomerAppointmentService) {
    this.takeUsualPrice = data.activity.sellPrice === undefined;
  }

  save(activity: KokuDto.CustomerAppointmentActivityDto, form: NgForm) {
    if (form.valid && this.priceCtl.valid && activity) {
      const dialogResult: CustomerAppointmentActivityInfoDialogComponentResponseData = {
        activity: {
          ...activity,
          sellPrice: this.takeUsualPrice ? undefined : (activity.sellPrice || 0)
        }
      };
      this.dialogRef.close(dialogResult);
    }
  }

  toggleTakeUsualPrice() {
    this.takeUsualPrice = !this.takeUsualPrice;
    if (this.takeUsualPrice) {
      this.data.activity.sellPrice = undefined;
    } else {
      this.data.activity.sellPrice = this.data.activity.activity?.currentPrice || 0;
    }
  }
}
