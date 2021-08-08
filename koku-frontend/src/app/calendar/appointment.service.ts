import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import * as moment from "moment";

export interface CalendarLoadSettings {
  start: Date
  end: Date
  privateAppointments: boolean;
  customerAppointments: boolean;
  customerBirthdays: boolean;
  userId?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {

  constructor(public httpClient: HttpClient) {

  }

  loadAppointments(loadSettings: CalendarLoadSettings) {
    const request: KokuDto.CalendarLoadSettingsDto = {
      start: moment(loadSettings.start).format('YYYY-MM-DD'),
      end: moment(loadSettings.end).format('YYYY-MM-DD'),
      loadCustomerAppointments: loadSettings.customerAppointments,
      loadCustomerBirthdays: loadSettings.customerBirthdays,
      loadPrivateAppointments: loadSettings.privateAppointments,
    }
    return this.httpClient.post<KokuDto.ICalendarContentUnion[]>(`/api/users/${loadSettings.userId || '@self'}/appointments`, request);
  }

}
