import {AfterViewInit, Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {CustomerAppointmentService} from "../../customer-appointment.service";
import {
  CustomerSelectionComponent,
  CustomerSelectionComponentData,
  CustomerSelectionComponentResponseData
} from "../customer-selection/customer-selection.component";
import {
  ActivityDetailsComponent,
  ActivityDetailsComponentData,
  ActivityDetailsComponentResponseData
} from "../../activity/activity-details/activity-details.component";
import {COMMA, ENTER} from "@angular/cdk/keycodes";
import {FormControl, NgForm, Validators} from "@angular/forms";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {forkJoin, Observable} from "rxjs";
import {ActivityService} from "../../activity/activity.service";
import {map, startWith, take} from "rxjs/operators";
import BigNumber from 'bignumber.js';
import {ProductService} from "../../product/product.service";
import * as moment from 'moment';
import {ActivitySequenceService} from "../activity-sequence.service";
import {
  ActivityStepDetailsComponent,
  ActivityStepDetailsComponentData,
  ActivityStepDetailsComponentResponseData
} from "../../activity-step/activity-step-details/activity-step-details.component";
import {
  CustomerInfoDialogComponent,
  CustomerInfoDialogData
} from "../customer-info-dialog/customer-info-dialog.component";
import {PreventLosingChangesService} from "../../prevent-losing-changes/prevent-losing-changes.service";
import {
  ProductInfoDialogComponent,
  ProductInfoDialogComponentData,
  ProductInfoDialogComponentResponseData
} from "../../product/product-info-dialog/product-info-dialog.component";
import {Router} from "@angular/router";
import {
  UserSelectionComponent,
  UserSelectionComponentData,
  UserSelectionComponentResponseData
} from "../../user/user-selection/user-selection.component";
import {MyUserDetailsService} from "../../user/my-user-details.service";
import {
  CustomerAppointmentSoldProductInfoDialogComponent,
  CustomerAppointmentSoldProductInfoDialogComponentData,
  CustomerAppointmentSoldProductInfoDialogComponentResponseData
} from "../customer-appointment-sold-product-info-dialog/customer-appointment-sold-product-info-dialog.component";
import {PromotionService} from "../../promotions/promotion.service";
import {
  CustomerAppointmentActivityInfoDialogComponent,
  CustomerAppointmentActivityInfoDialogComponentData,
  CustomerAppointmentActivityInfoDialogComponentResponseData
} from "../customer-appointment-activity-info-dialog/customer-appointment-activity-info-dialog.component";
import {SnackBarService} from "../../snackbar/snack-bar.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

export interface CustomerAppointmentDetailsData {
  customer?: KokuDto.CustomerDto;
  customerAppointmentId?: number;
  startDate?: string;
  startTime?: string;
}

export interface CustomerAppointmentDetailsResponseData {
  customerAppointment: KokuDto.CustomerAppointmentDto;
}

@Component({
  selector: 'customer-appointment-details',
  templateUrl: './customer-appointment-details.component.html',
  styleUrls: ['./customer-appointment-details.component.scss']
})
export class CustomerAppointmentDetailsComponent implements AfterViewInit {

  now = moment();
  customerAppointment: KokuDto.CustomerAppointmentDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;
  separatorKeysCodes: number[] = [ENTER, COMMA];
  customerAppointmentActivityCtrl = new FormControl('');
  activitySequenceCtrl = new FormControl('');
  customerAppointmentSoldProductsCtrl = new FormControl('');
  promotionsCtrl = new FormControl('');
  revenueCtl = new FormControl('', Validators.pattern('^[0-9]+(\\.[0-9]{0,2})?$'));
  @ViewChild('customerAppointmentActivityInput') activityInput: ElementRef<HTMLInputElement> | undefined;
  @ViewChild('activitySequenceInput') activitySequenceInput: ElementRef<HTMLInputElement> | undefined;
  @ViewChild('customerAppointmentSoldProductsInput') soldProductsInput: ElementRef<HTMLInputElement> | undefined;
  @ViewChild('promotionsInput') promotionsInput: ElementRef<HTMLInputElement> | undefined;
  @ViewChild('form') ngForm: NgForm | undefined;
  allAvailableActivities$: Observable<KokuDto.ActivityDto[]>;
  allAvailableProducts$: Observable<KokuDto.ProductDto[]>;
  allAvailableActivitySequences$: Observable<KokuDto.ActivitySequenceItemDtoUnion[]>;
  allAvailablePromotions$: Observable<KokuDto.PromotionDto[]>;
  filteredActivities$: Observable<KokuDto.ActivityDto[]> | undefined;
  filteredActivitySequenceItems$: Observable<KokuDto.ActivitySequenceItemDtoUnion[]> | undefined;
  filteredSoldProducts$: Observable<KokuDto.ProductDto[]> | undefined;
  filteredPromotions$: Observable<KokuDto.PromotionDto[]> | undefined;
  private dirty: boolean = false;

  constructor(@Inject(MAT_DIALOG_DATA) public data: CustomerAppointmentDetailsData,
              public dialogRef: MatDialogRef<CustomerAppointmentDetailsComponent>,
              public dialog: MatDialog,
              public snackBarService: SnackBarService,
              public activityService: ActivityService,
              public router: Router,
              public customerAppointmentService: CustomerAppointmentService,
              public promotionService: PromotionService,
              public activitySequenceService: ActivitySequenceService,
              private readonly preventLosingChangesService: PreventLosingChangesService,
              private myUserDetailsService: MyUserDetailsService,
              public productService: ProductService) {
    this.allAvailableActivities$ = this.activityService.getActivities();
    this.allAvailableActivities$.subscribe((allActivities) => {
      this.filteredActivities$ = this.customerAppointmentActivityCtrl.valueChanges.pipe(
        startWith(null),
        map((searchTerm: string | null) => {
          if (typeof searchTerm === "string") {
            return this._filterActivities(searchTerm, allActivities);
          } else {
            return allActivities
          }
        })
      );
    });
    this.allAvailableProducts$ = this.productService.getProducts()
    this.allAvailableProducts$.subscribe((allProducts) => {
      this.filteredSoldProducts$ = this.customerAppointmentSoldProductsCtrl.valueChanges.pipe(
        startWith(null),
        map((searchTerm: string | null) => {
          let productsFound;
          if (typeof searchTerm === "string") {
            productsFound = this._filterProducts(searchTerm, allProducts);
          } else {
            productsFound = allProducts;
          }
          return productsFound;
        })
      );
    });

    this.allAvailableActivitySequences$ = this.activitySequenceService.getActivitySequences();
    this.allAvailableActivitySequences$.subscribe((allActivitySequences) => {
      this.filteredActivitySequenceItems$ = this.activitySequenceCtrl.valueChanges.pipe(
        startWith(null),
        map((searchTerm: string | null) => {
          let productsFound;
          if (typeof searchTerm === "string") {
            productsFound = this._filterActivitySequences(searchTerm, allActivitySequences);
          } else {
            productsFound = allActivitySequences;
          }
          return productsFound;
        })
      );
    });
    this.allAvailablePromotions$ = this.promotionService.getPromotions();
    this.allAvailablePromotions$.subscribe((allPromotions) => {
      this.filteredPromotions$ = this.promotionsCtrl.valueChanges.pipe(
        startWith(null),
        map((searchTerm: string | null) => {
          let promotionsFound;
          if (typeof searchTerm === "string") {
            promotionsFound = this._filterPromotions(searchTerm, allPromotions);
          } else {
            promotionsFound = allPromotions;
          }
          return promotionsFound;
        })
      );
    });

    this.createMode = data.customerAppointmentId === undefined;
    if (data.customerAppointmentId) {
      this.customerAppointmentService.getCustomerAppointment(data.customerAppointmentId).subscribe((customerAppointment) => {
        this.customerAppointment = customerAppointment;
        this.loading = false;
      });
    } else {
      forkJoin([
        this.promotionService.getActivePromotions(),
        this.myUserDetailsService.getDetails().pipe(take(1))
      ]).subscribe((data) => {
        this.customerAppointment = {
          '@type': 'CustomerAppointment',
          startDate: this.data.startDate,
          startTime: this.data.startTime,
          customer: this.data.customer,
          user: data[1],
          promotions: data[0]
        };
        this.loading = false;
      });
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

    this.customerAppointmentActivityCtrl.valueChanges.subscribe(() => {
      this.dirty = true;
    });
    this.activitySequenceCtrl.valueChanges.subscribe(() => {
      this.dirty = true;
    });
    this.customerAppointmentSoldProductsCtrl.valueChanges.subscribe(() => {
      this.dirty = true;
    });
    this.revenueCtl.valueChanges.subscribe(() => {
      this.dirty = true;
    });
  }

  save(customerAppointment: KokuDto.CustomerAppointmentDto | undefined, form: NgForm) {
    if (form.valid && customerAppointment) {
      this.saving = true;
      if (!customerAppointment.id) {
        this.customerAppointmentService.createCustomerAppointment(customerAppointment).subscribe(() => {
          const dialogResult: CustomerAppointmentDetailsResponseData = {
            customerAppointment: customerAppointment
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.customerAppointmentService.updateCustomerAppointment(customerAppointment).subscribe(() => {
          const dialogResult: CustomerAppointmentDetailsResponseData = {
            customerAppointment: customerAppointment
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(appointment: KokuDto.CustomerAppointmentDto) {
    const dialogData: AlertDialogData = {
      headline: 'Termin Löschen',
      message: `Wollen Sie den Termin wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.customerAppointmentService.deleteCustomerAppointment(appointment).subscribe(() => {
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

  selectCustomer() {
    const dialogData: CustomerSelectionComponentData = {};
    const dialogRef = this.dialog.open(CustomerSelectionComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });

    dialogRef.afterClosed().subscribe((result: CustomerSelectionComponentResponseData) => {
      if (result && result.customer && this.customerAppointment) {
        this.customerAppointment.customer = result.customer;
        this.dirty = true;
      }
    });
  }

  selectUser() {
    const dialogData: UserSelectionComponentData = {};
    const dialogRef = this.dialog.open(UserSelectionComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })

    dialogRef.afterClosed().subscribe((result: UserSelectionComponentResponseData) => {
      if (result && result.user && this.customerAppointment) {
        this.customerAppointment.user = result.user;
        this.dirty = true;
      }
    });
  }

  removeActivity(activity: KokuDto.ActivityDto) {
    const index = this.customerAppointment?.activities?.indexOf(activity);

    if (index !== undefined && index >= 0) {
      this.customerAppointment?.activities?.splice(index, 1);
      this.dirty = true;
    }
  }

  addNewActivity(currentValue: string) {
    const dialogData: ActivityDetailsComponentData = {
      activityName: currentValue
    };
    const dialogRef = this.dialog.open(ActivityDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })
    dialogRef.afterClosed().subscribe((result: ActivityDetailsComponentResponseData) => {
      if (result && result.activity && this.customerAppointment) {
        if (!this.customerAppointment.activities) {
          this.customerAppointment.activities = [];
        }
        this.customerAppointment.activities?.push({
          activity: result.activity
        });
        if (this.activityInput) {
          this.activityInput.nativeElement.value = '';
          this.activityInput.nativeElement.focus();
        }
        this.dirty = true;
      }
    });
  }

  addNewActivityStep(currentValue: string) {
    const dialogData: ActivityStepDetailsComponentData = {
      activityStepName: currentValue
    };
    const dialogRef = this.dialog.open(ActivityStepDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })
    dialogRef.afterClosed().subscribe((result: ActivityStepDetailsComponentResponseData) => {
      if (result && result.activityStep && this.customerAppointment) {
        if (!this.customerAppointment.activitySequenceItems) {
          this.customerAppointment.activitySequenceItems = [];
        }
        this.customerAppointment.activitySequenceItems?.push(result.activityStep);
        if (this.activitySequenceInput) {
          this.activitySequenceInput.nativeElement.value = '';
          this.activitySequenceInput.nativeElement.focus();
        }
        this.dirty = true;
      }
    });
  }

  addNewProductAsActivityStep(currentValue: string) {
    const dialogData: ProductInfoDialogComponentData = {
      productName: currentValue
    };
    const dialogRef = this.dialog.open(ProductInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })
    dialogRef.afterClosed().subscribe((result: ProductInfoDialogComponentResponseData) => {
      if (result && result.product && this.customerAppointment) {
        if (!this.customerAppointment.activitySequenceItems) {
          this.customerAppointment.activitySequenceItems = [];
        }
        this.customerAppointment.activitySequenceItems?.push(result.product);
        if (this.activitySequenceInput) {
          this.activitySequenceInput.nativeElement.value = '';
          this.activitySequenceInput.nativeElement.focus();
        }
        this.dirty = true;
      }
    });
  }

  addNewProductAsSoldProduct(currentValue: string) {
    const dialogData: ProductInfoDialogComponentData = {
      productName: currentValue
    };
    const dialogRef = this.dialog.open(ProductInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })
    dialogRef.afterClosed().subscribe((result: ProductInfoDialogComponentResponseData) => {
      if (result && result.product && this.customerAppointment) {
        if (!this.customerAppointment.soldProducts) {
          this.customerAppointment.soldProducts = [];
        }
        const newProduct: KokuDto.CustomerAppointmentSoldProductDto = {
          product: result.product
        }
        this.customerAppointment.soldProducts?.push(newProduct);
        if (this.soldProductsInput) {
          this.soldProductsInput.nativeElement.value = '';
          this.soldProductsInput.nativeElement.focus();
        }
        this.dirty = true;
      }
    });
  }

  selectedActivity($event: MatAutocompleteSelectedEvent): void {
    let alreadySelected;
    if (this.customerAppointment?.activities) {
      for (const currentCustomerAppointmentActivity of this.customerAppointment?.activities || []) {
        if (currentCustomerAppointmentActivity.activity?.id === $event.option.value.id) {
          alreadySelected = true;
          break;
        }
      }
    } else {
      alreadySelected = false;
    }
    if (!alreadySelected) {
      if (this.customerAppointment) {
        const newValue: KokuDto.CustomerAppointmentActivityDto = {
          activity: $event?.option?.value
        };
        if (this.customerAppointment?.activities) {
          this.customerAppointment.activities.push(newValue);
        } else {
          this.customerAppointment.activities = [newValue];
        }
      }
      if (this.activityInput) {
        this.activityInput.nativeElement.value = '';
        this.activityInput.nativeElement.focus();
      }
      this.customerAppointmentActivityCtrl.setValue(null);
      this.dirty = true;
    } else {
      this.snackBarService.openCommonSnack('bereits hinzugefügt', 'top');
    }
  }

  getApproxActivityRevenue(activities: KokuDto.CustomerAppointmentActivityDto[]) {
    let approxRevenue = new BigNumber(0);
    for (const customerAppointmentActivity of activities) {
      let sellPrice = new BigNumber(0);
      if (customerAppointmentActivity.sellPrice !== undefined) {
        sellPrice = new BigNumber(customerAppointmentActivity.sellPrice || 0);
      } else {
        sellPrice = new BigNumber(customerAppointmentActivity.activity?.currentPrice || 0);
        for (const currentPromotion of this.customerAppointment?.promotions || []) {
          if (currentPromotion.activitySettings?.absoluteItemSavings) {
            sellPrice = sellPrice.minus(new BigNumber(currentPromotion.activitySettings.absoluteItemSavings));
          }
        }
        for (const currentPromotion of this.customerAppointment?.promotions || []) {
          if (currentPromotion.activitySettings?.relativeItemSavings) {
            sellPrice = sellPrice.multipliedBy(new BigNumber(1).minus(new BigNumber(currentPromotion.activitySettings.relativeItemSavings).div(100)));
          }
        }
        if (sellPrice.lt(0)) {
          sellPrice = new BigNumber(0);
        }
      }

      approxRevenue = approxRevenue.plus(sellPrice);
    }

    for (const currentPromotion of this.customerAppointment?.promotions || []) {
      if (currentPromotion.activitySettings?.absoluteSavings) {
        approxRevenue = approxRevenue.minus(new BigNumber(currentPromotion.activitySettings.absoluteSavings));
      }
    }
    for (const currentPromotion of this.customerAppointment?.promotions || []) {
      if (currentPromotion.activitySettings?.relativeSavings) {
        approxRevenue = approxRevenue.multipliedBy(new BigNumber(1).minus(new BigNumber(currentPromotion.activitySettings.relativeSavings).div(100)));
      }
    }

    if (approxRevenue.lt(0)) {
      approxRevenue = new BigNumber(0);
    }

    return approxRevenue.toFixed(2, BigNumber.ROUND_HALF_UP);
  }

  getApproxProductRevenue(soldProducts: KokuDto.CustomerAppointmentSoldProductDto[]) {
    let approxRevenue = new BigNumber(0);
    for (const soldProduct of soldProducts) {
      let sellPrice = new BigNumber(0);
      if (soldProduct.sellPrice !== undefined) {
        sellPrice = new BigNumber(soldProduct.sellPrice || 0);
      } else {
        sellPrice = new BigNumber(soldProduct.product?.currentPrice || 0);
        for (const currentPromotion of this.customerAppointment?.promotions || []) {
          if (currentPromotion.productSettings?.absoluteItemSavings) {
            sellPrice = sellPrice.minus(new BigNumber(currentPromotion.productSettings.absoluteItemSavings));
          }
        }
        for (const currentPromotion of this.customerAppointment?.promotions || []) {
          if (currentPromotion.productSettings?.relativeItemSavings) {
            sellPrice = sellPrice.multipliedBy(new BigNumber(1).minus(new BigNumber(currentPromotion.productSettings.relativeItemSavings).div(100)));
          }
        }
        if (sellPrice.lt(0)) {
          sellPrice = new BigNumber(0);
        }
      }

      approxRevenue = approxRevenue.plus(sellPrice);
    }

    for (const currentPromotion of this.customerAppointment?.promotions || []) {
      if (currentPromotion.productSettings?.absoluteSavings) {
        approxRevenue = approxRevenue.minus(new BigNumber(currentPromotion.productSettings.absoluteSavings));
      }
    }
    for (const currentPromotion of this.customerAppointment?.promotions || []) {
      if (currentPromotion.productSettings?.relativeSavings) {
        approxRevenue = approxRevenue.multipliedBy(new BigNumber(1).minus(new BigNumber(currentPromotion.productSettings.relativeSavings).div(100)));
      }
    }

    if (approxRevenue.lt(0)) {
      approxRevenue = new BigNumber(0);
    }

    return approxRevenue.toFixed(2, BigNumber.ROUND_HALF_UP);
  }

  removeActivitySequenceItem(sequenceItem: KokuDto.ActivitySequenceItemDtoUnion) {
    const index = this.customerAppointment?.activitySequenceItems?.indexOf(sequenceItem);

    if (index !== undefined && index >= 0) {
      this.customerAppointment?.activitySequenceItems?.splice(index, 1);
      this.dirty = true;
    }
  }

  selectedActivitySequence($event: MatAutocompleteSelectedEvent) {
    let alreadySelected;
    if (this.customerAppointment?.activitySequenceItems && $event?.option?.value['@type'] !== 'ProductDto') {
      alreadySelected = this.customerAppointment.activitySequenceItems.find((value) => {
        return value.id === $event.option.value.id;
      }) !== undefined;
    } else {
      alreadySelected = false;
    }
    if (!alreadySelected) {
      if (this.customerAppointment) {
        if (this.customerAppointment?.activitySequenceItems) {
          this.customerAppointment.activitySequenceItems.push($event?.option?.value);
        } else {
          this.customerAppointment.activitySequenceItems = [$event?.option?.value];
        }
      }
      if (this.activitySequenceInput) {
        this.activitySequenceInput.nativeElement.value = '';
        this.activitySequenceInput.nativeElement.focus();
      }
      this.dirty = true;
      this.activitySequenceCtrl.setValue(null);
    } else {
      this.snackBarService.openCommonSnack('bereits hinzugefügt');
    }
  }

  removeSoldProduct(product: KokuDto.CustomerAppointmentSoldProductDto) {
    const index = this.customerAppointment?.soldProducts?.indexOf(product);

    if (index !== undefined && index >= 0) {
      this.customerAppointment?.soldProducts?.splice(index, 1);
      this.dirty = true;
    }
  }

  selectedSoldProduct($event: MatAutocompleteSelectedEvent) {
    if (this.customerAppointment) {
      const newValue: KokuDto.CustomerAppointmentSoldProductDto = {
        product: $event?.option?.value
      };
      if (this.customerAppointment?.soldProducts) {
        this.customerAppointment.soldProducts.push(newValue);
      } else {
        this.customerAppointment.soldProducts = [newValue];
      }
    }
    if (this.soldProductsInput) {
      this.soldProductsInput.nativeElement.value = '';
      this.soldProductsInput.nativeElement.focus();
    }
    this.dirty = true;
    this.customerAppointmentSoldProductsCtrl.setValue(null);
  }

  removePromotion(promotion: KokuDto.PromotionDto) {
    const index = this.customerAppointment?.promotions?.indexOf(promotion);

    if (index !== undefined && index >= 0) {
      this.customerAppointment?.promotions?.splice(index, 1);
      this.dirty = true;
    }
  }

  selectedPromotion($event: MatAutocompleteSelectedEvent) {
    if (this.customerAppointment) {
      const newValue: KokuDto.PromotionDto = $event?.option?.value;
      if (this.customerAppointment?.promotions) {
        this.customerAppointment.promotions.push(newValue);
      } else {
        this.customerAppointment.promotions = [newValue];
      }
    }
    if (this.promotionsInput) {
      this.promotionsInput.nativeElement.value = '';
      this.promotionsInput.nativeElement.focus();
    }
    this.dirty = true;
    this.promotionsCtrl.setValue(null);
  }

  getApproxDuration(customerAppointmentActivities: KokuDto.CustomerAppointmentActivityDto[]) {
    let duration = moment.duration(0);
    for (const customerAppointmentActivity of customerAppointmentActivities) {
      duration = duration.add(moment.duration(customerAppointmentActivity.activity?.approximatelyDuration));
    }
    return duration.asMinutes() + ' Minuten';
  }

  openCustomerDetails(customer: KokuDto.CustomerDto) {
    if (customer) {
      const dialogData: CustomerInfoDialogData = {
        customerId: customer.id
      };
      this.dialog.open(CustomerInfoDialogComponent, {
        data: dialogData,
        closeOnNavigation: false,
        position: {
          top: '20px'
        }
      });
    }
  }

  ngAfterViewInit(): void {
    this.ngForm?.statusChanges?.subscribe(() => {
      this.dirty = (this.ngForm || {}).dirty || false;
    });
  }

  private _filterActivities(searchTerm: string | null, allActivities: KokuDto.ActivityDto[]) {
    if (searchTerm) {
      const filterValue = searchTerm.toLowerCase();
      return allActivities.filter(activity => {
        let positionFound = activity.description?.toLowerCase().indexOf(filterValue);
        if (positionFound === undefined) {
          return false;
        } else {
          return positionFound >= 0;
        }
      });
    } else {
      return allActivities;
    }
  }

  private _filterProducts(searchTerm: string | null, allProducts: KokuDto.ProductDto[]) {
    if (searchTerm) {
      const filterValue = searchTerm.toLowerCase();
      return allProducts.filter(product => {
        let positionFound = product.description?.toLowerCase().indexOf(filterValue);
        if (positionFound === undefined) {
          return false;
        } else {
          return positionFound >= 0;
        }
      });
    } else {
      return allProducts;
    }
  }

  private _filterActivitySequences(searchTerm: string | null, allActivitySequences: KokuDto.ActivitySequenceItemDtoUnion[]) {
    if (searchTerm) {
      const filterValue = searchTerm.toLowerCase();
      return allActivitySequences.filter(activitySequence => {
        let positionFound = activitySequence.description?.toLowerCase().indexOf(filterValue);
        if (positionFound === undefined) {
          return false;
        } else {
          return positionFound >= 0;
        }
      });
    } else {
      return allActivitySequences;
    }
  }

  private _filterPromotions(searchTerm: string | null, allPromotions: KokuDto.PromotionDto[]) {
    if (searchTerm) {
      const filterValue = searchTerm.toLowerCase();
      return allPromotions.filter(promotion => {
        let positionFound = promotion.name?.toLowerCase().indexOf(filterValue);
        if (positionFound === undefined) {
          return false;
        } else {
          return positionFound >= 0;
        }
      });
    } else {
      return allPromotions;
    }
  }

  customerAppointmentSoldProductClicked(customerAppointmentSoldProduct: KokuDto.CustomerAppointmentSoldProductDto) {
    const dialogData: CustomerAppointmentSoldProductInfoDialogComponentData = {
      soldProduct: {
        ...customerAppointmentSoldProduct
      }
    };
    const dialogRef = this.dialog.open(CustomerAppointmentSoldProductInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })
    dialogRef.afterClosed().subscribe((result: CustomerAppointmentSoldProductInfoDialogComponentResponseData) => {
      if (result && result.soldProduct && this.customerAppointment) {
        const oldIndex = (this.customerAppointment.soldProducts || []).indexOf(customerAppointmentSoldProduct);
        if (oldIndex >= 0) {
          (this.customerAppointment.soldProducts || [])[oldIndex] = {...result.soldProduct};
          this.dirty = true;
        }
      }
    });
  }

  customerAppointmentActivityClicked(customerAppointmentActivity: KokuDto.CustomerAppointmentActivityDto) {
    const dialogData: CustomerAppointmentActivityInfoDialogComponentData = {
      activity: {
        ...customerAppointmentActivity
      }
    };
    const dialogRef = this.dialog.open(CustomerAppointmentActivityInfoDialogComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    })
    dialogRef.afterClosed().subscribe((result: CustomerAppointmentActivityInfoDialogComponentResponseData) => {
      if (result && result.activity && this.customerAppointment) {
        const oldIndex = (this.customerAppointment.activities || []).indexOf(customerAppointmentActivity);
        if (oldIndex >= 0) {
          (this.customerAppointment.activities || [])[oldIndex] = {...result.activity};
          this.dirty = true;
        }
      }
    });
  }
}
