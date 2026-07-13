import { ViewContainerRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { AvatarComponent } from './avatar/avatar.component';
import { DashboardGridContainerComponent } from './dashboard-binding/containers/grid-container/dashboard-grid-container.component';
import { DockComponent } from './dock/dock.component';
import { ListItemComponent } from './list/list-item/list-item.component';
import { NaviTabbarComponent } from './navi-tabbar/navi-tabbar.component';
import { OutletDirective } from './portal/outlet.directive';
import { TextCircleComponent } from './text-circle/text-circle.component';
import { ToastComponent } from './toast/toast.component';
import { ToastService } from './toast/toast.service';
import { ModalContentRendererComponent } from './modal/modal-content-renderer.component';
import { ListInlineContentComponent } from './list/list-inline-content/list-inline-content.component';
import { ListActionRendererComponent } from './list/list-item-action/list-action-renderer.component';
import { ListItemPreviewComponent } from './list/list-item-preview/list-item-preview.component';
import { CalendarInlineContentComponent } from './calendar-binding/calendar-inline-content/calendar-inline-content.component';
import { ChartFilterRendererComponent } from './chart/filter-renderer/chart-filter-renderer.component';
import { BusinessRulesContentComponent } from './business-rules-binding/business-rules-content/business-rules-content.component';
import { DashboardContentRendererComponent } from './dashboard/content-renderer/dashboard-content-renderer.component';
import { MultiSelectWithPricesFieldComponent } from './fields/multi-select-with-prices/multi-select-with-prices-field.component';
import { CalendarActionRendererComponent } from './calendar/action-renderer/calendar-action-renderer.component';
import { ConditionActionComponent } from './list-binding/actions/condition-action/condition-action.component';

describe('declarative component initializers', () => {
  it('creates input and output signals for presentation components', () => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { queryParams: of({}), snapshot: { queryParams: {} } },
        },
      ],
    });
    const instances = TestBed.runInInjectionContext(() => [
      new AvatarComponent(),
      new TextCircleComponent(),
      new DashboardGridContainerComponent(),
      new DockComponent(),
      new ListItemComponent(),
      new ModalContentRendererComponent(),
      new ListInlineContentComponent(),
      new ListActionRendererComponent(),
      new ListItemPreviewComponent(),
      new CalendarInlineContentComponent(),
      new ChartFilterRendererComponent(),
      new BusinessRulesContentComponent(),
      new DashboardContentRendererComponent(),
      new MultiSelectWithPricesFieldComponent(),
      new CalendarActionRendererComponent(),
      new ConditionActionComponent(),
    ]);

    expect(instances).toHaveLength(16);
  });

  it('resolves the dependencies of DI-only declarations', () => {
    const route = {} as ActivatedRoute;
    const viewContainerRef = {} as ViewContainerRef;
    const toastService = new ToastService();
    TestBed.configureTestingModule({
      providers: [
        { provide: ActivatedRoute, useValue: route },
        { provide: ViewContainerRef, useValue: viewContainerRef },
        { provide: ToastService, useValue: toastService },
      ],
    });

    const instances = TestBed.runInInjectionContext(
      () => [new NaviTabbarComponent(), new OutletDirective(), new ToastComponent()] as const,
    );

    expect(instances[0].activatedRoute).toBe(route);
    expect(instances[1].viewContainerRef).toBe(viewContainerRef);
    expect(instances[2].toastService).toBe(toastService);
  });
});
