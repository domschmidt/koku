import {Component} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {MatDialog} from '@angular/material/dialog';
import {debounceTime, distinctUntilChanged} from 'rxjs/operators';
import {ActivityCategoryService} from './activity-category.service';
import {
  ActivityCategoryDetailsComponent,
  ActivityCategoryDetailsComponentData
} from './activity-category-details/activity-category-details.component';

@Component({
  selector: 'activity-step',
  templateUrl: './activity-category.component.html',
  styleUrls: ['./activity-category.component.scss']
})
export class ActivityCategoryComponent {

  activityCategories$: Observable<KokuDto.ActivityCategoryDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel = '';

  constructor(public dialog: MatDialog,
              public activityStepService: ActivityCategoryService) {
    this.activityCategories$ = this.activityStepService.getActivityCategories();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.activityStepService.getActivityCategories(debouncedValue));
  }

  openActivityCategoryDetails(activityCategory: KokuDto.ActivityCategoryDto): void {
    const dialogData: ActivityCategoryDetailsComponentData = {
      activityCategoryId: activityCategory.id || 0
    };
    this.dialog.open(ActivityCategoryDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.ActivityCategoryDto): number | undefined {
    return item.id;
  }

  addNewActivityCategory(): void {
    const dialogData: ActivityCategoryDetailsComponentData = {};
    this.dialog.open(ActivityCategoryDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  clearSearchField(): void {
    this.searchFieldModel = '';
    this.searchFieldChangeSubject.next('');
  }

}
