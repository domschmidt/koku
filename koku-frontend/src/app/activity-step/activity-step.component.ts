import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ActivityStepService} from "./activity-step.service";
import {
  ActivityStepDetailsComponent,
  ActivityStepDetailsComponentData
} from "./activity-step-details/activity-step-details.component";

@Component({
  selector: 'activity-step',
  templateUrl: './activity-step.component.html',
  styleUrls: ['./activity-step.component.scss']
})
export class ActivityStepComponent {

  activitySteps$: Observable<KokuDto.ActivityStepDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public activityStepService: ActivityStepService) {
    this.activitySteps$ = this.activityStepService.getActivitySteps();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.activityStepService.getActivitySteps(debouncedValue));
  }

  openActivityStepDetails(activityStep: KokuDto.ActivityStepDto) {
    const dialogData: ActivityStepDetailsComponentData = {
      activityStepId: activityStep.id || 0
    };
    this.dialog.open(ActivityStepDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.ActivityStepDto) {
    return item.id;
  }

  addNewActivityStep() {
    const dialogData: ActivityStepDetailsComponentData = {};
    this.dialog.open(ActivityStepDetailsComponent, {
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

}
