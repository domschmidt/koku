import {Component} from '@angular/core';
import {CustomerAppointmentService} from "../customer-appointment.service";
import {Observable} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {
  CustomerAppointmentDetailsComponent,
  CustomerAppointmentDetailsData
} from '../customer/customer-appointment-details/customer-appointment-details.component';
import * as moment from "moment";


@Component({
  selector: 'overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.scss']
})
export class OverviewComponent {
  appointmentGroups$: Observable<KokuDto.AppointmentGroupDto[]>;
  now = moment();
  calendarOptions = {
    sameDay: '[Heute (]DD.MM.YYYY[)]',
    nextDay: '[Morgen (]DD.MM.YYYY[)]',
    nextWeek: 'dddd [(]DD.MM.YYYY[)]',
    sameElse: 'dddd, [(]DD.MM.YYYY[)]'
  }

  constructor(public dialog: MatDialog,
              public readonly appointmentService: CustomerAppointmentService) {
    this.appointmentGroups$ = this.appointmentService.getAppointmentGroups();
  }

  trackByAppointmentGroup(index: number, item: KokuDto.AppointmentGroupDto) {
    return item.date;
  }

  trackByCustomerAppointment(index: number, item: KokuDto.CustomerAppointmentDto) {
    return item.id;
  }

  openCustomerAppointmentDetails(appointment: KokuDto.CustomerAppointmentDto) {
    const dialogData: CustomerAppointmentDetailsData = {
      customerAppointmentId: appointment.id
    };
    this.dialog.open(CustomerAppointmentDetailsComponent, {
      data: dialogData,
      autoFocus: false,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  addNewAppointment() {
    const dialogData: CustomerAppointmentDetailsData = {};
    this.dialog.open(CustomerAppointmentDetailsComponent, {
      data: dialogData,
      autoFocus: false,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }
}
