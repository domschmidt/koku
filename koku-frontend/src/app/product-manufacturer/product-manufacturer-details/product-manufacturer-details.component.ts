import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {ProductManufacturerService} from "../product-manufacturer.service";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

export interface ProductManufacturerDetailsComponentData {
  manufacturerId?: number;
  manufacturerName?: string;
}

export interface ProductManufacturerDetailsComponentResponseData {
  manufacturer?: KokuDto.ProductManufacturerDto;
}


@Component({
  selector: 'product-manufacturer-details',
  templateUrl: './product-manufacturer-details.component.html',
  styleUrls: ['./product-manufacturer-details.component.scss']
})
export class ProductManufacturerDetailsComponent implements AfterViewInit {

  manufacturer: KokuDto.ProductManufacturerDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  @ViewChild('form') ngForm: NgForm | undefined;
  @ViewChild('priceChart') priceCharts: ElementRef<HTMLCanvasElement> | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: ProductManufacturerDetailsComponentData,
              public dialogRef: MatDialogRef<ProductManufacturerDetailsComponent>,
              public dialog: MatDialog,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              public productManufacturerService: ProductManufacturerService) {
    this.createMode = data.manufacturerId === undefined;
    if (data.manufacturerId) {
      this.productManufacturerService.getProductManufacturer(data.manufacturerId).subscribe((manufacturer) => {
        this.manufacturer = manufacturer;
        this.loading = false;
      }, () => {
        this.loading = false;
      });
    } else {
      this.manufacturer = {
        name: this.data.manufacturerName || ''
      };
      this.loading = false;
    }
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

  save(productManufacturer: KokuDto.ProductManufacturerDto | undefined, form: NgForm) {
    if (form.valid && productManufacturer) {
      this.saving = true;
      if (!productManufacturer.id) {
        this.productManufacturerService.createProductManufacturer(productManufacturer).subscribe((result) => {
          const dialogResult: ProductManufacturerDetailsComponentResponseData = {
            manufacturer: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.productManufacturerService.updateProductManufacturer(productManufacturer).subscribe(() => {
          const dialogResult: ProductManufacturerDetailsComponentResponseData = {
            manufacturer: productManufacturer
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(productManufacturer: KokuDto.ProductManufacturerDto) {
    const dialogData: AlertDialogData = {
      headline: 'Produkthersteller Löschen',
      message: `Wollen Sie den Hersteller mit dem Namen ${productManufacturer.name} wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.productManufacturerService.deleteProductManufacturer(productManufacturer).subscribe(() => {
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

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

}
