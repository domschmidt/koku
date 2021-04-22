import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ProductManufacturerService} from "./product-manufacturer.service";
import {
  ProductManufacturerDetailsComponent,
  ProductManufacturerDetailsComponentData
} from "./product-manufacturer-details/product-manufacturer-details.component";

@Component({
  selector: 'product-manufacturer',
  templateUrl: './product-manufacturer.component.html',
  styleUrls: ['./product-manufacturer.component.scss']
})
export class ProductManufacturerComponent {

  productManufacturers$: Observable<KokuDto.ProductManufacturerDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public productManufacturerService: ProductManufacturerService) {
    this.productManufacturers$ = this.productManufacturerService.getProductManufacturers();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.productManufacturerService.getProductManufacturers(debouncedValue));
  }

  openProductManufacturerDetails(manufacturer: KokuDto.ProductManufacturerDto) {
    const dialogData: ProductManufacturerDetailsComponentData = {
      manufacturerId: manufacturer.id || 0
    };
    this.dialog.open(ProductManufacturerDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.ProductManufacturerDto) {
    return item.id;
  }

  addNewProductManufacturer() {
    const dialogData: ProductManufacturerDetailsComponentData = {};
    this.dialog.open(ProductManufacturerDetailsComponent, {
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
