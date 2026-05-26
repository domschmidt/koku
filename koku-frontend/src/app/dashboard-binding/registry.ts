import { FieldRendererComponent } from '../formular/field-renderer/field-renderer.component';
import { DashboardGridContainerComponent } from './containers/grid-container/dashboard-grid-container.component';
import { DashboardContentSetup } from '../dashboard/dashboard.component';
import { DashboardTextPanelComponent } from './panels/text/dashboard-text-panel.component';
import { DashboardAsyncTextPanelComponent } from './panels/text/dashboard-async-text-panel.component';
import { ChartComponent } from '../chart/chart.component';
import { CHART_CONTENT_SETUP } from '../chart-binding/registry';
import { DashboardAppointmentsPanelComponent } from './panels/appointments/dashboard-appointments-panel.component';
import { KokuDashboardAppointmentsPanel, KokuDashboardContainer } from '../../types/generated/dashboard';
import { KokuDashboardAsyncChartPanel } from '../../types/generated/dashboard';
import { KokuDashboardAsyncTextPanel } from '../../types/generated/dashboard';
import { KokuDashboardGridContainer } from '../../types/generated/dashboard';
import { KokuDashboardPanel } from '../../types/generated/dashboard';
import { KokuDashboardTextPanel } from '../../types/generated/dashboard';

const CONTAINER_REGISTRY: Partial<
  Record<
    KokuDashboardContainer['type'],
    {
      componentType: any;
      inputBindings?(instance: any, content: KokuDashboardContainer): Record<string, any>;
      outputBindings?(instance: any, content: KokuDashboardContainer): Record<string, any>;
    }
  >
> = {
  grid: {
    componentType: DashboardGridContainerComponent,
    inputBindings(instance: any, content: KokuDashboardGridContainer): Record<string, any> {
      return {
        content: content,
        contentSetup: DASHBOARD_CONTENT_SETUP,
      };
    },
  },
};
const PANEL_REGISTRY: Partial<
  Record<
    KokuDashboardPanel['type'],
    {
      componentType: any;
      inputBindings?(instance: FieldRendererComponent, content: KokuDashboardPanel): Record<string, any>;
      outputBindings?(instance: FieldRendererComponent, content: KokuDashboardPanel): Record<string, any>;
    }
  >
> = {
  text: {
    componentType: DashboardTextPanelComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDashboardTextPanel): Record<string, any> {
      return {
        content: content,
      };
    },
  },
  appointments: {
    componentType: DashboardAppointmentsPanelComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDashboardAppointmentsPanel): Record<string, any> {
      return {
        content: content,
      };
    },
  },
  'async-text': {
    componentType: DashboardAsyncTextPanelComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDashboardAsyncTextPanel): Record<string, any> {
      return {
        content: content,
      };
    },
  },
  'async-chart': {
    componentType: ChartComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDashboardAsyncChartPanel): Record<string, any> {
      return {
        chartUrl: content.chartUrl,
        contentSetup: CHART_CONTENT_SETUP,
      };
    },
  },
};

export const DASHBOARD_CONTENT_SETUP: DashboardContentSetup = {
  panelRegistry: PANEL_REGISTRY,
  containerRegistry: CONTAINER_REGISTRY,
};
