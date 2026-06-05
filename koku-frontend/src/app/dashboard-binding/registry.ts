import { computed } from '@angular/core';
import { DashboardContentRegistry, DashboardContentRenderContext } from '../dashboard/dashboard.component';
import { CHART_FILTER_REGISTRY } from '../chart-binding/registry';
type ContentRegistryItem = NonNullable<DashboardContentRegistry[string]>;
const CONTENT_REGISTRY: Partial<Record<string, ContentRegistryItem>> = {
  grid: (context: DashboardContentRenderContext<KokuDto.DashboardGridContainerDto>) => ({
    loadComponent: () =>
      import('./containers/grid-container/dashboard-grid-container.component').then(
        (module) => module.DashboardGridContainerComponent,
      ),
    inputs: computed(() => ({
      content: context.content(),
      contentRegistry: context.contentRegistry(),
    })),
  }),
  text: (context: DashboardContentRenderContext<KokuDto.DashboardTextPanelDto>) => ({
    loadComponent: () =>
      import('./panels/text/dashboard-text-panel.component').then((module) => module.DashboardTextPanelComponent),
    inputs: computed(() => ({
      content: context.content(),
    })),
  }),
  appointments: (context: DashboardContentRenderContext<KokuDto.DashboardAppointmentsPanelDto>) => ({
    loadComponent: () =>
      import('./panels/appointments/dashboard-appointments-panel.component').then(
        (module) => module.DashboardAppointmentsPanelComponent,
      ),
    inputs: computed(() => ({
      content: context.content(),
    })),
  }),
  'async-text': (context: DashboardContentRenderContext<KokuDto.DashboardAsyncTextPanelDto>) => ({
    loadComponent: () =>
      import('./panels/text/dashboard-async-text-panel.component').then(
        (module) => module.DashboardAsyncTextPanelComponent,
      ),
    inputs: computed(() => ({
      content: context.content(),
    })),
  }),
  'async-chart': (context: DashboardContentRenderContext<KokuDto.DashboardAsyncChartPanelDto>) => {
    const content = computed(() => context.content() as KokuDto.DashboardAsyncChartPanelDto);
    return {
      loadComponent: () => import('../chart/chart.component').then((module) => module.ChartComponent),
      inputs: computed(() => ({
        chartUrl: content().chartUrl,
        filterRegistry: CHART_FILTER_REGISTRY,
      })),
    };
  },
};
export const DASHBOARD_CONTENT_REGISTRY: DashboardContentRegistry = CONTENT_REGISTRY;
