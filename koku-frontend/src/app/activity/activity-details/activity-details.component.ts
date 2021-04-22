import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ActivityService} from "../activity.service";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {Chart} from 'chart.js';
import {padStart} from 'lodash';

import * as moment from "moment";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import ActivityDto = KokuDto.ActivityDto;

export interface ActivityDetailsComponentData {
  activityId?: number;
  activityName?: string;
  activityStartDate?: string;
}

export interface ActivityDetailsComponentResponseData {
  activity?: ActivityDto;
}


@Component({
  selector: 'activity-selection',
  templateUrl: './activity-details.component.html',
  styleUrls: ['./activity-details.component.scss']
})
export class ActivityDetailsComponent implements AfterViewInit {

  activity: KokuDto.ActivityDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  @ViewChild('priceChart') priceCharts: ElementRef<HTMLCanvasElement> | undefined;
  @ViewChild('form') ngForm: NgForm | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: ActivityDetailsComponentData,
              public dialogRef: MatDialogRef<ActivityDetailsComponent>,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public activityService: ActivityService) {
    this.createMode = data.activityId === undefined;
    if (data.activityId) {
      this.activityService.getActivity(data.activityId).subscribe((activity) => {
        this.activity = activity;
        this.loading = false;
        setTimeout(() => {
          if (this.priceCharts) {
            const priceChartsContext = this.priceCharts.nativeElement.getContext('2d');
            if (priceChartsContext) {
              new Chart(priceChartsContext, {
                // The type of chart we want to create
                type: 'line',

                // The data for our dataset
                data: {
                  labels: (() => {
                    const result: string[] = [];

                    for (const currentHistoryEntry of activity.priceHistory || []) {
                      result.push(moment(currentHistoryEntry.recorded).format('DD.MM.YYYY'))
                    }

                    return result
                  })(),
                  datasets: [{
                    label: 'Preis',
                    backgroundColor: 'rgba(100, 57, 180, 0.25)',
                    borderColor: 'rgb(100, 57, 180)',
                    data: (() => {
                      const result: number[] = [];

                      for (const currentHistoryEntry of activity.priceHistory || []) {
                        result.push(currentHistoryEntry.price || 0)
                      }

                      return result
                    })(),
                  }]
                },

                // Configuration options go here
                options: {
                  animation: {
                    duration: 1000
                  }
                }
              })
            }
          }
        })
      }, () => {
        this.loading = false;
      });
    } else {
      this.activity = {
        description: this.data.activityName || ''
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
    this.priceCtl.valueChanges.subscribe(() => {
      this.dirty = true;
    })
  }

  save(activity: KokuDto.ActivityDto | undefined, form: NgForm) {
    if (form.valid && activity) {
      this.saving = true;
      if (!activity.id) {
        this.activityService.createActivity(activity).subscribe((result) => {
          const dialogResult: ActivityDetailsComponentResponseData = {
            activity: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.activityService.updateActivity(activity).subscribe(() => {
          const dialogResult: ActivityDetailsComponentResponseData = {
            activity
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(activity: KokuDto.ActivityDto) {
    this.saving = true;
    this.activityService.deleteActivity(activity).subscribe(() => {
      this.dialogRef.close();
      this.saving = false;
    });
  }

  durationToNumeric(approximatelyDuration: string): string {
    if (!approximatelyDuration) {
      return "";
    }
    const duration = moment.duration(approximatelyDuration);
    if (duration.isValid()) {
      return padStart(String(duration.hours()), 2, '0') + ":" + padStart(String(duration.minutes()), 2, '0');
    } else {
      return "";
    }
  }

  numericToDuration(value: string) {
    const duration = moment.duration(value);
    if (duration.isValid()) {
      return duration.toISOString();
    } else {
      return "";
    }
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

}
