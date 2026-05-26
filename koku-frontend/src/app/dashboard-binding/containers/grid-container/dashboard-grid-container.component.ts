import { Component, input } from '@angular/core';
import { DashboardContentSetup } from '../../../dashboard/dashboard.component';
import { DashboardPanelRendererComponent } from '../../../dashboard/panel-renderer/dashboard-panel-renderer.component';
import { DashboardContainerRendererComponent } from '../../../dashboard/container-renderer/dashboard-container-renderer.component';
import { KokuDashboardGridContainer } from '../../../../types/generated/dashboard';

@Component({
  selector: '[dashboard-grid-container],dashboard-grid-container',
  imports: [DashboardPanelRendererComponent, DashboardContainerRendererComponent],
  templateUrl: './dashboard-grid-container.component.html',
})
export class DashboardGridContainerComponent {
  contentSetup = input.required<DashboardContentSetup>();
  content = input.required<KokuDashboardGridContainer>();
}
