import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ActivityService} from "./activity.service";
import {ActivityDetailsComponent, ActivityDetailsComponentData} from "./activity-details/activity-details.component";
import * as moment from "moment";
import {padStart} from "lodash";

@Component({
  selector: 'activity',
  templateUrl: './activity.component.html',
  styleUrls: ['./activity.component.scss']
})
export class ActivityComponent {

  activity$: Observable<KokuDto.ActivityDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public activityService: ActivityService) {
    this.activity$ = this.activityService.getActivities();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.activityService.getActivities(debouncedValue));
  }

  openActivityDetails(activity: KokuDto.ActivityDto) {
    const dialogData: ActivityDetailsComponentData = {
      activityId: activity.id || 0
    };
    this.dialog.open(ActivityDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.ActivityDto) {
    return item.id;
  }

  addNewActivity() {
    const dialogData: ActivityDetailsComponentData = {};
    this.dialog.open(ActivityDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

  getReadableDuration(value: string) {
    const duration = moment.duration(value);
    if (duration.isValid()) {
      return padStart(String(duration.hours()), 2, '0') + ":" + padStart(String(duration.minutes()), 2, '0');
    } else {
      return "";
    }
  }

}
