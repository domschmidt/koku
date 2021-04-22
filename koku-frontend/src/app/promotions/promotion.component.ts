import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {PromotionService} from "./promotion.service";
import {
  PromotionDetailsComponent,
  PromotionDetailsComponentData
} from "./promotion-details/promotion-details.component";

@Component({
  selector: 'promotion',
  templateUrl: './promotion.component.html',
  styleUrls: ['./promotion.component.scss']
})
export class PromotionComponent {

  promotions$: Observable<KokuDto.PromotionDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public promotionService: PromotionService) {
    this.promotions$ = this.promotionService.getPromotions();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.promotionService.getPromotions(debouncedValue));
  }

  openPromotionDetails(promotion: KokuDto.PromotionDto) {
    const dialogData: PromotionDetailsComponentData = {
      promotionId: promotion.id || 0
    };
    this.dialog.open(PromotionDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.PromotionDto) {
    return item.id;
  }

  addNewPromotion() {
    const dialogData: PromotionDetailsComponentData = {};
    this.dialog.open(PromotionDetailsComponent, {
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
