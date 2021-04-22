import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  private _activities: BehaviorSubject<KokuDto.ActivityDto[]> = new BehaviorSubject(new Array<KokuDto.ActivityDto>());

  public readonly activities: Observable<KokuDto.ActivityDto[]> = this._activities.asObservable();

  constructor(private _httpClient: HttpClient,
              private _snackBar: MatSnackBar) {
  }

  public getActivities(searchValue?: string) {
    this.loadActivities(searchValue).subscribe((result) => {
      this._activities.next(result)
    });
    return this.activities;
  }

  getActivity(activityId: number) {
    return this._httpClient.get<KokuDto.ActivityDto>(`/api/activities/${activityId}`);
  }

  createActivity(activity: KokuDto.ActivityDto) {
    return new Observable<KokuDto.ActivityDto>((observer) => {
      return this._httpClient.post(`/api/activities`, activity).subscribe((result: KokuDto.ActivityDto) => {
        this.loadActivities().subscribe((newResult) => {
          this._activities.next(newResult);
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

  updateActivity(activity: KokuDto.ActivityDto) {
    return new Observable((observer) => {
      return this._httpClient.put(`/api/activities/${activity.id}`, activity).subscribe(() => {
        this.loadActivities().subscribe((newResult) => {
          this._activities.next(newResult);
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

  deleteActivity(activity: KokuDto.ActivityDto) {
    return new Observable((observer) => {
      return this._httpClient.delete(`/api/activities/${activity.id}`).subscribe(() => {
        this.loadActivities().subscribe((newResult) => {
          this._activities.next(newResult);
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

  private loadActivities(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this._httpClient.get<KokuDto.ActivityDto[]>('/api/activities', {params});
  }

}
