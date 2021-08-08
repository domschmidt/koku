import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {PromotionService} from "../promotion.service";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

export interface PromotionDetailsComponentData {
  promotionId?: number;
  promotionName?: string;
}

export interface PromotionDetailsComponentResponseData {
  promotion?: KokuDto.PromotionDto;
}


@Component({
  selector: 'promotion-details',
  templateUrl: './promotion-details.component.html',
  styleUrls: ['./promotion-details.component.scss']
})
export class PromotionDetailsComponent implements AfterViewInit {

  promotion: KokuDto.PromotionDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  productAbsouteSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  productRelativeSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  productAbsouteItemSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  productRelativeItemSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  activityAbsouteSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  activityRelativeSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  activityAbsouteItemSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  activityRelativeItemSavingsCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  priceCtl = new FormControl('', Validators.pattern('^\\d+(\\.\\d{0,2})?$'));
  @ViewChild('form') ngForm: NgForm | undefined;
  @ViewChild('priceChart') priceCharts: ElementRef<HTMLCanvasElement> | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: PromotionDetailsComponentData,
              public dialogRef: MatDialogRef<PromotionDetailsComponent>,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              private dialog: MatDialog,
              public promotionService: PromotionService) {
    this.createMode = data.promotionId === undefined;
    if (data.promotionId) {
      this.promotionService.getPromotion(data.promotionId).subscribe((promotion) => {
        this.promotion = promotion;
        this.loading = false;
      }, () => {
        this.loading = false;
      });
    } else {
      this.promotion = {
        name: this.data.promotionName || '',
        activitySettings: {},
        productSettings: {}
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

  save(promotion: KokuDto.PromotionDto | undefined, form: NgForm) {
    if (form.valid && promotion) {
      this.saving = true;
      if (!promotion.id) {
        this.promotionService.createPromotion(promotion).subscribe((result) => {
          const dialogResult: PromotionDetailsComponentResponseData = {
            promotion: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.promotionService.updatePromotion(promotion).subscribe(() => {
          const dialogResult: PromotionDetailsComponentResponseData = {
            promotion: promotion
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(promotion: KokuDto.PromotionDto) {
    const dialogData: AlertDialogData = {
      headline: 'Aktion Löschen',
      message: `Wollen Sie die Aktion mit dem Namen ${promotion.name} wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.promotionService.deletePromotion(promotion).subscribe(() => {
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
