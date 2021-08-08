import {AfterViewInit, Component, ElementRef, Inject, ViewChild, ViewContainerRef} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {Chart} from 'chart.js';


import * as moment from "moment";
import {ProductService} from "../product.service";
import {
  ProductManufacturerSelectionComponent,
  ProductManufacturerSelectionComponentData,
  ProductManufacturerSelectionComponentResponseData
} from "../../product-manufacturer/product-manufacturer-selection/product-manufacturer-selection.component";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {MatTabGroup} from "@angular/material/tabs";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

export interface ProductInfoDialogComponentData {
  productId?: number;
  productName?: string;
  productStartDate?: string;
}

export interface ProductInfoDialogComponentResponseData {
  product?: KokuDto.ProductDto;
}


@Component({
  selector: 'product-info-dialog',
  templateUrl: './product-info-dialog.component.html',
  styleUrls: ['./product-info-dialog.component.scss']
})
export class ProductInfoDialogComponent implements AfterViewInit {

  productId: number | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  productTabs: {
    label: 'Info' | 'Statistik';
    showInCreateMode: boolean;
  }[];
  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  @ViewChild('priceChart') priceCharts: ElementRef<HTMLCanvasElement> | undefined;
  @ViewChild('form') ngForm: NgForm | undefined;
  activeTabIndex: number = 0;
  product: KokuDto.ProductDto | undefined;
  @ViewChild('tabHost', {read: ViewContainerRef}) tabHost: ViewContainerRef | undefined;
  @ViewChild('tabGroup', {read: MatTabGroup}) tabGroup: MatTabGroup | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: ProductInfoDialogComponentData,
              public dialogRef: MatDialogRef<ProductInfoDialogComponent>,
              public dialog: MatDialog,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public productService: ProductService) {
    this.productId = data.productId;
    this.createMode = this.productId === undefined;
    if (this.productId) {
      this.productService.getProduct(this.productId).subscribe((product) => {
        this.product = product;
        this.loading = false;
        setTimeout(() => {
          if (this.priceCharts) {
            const priceChartsContext = this.priceCharts.nativeElement.getContext('2d');
            if (priceChartsContext) {
              new Chart(priceChartsContext, {
                // The type of chart we want to create
                type: 'line',

                // The data for our dataset
                data: {
                  labels: (() => {
                    const result: string[] = [];

                    for (const currentHistoryEntry of product.priceHistory || []) {
                      result.push(moment(currentHistoryEntry.recorded).format('DD.MM.YYYY'))
                    }

                    return result
                  })(),
                  datasets: [{
                    label: 'Preis',
                    backgroundColor: 'rgba(100, 57, 180, 0.25)',
                    borderColor: 'rgb(100, 57, 180)',
                    data: (() => {
                      const result: number[] = [];

                      for (const currentHistoryEntry of product.priceHistory || []) {
                        result.push(currentHistoryEntry.price || 0)
                      }

                      return result
                    })(),
                  }]
                },

                // Configuration options go here
                options: {
                  animation: {
                    duration: 1000
                  }
                }
              })
            }
          }
        })
      });
    } else {
      this.product = {
        '@type': 'ProductDto',
        description: this.data.productName || ''
      };
      this.loading = false;
    }

    this.productTabs = [{
      label: 'Info',
      showInCreateMode: true
    }, {
      label: 'Statistik',
      showInCreateMode: false
    }];
    this.dialogRef.disableClose = true;
    this.dialogRef.backdropClick().subscribe(() => {
      this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
        this.dialogRef.close();
      });
    });
    this.dialogRef.keydownEvents().subscribe((event) => {
      if (event.key === 'Escape') {
        this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
          this.dialogRef.close();
        });
      }
    });

    this.priceCtl.valueChanges.subscribe(() => {
      this.dirty = true;
    });
  }

  onProductSaved(product: KokuDto.ProductDto) {
    const dialogResult: ProductInfoDialogComponentResponseData = {
      product: product
    };
    this.dialogRef.close(dialogResult);
  }

  delete(product: KokuDto.ProductDto) {
    const dialogData: AlertDialogData = {
      headline: 'Produkt Löschen',
      message: `Wollen Sie das Produkt mit dem Namen ${product.description} wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.productService.deleteProduct(product).subscribe(() => {
            dialogRef.close();
            this.dialogRef.close();
          }, () => {
            button.loading = false;
          });
        }
      }]
    };

    this.dialog.open(AlertDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  selectProductManufacturer() {
    const dialogData: ProductManufacturerSelectionComponentData = {};
    const dialogRef = this.dialog.open(ProductManufacturerSelectionComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });

    dialogRef.afterClosed().subscribe((result: ProductManufacturerSelectionComponentResponseData) => {
      if (result && result.manufacturer && this.product) {
        this.product.manufacturer = result.manufacturer;
      }
    });
  }

  formularDirty(dirty: boolean) {
    this.dirty = dirty;
  }

  ngAfterViewInit(): void {
    if (this.tabGroup) {
      this.tabGroup.selectedIndexChange.subscribe((newTabIndex: number) => {
        if (newTabIndex !== this.activeTabIndex) {
          this.preventLosingChangesService.preventLosingChanges(this.dirty, () => {
            this.activeTabIndex = newTabIndex;
            this.dirty = false;
          }, () => {
            if (this.tabGroup) {
              this.tabGroup.selectedIndex = this.activeTabIndex;
            }
          });
        }
      })
    }
  }

}
