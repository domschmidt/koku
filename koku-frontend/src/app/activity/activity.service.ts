import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  private _activities: BehaviorSubject<KokuDto.ActivityDto[]> = new BehaviorSubject(new Array<KokuDto.ActivityDto>());

  public readonly activities: Observable<KokuDto.ActivityDto[]> = this._activities.asObservable();

  constructor(
    private httpClient: HttpClient
  ) {
  }

  public getActivities(
    searchValue?: string
  ): Observable<KokuDto.ActivityDto[]> {
    this.loadActivities(searchValue).subscribe((result) => {
      this._activities.next(result);
    });
    return this.activities;
  }

  getActivity(activityId: number): Observable<KokuDto.ActivityDto> {
    return this.httpClient.get<KokuDto.ActivityDto>(`/backend/activities/${activityId}`);
  }

  createActivity(activity: KokuDto.ActivityDto): Observable<KokuDto.ActivityDto> {
    return new Observable<KokuDto.ActivityDto>((observer) => {
      return this.httpClient.post(`/backend/activities`, activity).subscribe((result: KokuDto.ActivityDto) => {
        this.loadActivities().subscribe((newResult) => {
          this._activities.next(newResult);
          observer.next(result);
          observer.complete();
        }, (error) => {
          observer.error(error);
        });
      }, (error) => {
        observer.error(error);
      });
    });
  }

  updateActivity(activity: KokuDto.ActivityDto): Observable<any> {
    return new Observable((observer) => {
      return this.httpClient.put(`/backend/activities/${activity.id}`, activity).subscribe(() => {
        this.loadActivities().subscribe((newResult) => {
          this._activities.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        });
      }, (error) => {
        observer.error(error);
      });
    });
  }

  deleteActivity(activity: KokuDto.ActivityDto): Observable<any> {
    return new Observable((observer) => {
      return this.httpClient.delete(`/backend/activities/${activity.id}`).subscribe(() => {
        this.loadActivities().subscribe((newResult) => {
          this._activities.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        });
      }, (error) => {
        observer.error(error);
      });
    });
  }

  private loadActivities(
    searchValue?: string
  ): Observable<KokuDto.ActivityDto[]> {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.ActivityDto[]>('/backend/activities', {params});
  }

}
