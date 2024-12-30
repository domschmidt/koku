import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ActivitySequenceService {
  private _allActivitySequences: BehaviorSubject<KokuDto.ActivitySequenceItemDtoUnion[]> = new BehaviorSubject(new Array<KokuDto.ActivitySequenceItemDtoUnion>());

  public readonly activitySequences: Observable<KokuDto.ActivitySequenceItemDtoUnion[]> = this._allActivitySequences.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getActivitySequences(searchValue?: string) {
    this.loadActivitySequences(searchValue).subscribe((result) => {
      this._allActivitySequences.next(result)
    });
    return this.activitySequences;
  }

  private loadActivitySequences(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.ActivitySequenceItemDtoUnion[]>('/backend/activitysequences', {params});
  }

}
