import { Component, input } from '@angular/core';
import { DashboardContentRegistry } from '../../../dashboard/dashboard.component';
import { DashboardContentRendererComponent } from '../../../dashboard/content-renderer/dashboard-content-renderer.component';

@Component({
  selector: '[dashboard-grid-container],dashboard-grid-container',
  imports: [DashboardContentRendererComponent],
  templateUrl: './dashboard-grid-container.component.html',
})
export class DashboardGridContainerComponent {
  contentRegistry = input.required<DashboardContentRegistry>();
  content = input.required<KokuDto.DashboardGridContainerDto>();
}
