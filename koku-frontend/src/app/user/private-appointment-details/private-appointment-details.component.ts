import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {PrivateAppointmentService} from "./private-appointment.service";
import * as moment from "moment";
import {SnackBarService} from "../../snackbar/snack-bar.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

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
              public snackBarService: SnackBarService,
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
        this.snackBarService.openCommonSnack('Das Enddatum liegt vor dem Startdatum', 'top');
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
    const dialogData: AlertDialogData = {
      headline: 'Privattermin Löschen',
      message: `Wollen Sie den Termin wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.privateAppointmentService.deletePrivateAppointment(appointment).subscribe(() => {
            dialogRef.close();
            this.dialogRef.close();
          }, () => {
            button.loading = false;
          });
        }
      }]
    };

    this.dialog.open(AlertDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

}
