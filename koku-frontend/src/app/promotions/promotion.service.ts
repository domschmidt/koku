import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class PromotionService {
  private _promotions: BehaviorSubject<KokuDto.PromotionDto[]> = new BehaviorSubject(new Array<KokuDto.PromotionDto>());

  public readonly Promotions: Observable<KokuDto.PromotionDto[]> = this._promotions.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getPromotions(searchValue?: string) {
    this.loadPromotions(searchValue).subscribe((result) => {
      this._promotions.next(result)
    });
    return this.Promotions;
  }

  private loadPromotions(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.PromotionDto[]>('/backend/promotions', {params});
  }

  getPromotion(PromotionId: number) {
    return this.httpClient.get<KokuDto.PromotionDto>(`/backend/promotions/${PromotionId}`);
  }

  getActivePromotions() {
    const params = new HttpParams({
      fromObject: {
        activeOnly: 'true'
      }
    });
    return this.httpClient.get<KokuDto.PromotionDto[]>(`/backend/promotions`, {params});
  }

  createPromotion(promotion: KokuDto.PromotionDto) {
    return new Observable<KokuDto.PromotionDto>((observer) => {
      return this.httpClient.post<KokuDto.PromotionDto>(`/backend/promotions`, promotion).subscribe((result: KokuDto.PromotionDto) => {
        this.loadPromotions().subscribe((newResult) => {
          this._promotions.next(newResult);
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

  updatePromotion(promotion: KokuDto.PromotionDto) {
    return new Observable((observer) => {
      return this.httpClient.put(`/backend/promotions/${promotion.id}`, promotion).subscribe(() => {
        this.loadPromotions().subscribe((newResult) => {
          this._promotions.next(newResult);
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

  deletePromotion(promotion: KokuDto.PromotionDto) {
    return new Observable((observer) => {
      return this.httpClient.delete(`/backend/promotions/${promotion.id}`).subscribe(() => {
        this.loadPromotions().subscribe((newResult) => {
          this._promotions.next(newResult);
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

}
