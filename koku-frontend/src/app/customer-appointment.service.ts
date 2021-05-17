import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class CustomerAppointmentService {

  private _appointmentGroups: BehaviorSubject<KokuDto.AppointmentGroupDto[]> = new BehaviorSubject(new Array<KokuDto.AppointmentGroupDto>());

  public readonly appointmentGroups: Observable<KokuDto.AppointmentGroupDto[]> = this._appointmentGroups.asObservable();

  private initialRequestTriggered = false;

  constructor(public httpClient: HttpClient) {

  }

  public getAppointmentGroups() {
    if (!this.initialRequestTriggered) {
      this.initialRequestTriggered = true;
      this.loadAppointmentGroups().subscribe((result) => {
        this._appointmentGroups.next(result)
      });
    }
    return this.appointmentGroups;
  }

  getCustomerAppointment(id: number) {
    return this.httpClient.get<KokuDto.CustomerAppointmentDto>(`/api/customers/appointments/${id}`);
  }

  updateCustomerAppointment(customerAppointment: KokuDto.CustomerAppointmentDto) {
    return new Observable((observer) => {
      this.httpClient.put(`/api/customers/appointments/${customerAppointment.id}`, customerAppointment).subscribe(() => {
        this.loadAppointmentGroups().subscribe((newResult) => {
          this._appointmentGroups.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  createCustomerAppointment(customerAppointment: KokuDto.CustomerAppointmentDto) {
    return new Observable<KokuDto.CustomerAppointmentDto>((observer) => {
      this.httpClient.post<KokuDto.CustomerAppointmentDto>(`/api/customers/appointments`, customerAppointment).subscribe((result: KokuDto.CustomerAppointmentDto) => {
        this.loadAppointmentGroups().subscribe((newResult) => {
          this._appointmentGroups.next(newResult);
          observer.next(result);
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  deleteCustomerAppointment(appointment: KokuDto.CustomerAppointmentDto) {
    return new Observable((observer) => {
      this.httpClient.delete(`/api/customers/appointments/${appointment.id}`).subscribe(() => {
        this.loadAppointmentGroups().subscribe((newResult) => {
          this._appointmentGroups.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  private loadAppointmentGroups() {
    return this.httpClient.get<KokuDto.AppointmentGroupDto[]>('/api/users/@self/appointmentgroups');
  }

  updateCustomerAppointmentTiming(customerAppointment: KokuDto.CustomerAppointmentDto) {
    return new Observable((observer) => {
      this.httpClient.put(`/api/customers/appointments/${customerAppointment.id}/timing`, customerAppointment).subscribe(() => {
        this.loadAppointmentGroups().subscribe((newResult) => {
          this._appointmentGroups.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

}
