import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatTabsModule} from "@angular/material/tabs";
import {MatIconModule} from "@angular/material/icon";
import {MatTreeModule} from "@angular/material/tree";
import {OverviewComponent} from './overview/overview.component';
import {CalendarComponent} from './calendar/calendar.component';
import {CustomerComponent} from './customer/customer.component';
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatListModule} from "@angular/material/list";
import {CustomerDetailsV2Component} from './customer/customer-details-v2/customer-details-v2.component';
import {MatDialogModule} from "@angular/material/dialog";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {MatMenuModule} from "@angular/material/menu";
import {CustomerAppointmentDetailsComponent} from './customer/customer-appointment-details/customer-appointment-details.component';
import {CustomerSelectionComponent} from './customer/customer-selection/customer-selection.component';
import {MomentModule} from "ngx-moment";
import 'moment/locale/de';
import {MatChipsModule} from "@angular/material/chips";
import {ActivityDetailsComponent} from './activity/activity-details/activity-details.component';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {ActivityComponent} from './activity/activity.component';
import {CustomerInfoDialogComponent} from './customer/customer-info-dialog/customer-info-dialog.component';
import {PortalModule} from "@angular/cdk/portal";
import {CommonModule} from "@angular/common";
import {CustomerAppointmentsComponent} from './customer/customer-appointments/customer-appointments.component';
import {DragDropModule} from "@angular/cdk/drag-drop";
import {MatToolbarModule} from "@angular/material/toolbar";
import dayGridPlugin from '@fullcalendar/daygrid';
import rrulePlugin from '@fullcalendar/rrule'
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import {FullCalendarModule} from '@fullcalendar/angular'; // the main connector. must go first
import {NaviService} from "./navi/navi.service";
import {MatSidenavModule} from "@angular/material/sidenav";
import {FlexLayoutModule} from '@angular/flex-layout';
import {MatCardModule} from "@angular/material/card";
import {GaugeModule} from "angular-gauge";
import {CustomerUploadsComponent} from "./customer/customer-uploads/customer-uploads.component";
import {NgxFileDropModule} from "ngx-file-drop";
import {CustomerDocumentCaptureDialogComponent} from "./customer/customer-document-capture-dialog/customer-document-capture-dialog.component";
import {DateFieldComponent} from "./fields/date/date-field.component";
import {SignatureFieldComponent} from "./fields/signature/signature-field.component";
import {TextFieldComponent} from "./fields/text/text-field.component";
import {SvgFieldComponent} from "./fields/svg/svg-field.component";
import {ProductInfoDialogComponent} from "./product/product-info-dialog/product-info-dialog.component";
import {ProductComponent} from "./product/product.component";
import {LoginComponent} from "./login/login.component";
import {LogoutComponent} from "./logout/logout.component";
import {UnauthorizedLoginInterceptorService} from "./unauthorized-login-interceptor.service";
import {WelcomeComponent} from "./welcome/welcome.component";
import {MyProfileComponent} from "./user/myprofile/my-profile.component";
import {ProductManufacturerDetailsComponent} from "./product-manufacturer/product-manufacturer-details/product-manufacturer-details.component";
import {ProductManufacturerSelectionComponent} from "./product-manufacturer/product-manufacturer-selection/product-manufacturer-selection.component";
import {ProductManufacturerComponent} from "./product-manufacturer/product-manufacturer.component";
import {AlertDialogComponent} from "./alert-dialog/alert-dialog.component";
import {ActivityStepDetailsComponent} from "./activity-step/activity-step-details/activity-step-details.component";
import {ActivityStepComponent} from "./activity-step/activity-step.component";
import {SortablejsModule} from "ngx-sortablejs";
import {CustomerCreateDialogComponent} from "./customer/customer-create-dialog/customer-create-dialog.component";
import {UserDetailsComponent} from "./user/user-details/user-details.component";
import {UserComponent} from "./user/user.component";
import {UserInfoComponent} from "./user/user-info/user-info.component";
import {MatExpansionModule} from '@angular/material/expansion';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {PrivateAppointmentDetailsComponent} from "./user/private-appointment-details/private-appointment-details.component";
import {CustomerAppointmentInlineInfoComponent} from "./customer/customer-appointment-inline-info/customer-appointment-inline-info.component";
import {NavigationEntryComponent} from "./navi/navigation-entry.component";
import {TabLayoutComponent} from "./layouts/tab-layout/tab-layout.component";
import {PageLayoutComponent} from "./layouts/page-layout/page-layout.component";
import {PanelLayoutComponent} from "./layouts/panel-layout/panel-layout.component";
import {ChartPanelComponent} from "./layouts/panel-layout/panels/chart/chart-panel.component";
import {CustomerStatisticsComponent} from "./customer/customer-statistics/customer-statistics.component";
import {ProductStatisticsComponent} from "./product/product-statistics/product-statistics.component";
import {ProductDetailsComponent} from "./product/product-details/product-details.component";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatDatepickerModule} from "@angular/material/datepicker";
import {ChartYearMonthFieldComponent} from "./layouts/panel-layout/panels/chart/year-month-field/chart-year-month-field.component";
import {DateAdapter, MAT_DATE_LOCALE} from "@angular/material/core";
import {MAT_MOMENT_DATE_ADAPTER_OPTIONS, MomentDateAdapter} from "@angular/material-moment-adapter";
import {GaugePanelComponent} from "./layouts/panel-layout/panels/gauge/gauge-panel.component";
import {TextPanelComponent} from "./layouts/panel-layout/panels/texts/text-panel.component";
import {MatSelectModule} from "@angular/material/select";
import {DocumentComponent} from "./document/document.component";
import {DocumentDetailsComponent} from "./document/document-details/document-details.component";
import {DocumentTextFieldComponent} from "./document/document-details/document-text-field/document-text-field.component";
import {DocumentSignatureFieldComponent} from "./document/document-details/document-signature-field/document-signature-field.component";
import {DocumentSvgFieldComponent} from "./document/document-details/document-svg-field/document-svg-field.component";
import {CustomerSalesComponent} from "./customer/customer-sales/customer-sales.component";
import {UserSelectionComponent} from "./user/user-selection/user-selection.component";
import {UserAvatarComponent} from "./user/user-avatar/user-avatar.component";
import {CircleWithLettersComponent} from "./circle-with-letters/circle-with-letters.component";
import {Chart, registerables} from 'chart.js';
import {CustomerAppointmentSoldProductInfoDialogComponent} from "./customer/customer-appointment-sold-product-info-dialog/customer-appointment-sold-product-info-dialog.component";
import {PromotionComponent} from "./promotions/promotion.component";
import {PromotionDetailsComponent} from "./promotions/promotion-details/promotion-details.component";
import {PromotionSelectionComponent} from "./promotions/promotion-selection/promotion-selection.component";
import {CustomerAppointmentActivityInfoDialogComponent} from "./customer/customer-appointment-activity-info-dialog/customer-appointment-activity-info-dialog.component";
import {DocumentCheckboxFieldComponent} from "./document/document-details/document-checkbox-field/document-checkbox-field.component";
import {CheckboxFieldComponent} from "./fields/checkbox/checkbox-field.component";
import {CustomerDetailsComponent} from "./customer/customer-details/customer-details.component";
import {RouterModule, Routes} from "@angular/router";
import {PreventNavigationIfModalIsOpenService} from "./prevent-losing-changes/prevent-navigation-if-modal-is-open.service";
import * as moment from "moment";
import {BreadcrumbLayoutComponent} from "./layouts/breadcrumb-layout/breadcrumb-layout.component";
import {CustomerAppointmentsV2Component} from "./customer/customer-appointments-v2/customer-appointments-v2.component";

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
    CustomerDetailsV2Component,
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
    PrivateAppointmentDetailsComponent,
    CustomerInfoDialogComponent,
    CustomerDocumentCaptureDialogComponent,
    CustomerAppointmentInlineInfoComponent,
    CustomerAppointmentsComponent,
    CustomerUploadsComponent,
    DateFieldComponent,
    SignatureFieldComponent,
    CustomerCreateDialogComponent,
    TextFieldComponent,
    SvgFieldComponent,
    NavigationEntryComponent,
    TabLayoutComponent,
    PageLayoutComponent,
    PanelLayoutComponent,
    ChartPanelComponent,
    CustomerStatisticsComponent,
    ProductStatisticsComponent,
    ProductDetailsComponent,
    ChartYearMonthFieldComponent,
    TextPanelComponent,
    GaugePanelComponent,
    DocumentComponent,
    DocumentDetailsComponent,
    DocumentSvgFieldComponent,
    DocumentTextFieldComponent,
    DocumentSignatureFieldComponent,
    CustomerSalesComponent,
    UserSelectionComponent,
    UserAvatarComponent,
    CircleWithLettersComponent,
    PromotionComponent,
    PromotionDetailsComponent,
    PromotionSelectionComponent,
    CustomerAppointmentActivityInfoDialogComponent,
    DocumentCheckboxFieldComponent,
    CheckboxFieldComponent,
    BreadcrumbLayoutComponent,
    CustomerAppointmentsV2Component,
  ],
  imports: [
    GaugeModule.forRoot(),
    FlexLayoutModule,
    CommonModule,
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
  ],
  providers: [
    NaviService,
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
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
