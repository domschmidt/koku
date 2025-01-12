import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {OverviewComponent} from "./overview/overview.component";
import {CustomerComponent} from "./customer/customer.component";
import {CalendarComponent} from "./calendar/calendar.component";
import {ActivityComponent} from "./activity/activity.component";
import {ProductComponent} from "./product/product.component";
import {LoginComponent} from "./login/login.component";
import {LogoutComponent} from "./logout/logout.component";
import {WelcomeComponent} from "./welcome/welcome.component";
import {MyProfileComponent} from "./user/myprofile/my-profile.component";
import {ProductManufacturerComponent} from "./product-manufacturer/product-manufacturer.component";
import {ActivityStepComponent} from "./activity-step/activity-step.component";
import {UserComponent} from "./user/user.component";
import {TabLayoutComponent} from "./layouts/tab-layout/tab-layout.component";
import {PanelLayoutComponent} from "./layouts/panel-layout/panel-layout.component";
import {ChartPanelComponent} from "./layouts/panel-layout/panels/chart/chart-panel.component";
import {
  PreventNavigationIfModalIsOpenService
} from "./prevent-losing-changes/prevent-navigation-if-modal-is-open.service";
import {DocumentComponent} from "./document/document.component";
import {PromotionComponent} from "./promotions/promotion.component";
import {PageSkeletonComponent} from "./layouts/page-skeleton/page-skeleton.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {CalendarViewToggleComponent} from "./calendar-view-toggle/calendar-view-toggle.component";
import {FilesOverviewComponent} from "./files-overview/files-overview.component";
import {ActivityCategoryComponent} from './activity-category/activity-category.component';

const routes: Routes = [
  {
    path: '',
    component: PageSkeletonComponent,
    children: [
      {
        path: 'appointments',
        data: {
          name: 'Termine'
        },
        children: [
          {
            path: 'calendar',
            data: {
              name: 'Kalender'
            },
            children: [
              {
                path: '',
                component: CalendarComponent,
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                component: CalendarViewToggleComponent,
                path: '',
                outlet: 'toolbar-right'
              }
            ]
          },
          {
            component: OverviewComponent,
            path: 'overview',
            data: {
              name: 'Übersicht'
            },
            canDeactivate: [PreventNavigationIfModalIsOpenService]
          },
          {
            path: '',
            redirectTo: 'calendar',
            pathMatch: 'full'
          },
        ]
      },
      {
        path: 'files',
        data: {
          name: 'Dateiübersicht'
        },
        component: FilesOverviewComponent
      },
      {
        path: 'manage',
        data: {
          name: 'Stammdaten'
        },
        children: [
          {
            component: TabLayoutComponent,
            path: '',
            children: [
              {
                path: 'customer',
                component: CustomerComponent,
                data: {
                  name: 'Kunden'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'product',
                component: ProductComponent,
                data: {
                  name: 'Produkte'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'productmanufacturer',
                component: ProductManufacturerComponent,
                data: {
                  name: 'Produkthersteller'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'activitysteps',
                component: ActivityStepComponent,
                data: {
                  name: 'Behandlungsschritte'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'activity',
                component: ActivityComponent,
                data: {
                  name: 'Tätigkeiten'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'activitycategories',
                component: ActivityCategoryComponent,
                data: {
                  name: 'Tätigkeitskategorien'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'promotions',
                component: PromotionComponent,
                data: {
                  name: 'Aktionen'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: '',
                redirectTo: 'customer',
                pathMatch: 'full'
              },
            ]
          }
        ]
      },
      {
        path: 'administration',
        data: {
          name: 'Administration'
        },
        children: [
          {
            component: TabLayoutComponent,
            path: '',
            children: [
              {
                path: 'users',
                component: UserComponent,
                data: {
                  name: 'Nutzer'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: 'documents',
                component: DocumentComponent,
                data: {
                  name: 'Dokumente'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: '',
                redirectTo: 'users',
                pathMatch: 'full'
              },
            ]
          },
        ]
      },
      {
        path: 'statistics',
        data: {
          name: 'Statistik'
        },
        children: [
          {
            component: TabLayoutComponent,
            path: '',
            children: [
              {
                path: 'dashboard',
                component: DashboardComponent,
                data: {
                  name: 'Dashboard'
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                component: PanelLayoutComponent,
                path: 'revenues',
                data: {
                  name: 'Umsätze',
                  panels: [
                    {
                      component: 'ChartPanelComponent',
                      widthPercentage: 100,
                      data: {
                        sourceUrl: '/revenues/statistics'
                      }
                    }
                  ]
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                component: PanelLayoutComponent,
                path: 'products',
                data: {
                  name: 'Produkte',
                  panels: [
                    {
                      component: 'ChartPanelComponent',
                      widthPercentage: 100,
                      lgWidthPercentage: 50,
                      data: {
                        sourceUrl: '/products/statistics/mostsold'
                      }
                    },
                    {
                      component: 'ChartPanelComponent',
                      widthPercentage: 100,
                      lgWidthPercentage: 50,
                      data: {
                        sourceUrl: '/products/statistics/mostrevenue'
                      }
                    }
                  ]
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                component: PanelLayoutComponent,
                path: 'activities',
                data: {
                  name: 'Tätigkeiten',
                  panels: [
                    {
                      component: 'ChartPanelComponent',
                      widthPercentage: 100,
                      data: {
                        sourceUrl: '/activities/statistics/mostapplied'
                      }
                    }
                  ]
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                component: PanelLayoutComponent,
                path: 'customers',
                data: {
                  name: 'Kunden',
                  panels: [
                    {
                      component: 'ChartPanelComponent',
                      widthPercentage: 100,
                      lgWidthPercentage: 50,
                      data: {
                        sourceUrl: '/customers/statistics/mostvisited'
                      }
                    },
                    {
                      component: 'ChartPanelComponent',
                      widthPercentage: 100,
                      lgWidthPercentage: 50,
                      data: {
                        sourceUrl: '/customers/statistics/mostrevenue'
                      }
                    }
                  ]
                },
                canDeactivate: [PreventNavigationIfModalIsOpenService]
              },
              {
                path: '',
                redirectTo: 'dashboard',
                pathMatch: 'full'
              },
            ]
          }
        ]
      },
      {
        component: WelcomeComponent,
        path: 'welcome',
        data: {
          name: 'Willkommen',
          hideInNav: true,
        },
        canDeactivate: [PreventNavigationIfModalIsOpenService]
      },
      {
        component: MyProfileComponent,
        path: 'myprofile',
        data: {
          name: 'Mein Profil',
          hideInNav: true,
        },
        canDeactivate: [PreventNavigationIfModalIsOpenService]
      },
      {
        path: '',
        redirectTo: 'welcome',
        pathMatch: 'full'
      },
    ]
  },
  {
    path: 'login',
    component: LoginComponent,
    data: {
      name: 'Login',
      hideInNav: true
    },
    canDeactivate: [PreventNavigationIfModalIsOpenService]
  },
  {
    path: 'logout',
    component: LogoutComponent,
    data: {
      name: 'Logout',
      align: 'bottom'
    },
    canDeactivate: [PreventNavigationIfModalIsOpenService]
  },
];


@NgModule({
  imports: [RouterModule.forRoot(routes, {})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
