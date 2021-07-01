import {Component, Input, OnInit} from '@angular/core';
import {CustomerService} from "../customer.service";
import * as moment from "moment";
import {MatDialog} from "@angular/material/dialog";
import {Observable, Subscription} from "rxjs";
import {ActivatedRoute} from "@angular/router";

@Component({
  selector: 'customer-appointments-v2',
  templateUrl: './customer-appointments-v2.component.html'
})
export class CustomerAppointmentsV2Component implements OnInit {
  customerId: number | undefined;
  customerAppointments: KokuDto.CustomerAppointmentDto[] | undefined;
  now = moment();
  @Input() customerAppointmentsChanged: Observable<undefined> | undefined;
  private eventsSubscription: Subscription | undefined;

  constructor(public customerService: CustomerService,
              private readonly route: ActivatedRoute,
              private _matDialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.loadAppointments();
    if (this.customerAppointmentsChanged) {
      this.eventsSubscription = this.customerAppointmentsChanged.subscribe(() => this.loadAppointments());
    }
  }

  private loadAppointments() {
    this.route.paramMap.subscribe((params) => {
      this.customerId = Number(params.get('customerId'));
      if (this.customerId) {
        this.customerService.getCustomerAppointments(this.customerId).subscribe((apiResponse) => {
          this.customerAppointments = apiResponse;
        });
      }
    });
  }

  trackByFn(index: number, item: KokuDto.CustomerAppointmentDto) {
    return item.id;
  }

  afterChanged(changedAppointment: KokuDto.CustomerAppointmentDto) {
    if (changedAppointment) {
      for (let customerAppointmentIndex = 0;
           customerAppointmentIndex < (this.customerAppointments || []).length;
           customerAppointmentIndex++){
        let currentAppointment = (this.customerAppointments || [])[customerAppointmentIndex];
        if (currentAppointment.id === changedAppointment.id) {
          (this.customerAppointments || [])[customerAppointmentIndex] = changedAppointment;
        }
      }
    } else {
      // refresh after potential change
      this.loadAppointments();
    }
  }

}
