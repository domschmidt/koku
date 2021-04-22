import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {MatSnackBar} from "@angular/material/snack-bar";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {PrivateAppointmentService} from "./private-appointment.service";
import * as moment from "moment";

export interface PrivateAppointmentDetailsData {
  privateAppointmentId?: number;
  startDate?: string;
  startTime?: string;
  endDate?: string;
  endTime?: string;
}

export interface PrivateAppointmentDetailsResponseData {
  privateAppointment: KokuDto.PrivateAppointmentDto;
}

@Component({
  selector: 'private-appointment-details',
  templateUrl: './private-appointment-details.component.html',
  styleUrls: ['./private-appointment-details.component.scss']
})
export class PrivateAppointmentDetailsComponent implements AfterViewInit {

  privateAppointment: KokuDto.PrivateAppointmentDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  @ViewChild('form') ngForm: NgForm | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: PrivateAppointmentDetailsData,
              public dialogRef: MatDialogRef<PrivateAppointmentDetailsComponent>,
              public dialog: MatDialog,
              public snackBar: MatSnackBar,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public privateAppointmentService: PrivateAppointmentService) {
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => {
      this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
        this.dialogRef.close();
      });
    });
    this.dialogRef.keydownEvents().subscribe((event) => {
      if (event.key === 'Escape') {
        this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
          this.dialogRef.close();
        });
      }
    });
    this.createMode = data.privateAppointmentId === undefined;
    if (data.privateAppointmentId) {
      this.privateAppointmentService.getPrivateAppointment(data.privateAppointmentId).subscribe((privateAppointment) => {
        this.privateAppointment = privateAppointment;
        this.loading = false;
      });
    } else {
      this.privateAppointment = {
        '@type': 'PrivateAppointment',
        startDate: this.data.startDate,
        startTime: this.data.startTime,
        endDate: this.data.endDate,
        endTime: this.data.endTime,
      };
      this.loading = false;
    }
  }

  save(privateAppointment: KokuDto.PrivateAppointmentDto | undefined, form: NgForm) {
    if (form.valid && privateAppointment) {
      const endMoment = moment(privateAppointment.endDate + 'T' + privateAppointment.endTime);
      const startMoment = moment(privateAppointment.startDate + 'T' + privateAppointment.startTime);
      if (endMoment.isBefore(startMoment)) {
        this.snackBar.open('Das Enddatum liegt vor dem Startdatum', undefined, {
          duration: 3000,
          verticalPosition: 'top',
          politeness: "polite"
        });
      } else {
        this.saving = true;
        if (!privateAppointment.id) {
          this.privateAppointmentService.createPrivateAppointment(privateAppointment).subscribe(() => {
            const dialogResult: PrivateAppointmentDetailsResponseData = {
              privateAppointment: privateAppointment
            };
            this.dialogRef.close(dialogResult);
            this.saving = false;
          }, () => {
            this.saving = false;
          });
        } else {
          this.privateAppointmentService.updatePrivateAppointment(privateAppointment).subscribe(() => {
            const dialogResult: PrivateAppointmentDetailsResponseData = {
              privateAppointment: privateAppointment
            };
            this.dialogRef.close(dialogResult);
            this.saving = false;
          }, () => {
            this.saving = false;
          });
        }
      }
    }
  }

  delete(appointment: KokuDto.PrivateAppointmentDto) {
    this.saving = true;
    this.privateAppointmentService.deletePrivateAppointment(appointment).subscribe(() => {
      this.dialogRef.close();
      this.saving = false;
    }, () => {
      this.saving = false;
    });
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

}
