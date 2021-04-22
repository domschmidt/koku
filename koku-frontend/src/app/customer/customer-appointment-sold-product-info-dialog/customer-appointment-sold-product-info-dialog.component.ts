import {Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {CustomerAppointmentService} from "../../customer-appointment.service";

export interface CustomerAppointmentSoldProductInfoDialogComponentData {
  soldProduct: KokuDto.CustomerAppointmentSoldProductDto;
}

export interface CustomerAppointmentSoldProductInfoDialogComponentResponseData {
  soldProduct: KokuDto.CustomerAppointmentSoldProductDto;
}

@Component({
  selector: 'customer-appointment-sold-product-info-dialog',
  templateUrl: './customer-appointment-sold-product-info-dialog.component.html',
  styleUrls: ['./customer-appointment-sold-product-info-dialog.component.scss']
})
export class CustomerAppointmentSoldProductInfoDialogComponent {

  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  takeUsualPrice: boolean;
  @ViewChild('form') ngForm: NgForm | undefined;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerAppointmentSoldProductInfoDialogComponentData,
              public dialogRef: MatDialogRef<CustomerAppointmentSoldProductInfoDialogComponent>,
              public dialog: MatDialog,
              public customerAppointmentService: CustomerAppointmentService) {
    this.takeUsualPrice = data.soldProduct.sellPrice === undefined;
  }

  save(soldProduct: KokuDto.CustomerAppointmentSoldProductDto, form: NgForm) {
    if (form.valid && this.priceCtl.valid && soldProduct) {
      const dialogResult: CustomerAppointmentSoldProductInfoDialogComponentResponseData = {
        soldProduct: {
          ...soldProduct,
          sellPrice: this.takeUsualPrice ? undefined : (soldProduct.sellPrice || 0)
        }
      };
      this.dialogRef.close(dialogResult);
    }
  }

  toggleTakeUsualPrice() {
    this.takeUsualPrice = !this.takeUsualPrice;
    if (this.takeUsualPrice) {
      this.data.soldProduct.sellPrice = undefined;
    } else {
      this.data.soldProduct.sellPrice = this.data.soldProduct.product?.currentPrice || 0;
    }
  }
}
