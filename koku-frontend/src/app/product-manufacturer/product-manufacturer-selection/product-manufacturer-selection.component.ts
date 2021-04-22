import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Observable, Subject} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ProductManufacturerService} from "../product-manufacturer.service";
import {
  ProductManufacturerDetailsComponent,
  ProductManufacturerDetailsComponentData,
  ProductManufacturerDetailsComponentResponseData
} from "../product-manufacturer-details/product-manufacturer-details.component";


export interface ProductManufacturerSelectionComponentData {
}

export interface ProductManufacturerSelectionComponentResponseData {
  manufacturer: KokuDto.ProductManufacturerDto;
}

@Component({
  selector: 'product-manufacturer-selection',
  templateUrl: './product-manufacturer-selection.component.html',
  styleUrls: ['./product-manufacturer-selection.component.scss']
})
export class ProductManufacturerSelectionComponent {

  productManufacturers$: Observable<KokuDto.ProductManufacturerDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public dialogRef: MatDialogRef<ProductManufacturerSelectionComponent>,
              public productManufacturerService: ProductManufacturerService) {
    this.productManufacturers$ = this.productManufacturerService.getProductManufacturers();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.productManufacturerService.getProductManufacturers(debouncedValue));
  }

  selectManufacturer(manufacturer: KokuDto.ProductManufacturerDto) {
    const dialogData: ProductManufacturerSelectionComponentResponseData = {
      manufacturer
    };
    this.dialogRef.close(dialogData);
  }

  trackByFn(index: number, item: KokuDto.ProductManufacturerDto) {
    return item.id;
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

  addNewProductManufacturer() {
    const dialogData: ProductManufacturerDetailsComponentData = {
      manufacturerName: this.searchFieldModel || ''
    };
    const dialogRef = this.dialog.open(ProductManufacturerDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });

    dialogRef.afterClosed().subscribe((result: ProductManufacturerDetailsComponentResponseData) => {
      if (result && result.manufacturer) {
        const dialogData: ProductManufacturerSelectionComponentResponseData = {
          manufacturer: result.manufacturer
        };
        this.dialogRef.close(dialogData);
      }
    });
  }

}
