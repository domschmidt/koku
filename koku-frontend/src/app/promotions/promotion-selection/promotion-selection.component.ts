import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Observable, Subject} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {PromotionService} from "../promotion.service";
import {
  PromotionDetailsComponent,
  PromotionDetailsComponentData,
  PromotionDetailsComponentResponseData
} from "../promotion-details/promotion-details.component";

export interface PromotionSelectionComponentResponseData {
  promotion: KokuDto.PromotionDto;
}

@Component({
  selector: 'promotion-selection',
  templateUrl: './promotion-selection.component.html',
  styleUrls: ['./promotion-selection.component.scss']
})
export class PromotionSelectionComponent {

  promotions$: Observable<KokuDto.PromotionDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public dialogRef: MatDialogRef<PromotionSelectionComponent>,
              public promotionService: PromotionService) {
    this.promotions$ = this.promotionService.getPromotions();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.promotionService.getPromotions(debouncedValue));
  }

  selectPromotion(promotion: KokuDto.PromotionDto) {
    const dialogData: PromotionSelectionComponentResponseData = {
      promotion
    };
    this.dialogRef.close(dialogData);
  }

  trackByFn(index: number, item: KokuDto.PromotionDto) {
    return item.id;
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

  addNewPromotion() {
    const dialogData: PromotionDetailsComponentData = {
      promotionName: this.searchFieldModel || ''
    };
    const dialogRef = this.dialog.open(PromotionDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });

    dialogRef.afterClosed().subscribe((result: PromotionDetailsComponentResponseData) => {
      if (result && result.promotion) {
        const dialogData: PromotionSelectionComponentResponseData = {
          promotion: result.promotion
        };
        this.dialogRef.close(dialogData);
      }
    });
  }

}
