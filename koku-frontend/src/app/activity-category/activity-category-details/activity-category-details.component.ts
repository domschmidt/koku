import {AfterViewInit, Component, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {NgForm} from '@angular/forms';
import {ActivityCategoryService} from '../activity-category.service';
import {PreventLosingChangesService} from '../../prevent-losing-changes/prevent-losing-changes.service';
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from '../../alert-dialog/alert-dialog.component';

export interface ActivityCategoryDetailsComponentData {
  activityCategoryId?: number;
  activityCategoryName?: string;
}

export interface ActivityCategoryDetailsComponentResponseData {
  activityCategory?: KokuDto.ActivityCategoryDto;
}


@Component({
  selector: 'activity-category-selection',
  templateUrl: './activity-category-details.component.html',
  styleUrls: ['./activity-category-details.component.scss']
})
export class ActivityCategoryDetailsComponent implements AfterViewInit {

  activityCategory: KokuDto.ActivityCategoryDto | undefined;
  saving = false;
  loading = true;
  createMode: boolean;
  @ViewChild('form') ngForm: NgForm | undefined;
  private dirty = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: ActivityCategoryDetailsComponentData,
              public dialogRef: MatDialogRef<ActivityCategoryDetailsComponent>,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public dialog: MatDialog,
              public activityCategoryService: ActivityCategoryService) {
    this.createMode = data.activityCategoryId === undefined;
    if (data.activityCategoryId) {
      this.activityCategoryService.getActivityCategory(data.activityCategoryId).subscribe((activityCategory) => {
        this.activityCategory = activityCategory;
        this.loading = false;
      }, () => {
        this.loading = false;
      });
    } else {
      this.activityCategory = {
        description: this.data.activityCategoryName || ''
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

  save(activityCategory: KokuDto.ActivityCategoryDto | undefined, form: NgForm): void {
    if (form.valid && activityCategory) {
      this.saving = true;
      if (!activityCategory.id) {
        this.activityCategoryService.createActivityCategory(activityCategory).subscribe((result) => {
          const dialogResult: ActivityCategoryDetailsComponentResponseData = {
            activityCategory: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.activityCategoryService.updateActivityCategory(activityCategory).subscribe(() => {
          const dialogResult: ActivityCategoryDetailsComponentResponseData = {
            activityCategory
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(activityCategory: KokuDto.ActivityCategoryDto): void {
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
          this.activityCategoryService.deleteActivityCategory(activityCategory).subscribe(() => {
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
