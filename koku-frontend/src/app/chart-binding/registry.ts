import { computed } from '@angular/core';
import { ChartFilterRegistry, ChartFilterRenderContext } from '../chart/chart.component';
type FilterRegistryItem = NonNullable<ChartFilterRegistry[string]>;
const changedOutput = (context: ChartFilterRenderContext) => ({
  changed: (data: string | number | boolean) => context.emit(data),
});
const FILTER_REGISTRY: Partial<Record<string, FilterRegistryItem>> = {
  input: (context: ChartFilterRenderContext) => {
    const filter = computed(() => context.content() as KokuDto.InputChartFilterDto);
    return {
      loadComponent: () => import('../fields/input/input-field.component').then((module) => module.InputFieldComponent),
      inputs: computed(() => ({
        loading: context.loading(),
        value: filter().value || '',
        label: filter().label,
        placeholder: filter().placeholder,
        type: filter().type,
      })),
      outputs: changedOutput(context),
    };
  },
  'date-input': (context: ChartFilterRenderContext) => {
    const filter = computed(() => context.content() as KokuDto.DateInputChartFilterDto);
    return {
      loadComponent: () =>
        import('../fields/input/date-input-field.component').then((module) => module.DateInputFieldComponent),
      inputs: computed(() => ({
        loading: context.loading(),
        value: filter().value || '',
        label: filter().label,
        placeholder: filter().placeholder,
      })),
      outputs: changedOutput(context),
    };
  },
  'time-input': (context: ChartFilterRenderContext) => {
    const filter = computed(() => context.content() as KokuDto.TimeInputChartFilterDto);
    return {
      loadComponent: () =>
        import('../fields/input/time-input-field.component').then((module) => module.TimeInputFieldComponent),
      inputs: computed(() => ({
        loading: context.loading(),
        value: filter().value || '',
        label: filter().label,
        placeholder: filter().placeholder,
      })),
      outputs: changedOutput(context),
    };
  },
  'month-input': (context: ChartFilterRenderContext) => {
    const filter = computed(() => context.content() as KokuDto.MonthInputChartFilterDto);
    return {
      loadComponent: () =>
        import('../fields/input/month-input-field.component').then((module) => module.MonthInputFieldComponent),
      inputs: computed(() => ({
        loading: context.loading(),
        value: filter().value || '',
        label: filter().label,
        placeholder: filter().placeholder,
      })),
      outputs: changedOutput(context),
    };
  },
  'week-input': (context: ChartFilterRenderContext) => {
    const filter = computed(() => context.content() as KokuDto.WeekInputChartFilterDto);
    return {
      loadComponent: () =>
        import('../fields/input/week-input-field.component').then((module) => module.WeekInputFieldComponent),
      inputs: computed(() => ({
        loading: context.loading(),
        value: filter().value || '',
        label: filter().label,
        placeholder: filter().placeholder,
      })),
      outputs: changedOutput(context),
    };
  },
};
export const CHART_FILTER_REGISTRY: ChartFilterRegistry = FILTER_REGISTRY;
