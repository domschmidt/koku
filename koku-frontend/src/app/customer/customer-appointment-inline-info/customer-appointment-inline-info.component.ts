import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {CustomerAppointmentService} from "../../customer-appointment.service";
import {
  CustomerAppointmentDetailsComponent,
  CustomerAppointmentDetailsData,
  CustomerAppointmentDetailsResponseData
} from "../customer-appointment-details/customer-appointment-details.component";

@Component({
  selector: 'customer-appointment-inline-info',
  templateUrl: './customer-appointment-inline-info.component.html',
  styleUrls: ['./customer-appointment-inline-info.component.scss']
})
export class CustomerAppointmentInlineInfoComponent implements OnInit {

  @Input('appointmentId') appointmentId: number | undefined;
  @Output('afterChanged') afterChanged = new EventEmitter<KokuDto.CustomerAppointmentDto>();
  customerAppointment: KokuDto.CustomerAppointmentDto | undefined;

  constructor(public customerAppointmentService: CustomerAppointmentService,
              private _matDialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    if (this.appointmentId) {
      this.customerAppointmentService.getCustomerAppointment(this.appointmentId).subscribe((apiResponse) => {
        this.customerAppointment = apiResponse;
      });
    }
  }

  openAppointmentDetails($event: MouseEvent, customerAppointment: KokuDto.CustomerAppointmentDto) {
    $event.preventDefault();
    $event.stopPropagation();
    $event.stopImmediatePropagation();

    const dialogData: CustomerAppointmentDetailsData = {
      customerAppointmentId: customerAppointment.id
    };
    const dialogRef = this._matDialog.open(CustomerAppointmentDetailsComponent, {
      data: dialogData,
      position: {
        top: '20px'
      }
    });
    dialogRef.afterClosed().subscribe((data: CustomerAppointmentDetailsResponseData) => {
      if (data.customerAppointment) {
        this.customerAppointment = {...data.customerAppointment};
        this.afterChanged.emit(data.customerAppointment);
      } else {
        this.afterChanged.emit();
      }
    });
  }

}
