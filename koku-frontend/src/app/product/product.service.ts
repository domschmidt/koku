import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private _products: BehaviorSubject<KokuDto.ProductDto[]> = new BehaviorSubject(new Array<KokuDto.ProductDto>());

  public readonly products: Observable<KokuDto.ProductDto[]> = this._products.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getProducts(searchValue?: string) {
    this.loadProducts(searchValue).subscribe((result) => {
      this._products.next(result)
    });
    return this.products;
  }

  getProduct(productId: number) {
    return this.httpClient.get<KokuDto.ProductDto>(`/backend/products/${productId}`);
  }

  createProduct(product: KokuDto.ProductDto) {
    return new Observable<KokuDto.ProductDto>((observer) => {
      return this.httpClient.post<KokuDto.ProductDto>(`/backend/products`, product).subscribe((result: KokuDto.ProductDto) => {
        this.loadProducts().subscribe((newResult) => {
          this._products.next(newResult);
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

  updateProduct(product: KokuDto.ProductDto) {
    return new Observable((observer) => {
      return this.httpClient.put(`/backend/products/${product.id}`, product).subscribe(() => {
        this.loadProducts().subscribe((newResult) => {
          this._products.next(newResult);
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

  deleteProduct(product: KokuDto.ProductDto) {
    return new Observable((observer) => {
      return this.httpClient.delete(`/backend/products/${product.id}`).subscribe(() => {
        this.loadProducts().subscribe((newResult) => {
          this._products.next(newResult);
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

  private loadProducts(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.ProductDto[]>('/backend/products', {params});
  }

}
