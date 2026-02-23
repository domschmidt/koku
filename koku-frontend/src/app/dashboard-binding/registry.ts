import { FieldRendererComponent } from '../formular/field-renderer/field-renderer.component';
import { DashboardGridContainerComponent } from './containers/grid-container/dashboard-grid-container.component';
import { DashboardContentSetup } from '../dashboard/dashboard.component';
import { DashboardTextPanelComponent } from './panels/text/dashboard-text-panel.component';
import { DashboardAsyncTextPanelComponent } from './panels/text/dashboard-async-text-panel.component';
import { ChartComponent } from '../chart/chart.component';
import { CHART_CONTENT_SETUP } from '../chart-binding/registry';
import { DashboardAppointmentsPanelComponent } from './panels/appointments/dashboard-appointments-panel.component';

const CONTAINER_REGISTRY: Partial<
  Record<
    KokuDto.AbstractDashboardContainer['@type'],
    {
      componentType: any;
      inputBindings?(instance: any, content: KokuDto.AbstractDashboardContainer): Record<string, any>;
      outputBindings?(instance: any, content: KokuDto.AbstractDashboardContainer): Record<string, any>;
    }
  >
> = {
  grid: {
    componentType: DashboardGridContainerComponent,
    inputBindings(instance: any, content: KokuDto.DashboardGridContainerDto): Record<string, any> {
      return {
        content: content,
        contentSetup: DASHBOARD_CONTENT_SETUP,
      };
    },
  },
};
const PANEL_REGISTRY: Partial<
  Record<
    KokuDto.AbstractDashboardPanel['@type'],
    {
      componentType: any;
      inputBindings?(instance: FieldRendererComponent, content: KokuDto.AbstractDashboardPanel): Record<string, any>;
      outputBindings?(instance: FieldRendererComponent, content: KokuDto.AbstractDashboardPanel): Record<string, any>;
    }
  >
> = {
  text: {
    componentType: DashboardTextPanelComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDto.DashboardTextPanelDto): Record<string, any> {
      return {
        content: content,
      };
    },
  },
  appointments: {
    componentType: DashboardAppointmentsPanelComponent,
    inputBindings(
      instance: FieldRendererComponent,
      content: KokuDto.DashboardAppointmentsPanelDto,
    ): Record<string, any> {
      return {
        content: content,
      };
    },
  },
  'async-text': {
    componentType: DashboardAsyncTextPanelComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDto.DashboardAsyncTextPanelDto): Record<string, any> {
      return {
        content: content,
      };
    },
  },
  'async-chart': {
    componentType: ChartComponent,
    inputBindings(instance: FieldRendererComponent, content: KokuDto.DashboardAsyncChartPanelDto): Record<string, any> {
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
