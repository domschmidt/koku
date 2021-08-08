import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {ActivityStepService} from "../activity-step.service";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

export interface ActivityStepDetailsComponentData {
  activityStepId?: number;
  activityStepName?: string;
}

export interface ActivityStepDetailsComponentResponseData {
  activityStep?: KokuDto.ActivityStepDto;
}


@Component({
  selector: 'activity-step-selection',
  templateUrl: './activity-step-details.component.html',
  styleUrls: ['./activity-step-details.component.scss']
})
export class ActivityStepDetailsComponent implements AfterViewInit {

  activityStep: KokuDto.ActivityStepDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  @ViewChild('form') ngForm: NgForm | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: ActivityStepDetailsComponentData,
              public dialogRef: MatDialogRef<ActivityStepDetailsComponent>,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public dialog: MatDialog,
              public activityStepService: ActivityStepService) {
    this.createMode = data.activityStepId === undefined;
    if (data.activityStepId) {
      this.activityStepService.getActivityStep(data.activityStepId).subscribe((activityStep) => {
        this.activityStep = activityStep;
        this.loading = false;
      }, () => {
        this.loading = false;
      });
    } else {
      this.activityStep = {
        '@type': 'ActivityStepDto',
        description: this.data.activityStepName || ''
      };
      this.loading = false;
    }
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
  }

  save(activityStep: KokuDto.ActivityStepDto | undefined, form: NgForm) {
    if (form.valid && activityStep) {
      this.saving = true;
      if (!activityStep.id) {
        this.activityStepService.createActivityStep(activityStep).subscribe((result) => {
          const dialogResult: ActivityStepDetailsComponentResponseData = {
            activityStep: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.activityStepService.updateActivityStep(activityStep).subscribe(() => {
          const dialogResult: ActivityStepDetailsComponentResponseData = {
            activityStep
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(activityStep: KokuDto.ActivityStepDto) {
    const dialogData: AlertDialogData = {
      headline: 'Termin Löschen',
      message: `Wollen Sie den Behandlungschritt wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.activityStepService.deleteActivityStep(activityStep).subscribe(() => {
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
