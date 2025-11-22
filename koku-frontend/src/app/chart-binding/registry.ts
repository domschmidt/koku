import {InputFieldComponent} from '../fields/input/input-field.component';
import {ChartContentSetup} from '../chart/chart.component';
import {ChartFilterRendererComponent} from '../chart/filter-renderer/chart-filter-renderer.component';

const FILTER_REGISTRY: Partial<Record<KokuDto.AbstractChartFilterDto["@type"] | string, {
  componentType: any;
  inputBindings?(instance: any, filterConfig: KokuDto.AbstractChartFilterDto): { [key: string]: any }
  outputBindings?(instance: any, filterConfig: KokuDto.AbstractChartFilterDto): { [key: string]: any }
}>> = {
  "input": {
    componentType: InputFieldComponent,
    inputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.InputChartFilterDto) => {
      return {
        'value': filterDef.value,
        'label': filterDef.label,
        'placeholder': filterDef.placeholder,
        'type': filterDef.type,
      }
    },
    outputBindings: (instance: ChartFilterRendererComponent, filterDef: KokuDto.InputChartFilterDto) => {
      return {
        onInput: (data: any) => instance.filterValueChanged.emit(data),
      }
    }
  }
};

export const CHART_CONTENT_SETUP: ChartContentSetup = {
  filterRegistry: FILTER_REGISTRY,
}
