import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconModule} from '@angular/material/icon';
import {MatTreeModule} from '@angular/material/tree';
import {OverviewComponent} from './overview/overview.component';
import {CalendarComponent} from './calendar/calendar.component';
import {CustomerComponent} from './customer/customer.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatListModule} from '@angular/material/list';
import {CustomerDetailsComponent} from './customer/customer-details/customer-details.component';
import {MatDialogModule} from '@angular/material/dialog';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatMenuModule} from '@angular/material/menu';
import {
  CustomerAppointmentDetailsComponent
} from './customer/customer-appointment-details/customer-appointment-details.component';
import {CustomerSelectionComponent} from './customer/customer-selection/customer-selection.component';
import {MomentModule} from 'ngx-moment';
import 'moment/locale/de';
import {MatChipsModule} from '@angular/material/chips';
import {ActivityDetailsComponent} from './activity/activity-details/activity-details.component';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {ActivityComponent} from './activity/activity.component';
import {CustomerInfoDialogComponent} from './customer/customer-info-dialog/customer-info-dialog.component';
import {PortalModule} from '@angular/cdk/portal';
import {CustomerAppointmentsComponent} from './customer/customer-appointments/customer-appointments.component';
import {DragDropModule} from '@angular/cdk/drag-drop';
import {MatToolbarModule} from '@angular/material/toolbar';
import dayGridPlugin from '@fullcalendar/daygrid';
import rrulePlugin from '@fullcalendar/rrule';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import {FullCalendarModule} from '@fullcalendar/angular'; // the main connector. must go first
import {NaviService} from './navi/navi.service';
import {MatSidenavModule} from '@angular/material/sidenav';
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatCardModule} from '@angular/material/card';
import {GaugeModule} from 'angular-gauge';
import {CustomerUploadsComponent} from './customer/customer-uploads/customer-uploads.component';
import {NgxFileDropModule} from 'ngx-file-drop';
import {
  CustomerDocumentCaptureDialogComponent
} from './customer/customer-document-capture-dialog/customer-document-capture-dialog.component';
import {ProductInfoDialogComponent} from './product/product-info-dialog/product-info-dialog.component';
import {ProductComponent} from './product/product.component';
import {LoginComponent} from './login/login.component';
import {LogoutComponent} from './logout/logout.component';
import {UnauthorizedLoginInterceptorService} from './unauthorized-login-interceptor.service';
import {WelcomeComponent} from './welcome/welcome.component';
import {MyProfileComponent} from './user/myprofile/my-profile.component';
import {
  ProductManufacturerDetailsComponent
} from './product-manufacturer/product-manufacturer-details/product-manufacturer-details.component';
import {
  ProductManufacturerSelectionComponent
} from './product-manufacturer/product-manufacturer-selection/product-manufacturer-selection.component';
import {ProductManufacturerComponent} from './product-manufacturer/product-manufacturer.component';
import {AlertDialogComponent} from './alert-dialog/alert-dialog.component';
import {ActivityStepDetailsComponent} from './activity-step/activity-step-details/activity-step-details.component';
import {ActivityStepComponent} from './activity-step/activity-step.component';
import {SortablejsModule} from 'ngx-sortablejs';
import {CustomerCreateDialogComponent} from './customer/customer-create-dialog/customer-create-dialog.component';
import {UserDetailsComponent} from './user/user-details/user-details.component';
import {UserComponent} from './user/user.component';
import {UserInfoComponent} from './user/user-info/user-info.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {
  PrivateAppointmentDetailsComponent
} from './user/private-appointment-details/private-appointment-details.component';
import {
  CustomerAppointmentInlineInfoComponent
} from './customer/customer-appointment-inline-info/customer-appointment-inline-info.component';
import {NavigationEntryComponent} from './navi/navigation-entry.component';
import {TabLayoutComponent} from './layouts/tab-layout/tab-layout.component';
import {PanelLayoutComponent} from './layouts/panel-layout/panel-layout.component';
import {ChartPanelComponent} from './layouts/panel-layout/panels/chart/chart-panel.component';
import {CustomerStatisticsComponent} from './customer/customer-statistics/customer-statistics.component';
import {ProductStatisticsComponent} from './product/product-statistics/product-statistics.component';
import {ProductDetailsComponent} from './product/product-details/product-details.component';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {
  ChartYearMonthFieldComponent
} from './layouts/panel-layout/panels/chart/year-month-field/chart-year-month-field.component';
import {DateAdapter, MAT_DATE_LOCALE} from '@angular/material/core';
import {MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter} from '@angular/material-moment-adapter';
import {GaugePanelComponent} from './layouts/panel-layout/panels/gauge/gauge-panel.component';
import {TextPanelComponent} from './layouts/panel-layout/panels/texts/text-panel.component';
import {MatSelectModule} from '@angular/material/select';
import {DocumentComponent} from './document/document.component';
import {
  DocumentTextConfigFieldComponent
} from './document-fields/document-text-field/config/document-text-config-field.component';
import {
  DocumentSignatureConfigFieldComponent
} from './document-fields/document-signature-field/config/document-signature-config-field.component';
import {
  DocumentSvgConfigFieldComponent
} from './document-fields/document-svg-field/config/document-svg-config-field.component';
import {CustomerSalesComponent} from './customer/customer-sales/customer-sales.component';
import {UserSelectionComponent} from './user/user-selection/user-selection.component';
import {UserAvatarComponent} from './user/user-avatar/user-avatar.component';
import {CircleWithLettersComponent} from './circle-with-letters/circle-with-letters.component';
import {Chart, registerables} from 'chart.js';
import {
  CustomerAppointmentSoldProductInfoDialogComponent
} from './customer/customer-appointment-sold-product-info-dialog/customer-appointment-sold-product-info-dialog.component';
import {PromotionComponent} from './promotions/promotion.component';
import {PromotionDetailsComponent} from './promotions/promotion-details/promotion-details.component';
import {PromotionSelectionComponent} from './promotions/promotion-selection/promotion-selection.component';
import {
  CustomerAppointmentActivityInfoDialogComponent
} from './customer/customer-appointment-activity-info-dialog/customer-appointment-activity-info-dialog.component';
import {
  DocumentCheckboxConfigFieldComponent
} from './document-fields/document-checkbox-field/config/document-checkbox-config-field.component';
import {ServiceWorkerModule} from '@angular/service-worker';
import {environment} from '../environments/environment';
import {PageSkeletonComponent} from './layouts/page-skeleton/page-skeleton.component';
import {DashboardComponent} from './dashboard/dashboard.component';
import {DashboardDiagramPanelComponent} from './dashboard/diagram/dashboard-diagram-panel.component';
import {DashboardDeferredPanelComponent} from './dashboard/deferred/dashboard-deferred-panel.component';
import {TableDiagramPanelComponent} from './dashboard/table/table-diagram-panel.component';
import {MatTableModule} from '@angular/material/table';
import {CalendarViewToggleComponent} from './calendar-view-toggle/calendar-view-toggle.component';
import {CalendarViewSettingsService} from './calendar-view-toggle/calendar-view-settings.service';
import {
  DocumentQrcodeConfigFieldComponent
} from './document-fields/document-qrcode-field/config/document-qrcode-config-field.component';
import {
  DocumentDateConfigFieldComponent
} from './document-fields/document-date-field/config/document-date-config-field.component';
import {
  DocumentCheckboxFieldComponent
} from './document-fields/document-checkbox-field/document-checkbox-field.component';
import {DocumentDateFieldComponent} from './document-fields/document-date-field/document-date-field.component';
import {DocumentQrcodeFieldComponent} from './document-fields/document-qrcode-field/document-qrcode-field.component';
import {
  DocumentSignatureFieldComponent
} from './document-fields/document-signature-field/document-signature-field.component';
import {DocumentTextFieldComponent} from './document-fields/document-text-field/document-text-field.component';
import {DocumentSvgFieldComponent} from './document-fields/document-svg-field/document-svg-field.component';
import {DocumentModule} from './document-designer-module/document.module';
import {DocumentDialogComponent} from './document/document-dialog.component';
import {DataTableModule} from './data-table-module/data-table.module';
import {FilesOverviewComponent} from './files-overview/files-overview.component';
import {
  AlphaNumericColumnFilterComponent
} from './data-table/alpha-numeric-columns/alpha-numeric-column-filter.component';
import {DATA_TABLE_CONFIG} from './data-table-module/data-table-config.injector';
import {AlphaNumericColumnComponent} from './data-table/alpha-numeric-columns/alpha-numeric-column.component';
import {BooleanColumnComponent} from './data-table/boolean-columns/boolean-column.component';
import {BooleanColumnFilterComponent} from './data-table/boolean-columns/boolean-column-filter.component';
import {DateTimeColumnComponent} from './data-table/date-time-columns/date-time-column.component';
import {DateTimeColumnFilterComponent} from './data-table/date-time-columns/date-time-column-filter.component';
import {QRCodeCaptureDialogComponent} from './qr-code-capture/qr-code-capture-dialog.component';
import {DocumentContextSelectionDialogComponent} from './document/document-context-selection-dialog.component';
import {FileUploadDialogComponent} from './files-overview/file-upload-dialog.component';
import {FileSizeColumnComponent} from './data-table/file-size-columns/file-size-column.component';
import {FileSizeColumnFilterComponent} from './data-table/file-size-columns/file-size-column-filter.component';
import {DocumentCaptureDialogComponent} from './document/document-capture-dialog.component';
import {NgxMaskModule} from 'ngx-mask';
import {SelectColumnComponent} from './data-table/select-columns/select-column.component';
import {SelectColumnFilterComponent} from './data-table/select-columns/select-column-filter.component';
import {
  DocumentActivityPriceListConfigFieldComponent
} from './document-fields/document-activity-price-list-field/config/document-activity-price-list-config-field.component';
import {
  DocumentActivityPriceListFieldComponent
} from './document-fields/document-activity-price-list-field/document-activity-price-list-field.component';
import {ActivityCategoryComponent} from './activity-category/activity-category.component';
import {
  ActivityCategoryDetailsComponent
} from './activity-category/activity-category-details/activity-category-details.component';
import {SortableDialogComponent} from './sortable-dialog/sortable-dialog.component';
import {SortableDialogItemComponent} from './sortable-dialog/sortable-dialog-item.component';

FullCalendarModule.registerPlugins([ // register FullCalendar plugins
  dayGridPlugin,
  interactionPlugin,
  timeGridPlugin,
  rrulePlugin
]);

Chart.register(...registerables);

@NgModule({
  declarations: [
    CustomerAppointmentSoldProductInfoDialogComponent,
    AppComponent,
    LoginComponent,
    WelcomeComponent,
    MyProfileComponent,
    LogoutComponent,
    OverviewComponent,
    CalendarComponent,
    CustomerComponent,
    CustomerDetailsComponent,
    CustomerAppointmentDetailsComponent,
    UserComponent,
    UserInfoComponent,
    UserDetailsComponent,
    CustomerSelectionComponent,
    ProductManufacturerSelectionComponent,
    ProductManufacturerComponent,
    ActivityStepDetailsComponent,
    ActivityStepComponent,
    ProductManufacturerDetailsComponent,
    ActivityDetailsComponent,
    ProductInfoDialogComponent,
    AlertDialogComponent,
    ProductComponent,
    ActivityComponent,
    SortableDialogItemComponent,
    ActivityCategoryComponent,
    ActivityCategoryDetailsComponent,
    PrivateAppointmentDetailsComponent,
    CustomerInfoDialogComponent,
    CustomerDocumentCaptureDialogComponent,
    CustomerAppointmentInlineInfoComponent,
    CustomerAppointmentsComponent,
    CustomerUploadsComponent,
    CustomerCreateDialogComponent,
    NavigationEntryComponent,
    TabLayoutComponent,
    PanelLayoutComponent,
    ChartPanelComponent,
    CustomerStatisticsComponent,
    ProductStatisticsComponent,
    ProductDetailsComponent,
    ChartYearMonthFieldComponent,
    TextPanelComponent,
    GaugePanelComponent,
    DocumentComponent,
    DocumentSvgConfigFieldComponent,
    DocumentTextConfigFieldComponent,
    DocumentSignatureConfigFieldComponent,
    CustomerSalesComponent,
    UserSelectionComponent,
    UserAvatarComponent,
    CircleWithLettersComponent,
    PromotionComponent,
    PromotionDetailsComponent,
    SortableDialogComponent,
    PromotionSelectionComponent,
    CustomerAppointmentActivityInfoDialogComponent,
    DocumentCheckboxConfigFieldComponent,
    PageSkeletonComponent,
    DashboardComponent,
    DashboardDiagramPanelComponent,
    DashboardDeferredPanelComponent,
    TableDiagramPanelComponent,
    CalendarViewToggleComponent,
    DocumentQrcodeConfigFieldComponent,
    DocumentDateConfigFieldComponent,
    DocumentCheckboxFieldComponent,
    DocumentDateFieldComponent,
    DocumentQrcodeFieldComponent,
    DocumentSignatureFieldComponent,
    DocumentTextFieldComponent,
    DocumentSvgFieldComponent,
    DocumentDialogComponent,
    FilesOverviewComponent,
    AlphaNumericColumnFilterComponent,
    AlphaNumericColumnComponent,
    BooleanColumnComponent,
    BooleanColumnFilterComponent,
    DateTimeColumnComponent,
    DateTimeColumnFilterComponent,
    QRCodeCaptureDialogComponent,
    DocumentContextSelectionDialogComponent,
    FileUploadDialogComponent,
    FileSizeColumnComponent,
    FileSizeColumnFilterComponent,
    DocumentCaptureDialogComponent,
    SelectColumnComponent,
    SelectColumnFilterComponent,
    DocumentActivityPriceListFieldComponent,
    DocumentActivityPriceListConfigFieldComponent
  ],
  imports: [
    DocumentModule,
    DataTableModule,
    GaugeModule.forRoot(),
    FlexLayoutModule,
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatTabsModule,
    MatSlideToggleModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressBarModule,
    MatTreeModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatDialogModule,
    MatExpansionModule,
    MatCardModule,
    MatSnackBarModule,
    MatCheckboxModule,
    FormsModule,
    MatButtonModule,
    HttpClientModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatMenuModule,
    MomentModule,
    MatChipsModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    PortalModule,
    DragDropModule,
    MatToolbarModule,
    FullCalendarModule,
    MatSidenavModule,
    NgxFileDropModule,
    SortablejsModule.forRoot({animation: 0}),
    MatDatepickerModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: environment.production,
      registrationStrategy: 'registerImmediately'
    }),
    MatTableModule,
    NgxMaskModule.forRoot(),
  ],
  providers: [
    NaviService,
    CalendarViewSettingsService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: UnauthorizedLoginInterceptorService,
      multi: true
    },
    {
      provide: DateAdapter,
      useClass: MomentDateAdapter,
      deps: [MAT_DATE_LOCALE, MAT_MOMENT_DATE_ADAPTER_OPTIONS]
    },
    {
      provide: DATA_TABLE_CONFIG,
      useValue: {
        columnTypes: {
          AlphaNumeric: {
            cellComponent: AlphaNumericColumnComponent,
            filterComponent: AlphaNumericColumnFilterComponent
          },
          Boolean: {
            cellComponent: BooleanColumnComponent,
            filterComponent: BooleanColumnFilterComponent,
            disableAdvancedFiltering: true
          },
          DateTime: {
            cellComponent: DateTimeColumnComponent,
            filterComponent: DateTimeColumnFilterComponent
          },
          FileSize: {
            cellComponent: FileSizeColumnComponent,
            filterComponent: FileSizeColumnFilterComponent
          },
          Select: {
            cellComponent: SelectColumnComponent,
            filterComponent: SelectColumnFilterComponent,
            disableAdvancedFiltering: true
          }
        }
      }
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
