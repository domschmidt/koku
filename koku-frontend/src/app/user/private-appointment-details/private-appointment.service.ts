import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class PrivateAppointmentService {
  constructor(public httpClient: HttpClient) {
  }

  getPrivateAppointment(id: number) {
    return this.httpClient.get<KokuDto.PrivateAppointmentDto>(`/api/users/@self/privateappointments/${id}`)
  }

  updatePrivateAppointment(privateAppointment: KokuDto.PrivateAppointmentDto) {
    return new Observable((observer) => {
      this.httpClient.put(`/api/users/@self/privateappointments/${privateAppointment.id}`, privateAppointment).subscribe(() => {
        observer.next();
        observer.complete();
      }, (error) => {
        observer.error(error);
      });
    });
  }

  createPrivateAppointment(customerAppointment: KokuDto.PrivateAppointmentDto) {
    return new Observable<KokuDto.PrivateAppointmentDto>((observer) => {
      this.httpClient.post<KokuDto.PrivateAppointmentDto>(`/api/users/@self/privateappointments`, customerAppointment).subscribe((result: KokuDto.PrivateAppointmentDto) => {
        observer.next(result);
        observer.complete();
      }, (error) => {
        observer.error(error);
      });
    });
  }

  deletePrivateAppointment(appointment: KokuDto.PrivateAppointmentDto) {
    return new Observable((observer) => {
      this.httpClient.delete(`/api/users/@self/privateappointments/${appointment.id}`).subscribe(() => {
        observer.next();
        observer.complete();
      }, (error) => {
        observer.error(error);
      });
    });
  }
}
