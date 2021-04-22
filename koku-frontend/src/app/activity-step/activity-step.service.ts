import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class ActivityStepService {
  private _activitySteps: BehaviorSubject<KokuDto.ActivityStepDto[]> = new BehaviorSubject(new Array<KokuDto.ActivityStepDto>());

  public readonly activitySteps: Observable<KokuDto.ActivityStepDto[]> = this._activitySteps.asObservable();

  constructor(private _httpClient: HttpClient,
              private _snackBar: MatSnackBar) {
  }

  public getActivitySteps(searchValue?: string) {
    this.loadActivitySteps(searchValue).subscribe((result) => {
      this._activitySteps.next(result)
    });
    return this.activitySteps;
  }

  getActivityStep(activityStepId: number) {
    return this._httpClient.get<KokuDto.ActivityStepDto>(`/api/activitysteps/${activityStepId}`);
  }

  createActivityStep(activityStep: KokuDto.ActivityStepDto) {
    return new Observable<KokuDto.ActivityStepDto>((observer) => {
      return this._httpClient.post<KokuDto.ActivityStepDto>(`/api/activitysteps`, activityStep).subscribe((result: KokuDto.ActivityStepDto) => {
          this.loadActivitySteps().subscribe((newResult) => {
            this._activitySteps.next(newResult);
            observer.next(result);
            observer.complete();
          }, (error) => {
            observer.error(error);
          })
        }, (error) => {
          observer.error(error);
        }
      );
    });
  }

  updateActivityStep(activityStep: KokuDto.ActivityStepDto) {
    return new Observable((observer) => {
      return this._httpClient.put(`/api/activitysteps/${activityStep.id}`, activityStep).subscribe(() => {
        this.loadActivitySteps().subscribe((newResult) => {
          this._activitySteps.next(newResult);
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

  deleteActivityStep(activityStep: KokuDto.ActivityStepDto) {
    return new Observable((observer) => {
      return this._httpClient.delete(`/api/activitysteps/${activityStep.id}`).subscribe(() => {
        this.loadActivitySteps().subscribe((newResult) => {
          this._activitySteps.next(newResult);
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

  private loadActivitySteps(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this._httpClient.get<KokuDto.ActivityStepDto[]>('/api/activitysteps', {params});
  }

}
