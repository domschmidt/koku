import {Component} from '@angular/core';
import {MyUserDetailsService} from "../user/my-user-details.service";
import {Observable} from "rxjs";
import * as moment from "moment";
import {MatDialog} from "@angular/material/dialog";
import {CustomerAppointmentService} from "../customer-appointment.service";

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

}
