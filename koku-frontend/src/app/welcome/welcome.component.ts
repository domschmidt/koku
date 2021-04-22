import {Component} from '@angular/core';
import {MyUserDetailsService} from "../user/my-user-details.service";
import {Observable} from "rxjs";
import * as moment from "moment";
import {MatDialog} from "@angular/material/dialog";
import {CustomerAppointmentService} from "../customer-appointment.service";
import {
  CustomerAppointmentDetailsComponent,
  CustomerAppointmentDetailsData
} from "../customer/customer-appointment-details/customer-appointment-details.component";

@Component({
  selector: 'welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent {
  userDetails$: Observable<KokuDto.KokuUserDetailsDto>;
  appointmentGroups$: Observable<KokuDto.AppointmentGroupDto[]>;
  now = moment();

  constructor(private readonly service: MyUserDetailsService,
              public dialog: MatDialog,
              public readonly appointmentService: CustomerAppointmentService) {
    this.userDetails$ = this.service.getDetails();
    this.appointmentGroups$ = this.appointmentService.getAppointmentGroups();
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
