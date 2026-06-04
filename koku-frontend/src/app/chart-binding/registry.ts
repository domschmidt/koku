import { InputFieldComponent } from '../fields/input/input-field.component';
import { DateInputFieldComponent } from '../fields/input/date-input-field.component';
import { TimeInputFieldComponent } from '../fields/input/time-input-field.component';
import { MonthInputFieldComponent } from '../fields/input/month-input-field.component';
import { WeekInputFieldComponent } from '../fields/input/week-input-field.component';
import { ChartContentSetup } from '../chart/chart.component';
import { ChartFilterRendererComponent } from '../chart/filter-renderer/chart-filter-renderer.component';

const FILTER_REGISTRY: Partial<
  Record<
    KokuDto.AbstractChartFilterDto['@type'] | string,
    {
      componentType: any;
      inputBindings?(instance: any, filterConfig: KokuDto.AbstractChartFilterDto): Record<string, any>;
      outputBindings?(instance: any, filterConfig: KokuDto.AbstractChartFilterDto): Record<string, any>;
    }
  >
> = {
  input: {
    componentType: InputFieldComponent,
    inputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.InputChartFilterDto) => {
      return {
        value: filterDef.value,
        label: filterDef.label,
        placeholder: filterDef.placeholder,
        type: filterDef.type,
      };
    },
    outputBindings: (instance: ChartFilterRendererComponent) => {
      return {
        changed: (data: any) => instance.filterValueChanged.emit(data),
      };
    },
  },
  'date-input': {
    componentType: DateInputFieldComponent,
    inputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.DateInputChartFilterDto) => {
      return {
        value: filterDef.value || '',
        label: filterDef.label,
        placeholder: filterDef.placeholder,
      };
    },
    outputBindings: (instance: ChartFilterRendererComponent) => {
      return {
        changed: (data: any) => instance.filterValueChanged.emit(data),
      };
    },
  },
  'time-input': {
    componentType: TimeInputFieldComponent,
    inputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.TimeInputChartFilterDto) => {
      return {
        value: filterDef.value || '',
        label: filterDef.label,
        placeholder: filterDef.placeholder,
      };
    },
    outputBindings: (instance: ChartFilterRendererComponent) => {
      return {
        changed: (data: any) => instance.filterValueChanged.emit(data),
      };
    },
  },
  'month-input': {
    componentType: MonthInputFieldComponent,
    inputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.MonthInputChartFilterDto) => {
      return {
        value: filterDef.value || '',
        label: filterDef.label,
        placeholder: filterDef.placeholder,
      };
    },
    outputBindings: (instance: ChartFilterRendererComponent) => {
      return {
        changed: (data: any) => instance.filterValueChanged.emit(data),
      };
    },
  },
  'week-input': {
    componentType: WeekInputFieldComponent,
    inputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.WeekInputChartFilterDto) => {
      return {
        value: filterDef.value || '',
        label: filterDef.label,
        placeholder: filterDef.placeholder,
      };
    },
    outputBindings: (instance: ChartFilterRendererComponent) => {
      return {
        changed: (data: any) => instance.filterValueChanged.emit(data),
      };
    },
  },
};

export const CHART_CONTENT_SETUP: ChartContentSetup = {
  filterRegistry: FILTER_REGISTRY,
};
