import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ActivityCategoryService {
  private _activityCategories: BehaviorSubject<KokuDto.ActivityCategoryDto[]> = new BehaviorSubject(new Array<KokuDto.ActivityCategoryDto>());

  public readonly activityCategorys: Observable<KokuDto.ActivityCategoryDto[]> = this._activityCategories.asObservable();

  constructor(
    private httpClient: HttpClient
  ) {
  }

  public getActivityCategories(searchValue?: string): Observable<KokuDto.ActivityCategoryDto[]> {
    this.loadActivityCategories(searchValue).subscribe((result) => {
      this._activityCategories.next(result);
    });
    return this.activityCategorys;
  }

  getActivityCategory(activityCategoryId: number): Observable<KokuDto.ActivityCategoryDto> {
    return this.httpClient.get<KokuDto.ActivityCategoryDto>(`/api/activities/categories/${activityCategoryId}`);
  }

  createActivityCategory(activityCategory: KokuDto.ActivityCategoryDto) {
    return new Observable<KokuDto.ActivityCategoryDto>((observer) => {
      return this.httpClient.post<KokuDto.ActivityCategoryDto>(`/api/activities/categories`, activityCategory).subscribe((result: KokuDto.ActivityCategoryDto) => {
          this.loadActivityCategories().subscribe((newResult) => {
            this._activityCategories.next(newResult);
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

  updateActivityCategory(activityCategory: KokuDto.ActivityCategoryDto): Observable<any> {
    return new Observable((observer) => {
      return this.httpClient.put(`/api/activities/categories/${activityCategory.id}`, activityCategory).subscribe(() => {
        this.loadActivityCategories().subscribe((newResult) => {
          this._activityCategories.next(newResult);
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

  deleteActivityCategory(activityCategory: KokuDto.ActivityCategoryDto): Observable<any> {
    return new Observable((observer) => {
      return this.httpClient.delete(`/api/activities/categories/${activityCategory.id}`).subscribe(() => {
        this.loadActivityCategories().subscribe((newResult) => {
          this._activityCategories.next(newResult);
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

  private loadActivityCategories(searchValue?: string): Observable<KokuDto.ActivityCategoryDto[]> {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.ActivityCategoryDto[]>('/api/activities/categories', {params});
  }

}
