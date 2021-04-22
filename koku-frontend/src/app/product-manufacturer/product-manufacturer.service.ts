import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ProductManufacturerService {
  private _productManufacturers: BehaviorSubject<KokuDto.ProductManufacturerDto[]> = new BehaviorSubject(new Array<KokuDto.ProductManufacturerDto>());

  public readonly productManufacturers: Observable<KokuDto.ProductManufacturerDto[]> = this._productManufacturers.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getProductManufacturers(searchValue?: string) {
    this.loadProductManufacturers(searchValue).subscribe((result) => {
      this._productManufacturers.next(result)
    });
    return this.productManufacturers;
  }

  private loadProductManufacturers(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.ProductManufacturerDto[]>('/api/productmanufacturers', {params});
  }

  getProductManufacturer(productManufacturerId: number) {
    return this.httpClient.get<KokuDto.ProductManufacturerDto>(`/api/productmanufacturers/${productManufacturerId}`);
  }

  createProductManufacturer(productManufacturer: KokuDto.ProductManufacturerDto) {
    return new Observable<KokuDto.ProductManufacturerDto>((observer) => {
      return this.httpClient.post<KokuDto.ProductManufacturerDto>(`/api/productmanufacturers`, productManufacturer).subscribe((result: KokuDto.ProductManufacturerDto) => {
        this.loadProductManufacturers().subscribe((newResult) => {
          this._productManufacturers.next(newResult);
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

  updateProductManufacturer(productManufacturer: KokuDto.ProductManufacturerDto) {
    return new Observable((observer) => {
      return this.httpClient.put(`/api/productmanufacturers/${productManufacturer.id}`, productManufacturer).subscribe(() => {
        this.loadProductManufacturers().subscribe((newResult) => {
          this._productManufacturers.next(newResult);
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

  deleteProductManufacturer(productManufacturer: KokuDto.ProductManufacturerDto) {
    return new Observable((observer) => {
      return this.httpClient.delete(`/api/productmanufacturers/${productManufacturer.id}`).subscribe(() => {
        this.loadProductManufacturers().subscribe((newResult) => {
          this._productManufacturers.next(newResult);
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
