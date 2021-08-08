import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {FormControl, NgForm, Validators} from "@angular/forms";
import {ProductService} from "../product.service";
import {
  ProductManufacturerSelectionComponent,
  ProductManufacturerSelectionComponentData,
  ProductManufacturerSelectionComponentResponseData
} from "../../product-manufacturer/product-manufacturer-selection/product-manufacturer-selection.component";
import {Chart} from 'chart.js';
import * as moment from "moment";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'product-details',
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.scss']
})
export class ProductDetailsComponent implements OnInit, AfterViewInit {

  @Input('productId') productId: number | undefined;
  @Input('productName') productName: string | undefined;
  @Output() afterSaved = new EventEmitter<KokuDto.ProductDto>();
  @ViewChild('form') ngForm: NgForm | undefined;
  @Input() dialogActions: TemplateRef<any> | null = null;
  @Output() dirty = new EventEmitter<boolean>();

  product: KokuDto.ProductDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean = false;

  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  @ViewChild('priceChart') priceCharts: ElementRef<HTMLCanvasElement> | undefined;

  constructor(public productService: ProductService,
              private readonly dialog: MatDialog) {
  }

  save(product: KokuDto.ProductDto | undefined, form: NgForm) {
    if (form.valid && product) {
      this.saving = true;
      if (!product.id) {
        this.productService.createProduct(product).subscribe((response) => {
          this.saving = false;
          this.afterSaved.emit(response);
        }, () => {
          this.saving = false;
        });
      } else {
        this.productService.updateProduct(product).subscribe(() => {
          this.saving = false;
          this.afterSaved.emit(product);
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  ngOnInit(): void {
    this.priceCtl.valueChanges.subscribe(() => {
      this.dirty.emit(true);
    });
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
        description: this.productName || ''
      };
      this.loading = false;
    }
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty.emit((this.ngForm || {}).dirty || false);
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
    })

    dialogRef.afterClosed().subscribe((result: ProductManufacturerSelectionComponentResponseData) => {
      if (result && result.manufacturer && this.product) {
        this.product.manufacturer = result.manufacturer;
      }
    });
  }
}
