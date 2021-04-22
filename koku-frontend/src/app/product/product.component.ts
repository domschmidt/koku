import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ProductService} from "./product.service";
import {
  ProductInfoDialogComponent,
  ProductInfoDialogComponentData
} from "./product-info-dialog/product-info-dialog.component";

@Component({
  selector: 'product',
  templateUrl: './product.component.html',
  styleUrls: ['./product.component.scss']
})
export class ProductComponent {

  product$: Observable<KokuDto.ProductDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public productService: ProductService) {
    this.product$ = this.productService.getProducts();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.productService.getProducts(debouncedValue));
  }

  openProductDetails(product: KokuDto.ProductDto) {
    const dialogData: ProductInfoDialogComponentData = {
      productId: product.id || 0
    };
    this.dialog.open(ProductInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.ProductDto) {
    return item.id;
  }

  addNewProduct() {
    const dialogData: ProductInfoDialogComponentData = {};
    this.dialog.open(ProductInfoDialogComponent, {
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
