import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  OnDestroy,
  Signal,
  signal,
  viewChild,
} from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import ApexCharts from 'apexcharts';

import { finalize, Subscription } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../toast/toast.service';
import { ChartFilterRendererComponent } from './filter-renderer/chart-filter-renderer.component';
import { DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';
import { colorValue } from '../utils/color.utils';

export interface ChartFilterRenderContext {
  content: Signal<KokuDto.AbstractChartFilterDto>;
  loading: Signal<boolean>;
  emit(value: string | number | boolean): void;
}

export type ChartFilterRecipeFactory = (context: ChartFilterRenderContext) => DynamicRenderRecipe;

export type ChartFilterRegistry = Partial<Record<string, ChartFilterRecipeFactory>>;

@Component({
  selector: 'koku-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css'],
  imports: [ChartFilterRendererComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChartComponent implements OnDestroy {
  readonly httpClient = inject(HttpClient);
  readonly toastService = inject(ToastService);
  readonly chartRoot = viewChild<ElementRef<HTMLDivElement>>('chartRoot');

  chartUrl = input.required<string>();
  filterRegistry = input.required<ChartFilterRegistry>();

  loading = signal(false);
  chartData = signal<KokuDto.AbstractChartDto | null>(null);

  activeFilters = signal<Record<string, string | number | boolean>>({});

  private lastChartSubscription: Subscription | undefined;
  private currentChartInstance: ApexCharts | undefined;

  constructor() {
    toObservable(this.chartUrl).subscribe(() => {
      this.activeFilters.set({});
      this.loadChart();
    });
  }

  private loadChart() {
    const chartUrlSnapshot = this.chartUrl();
    if (!chartUrlSnapshot) {
      this.loading.set(false);
      this.chartData.set(null);
      return;
    }
    this.loading.set(true);
    this.lastChartSubscription?.unsubscribe();
    this.lastChartSubscription = this.httpClient
      .get<KokuDto.AbstractChartDto>(chartUrlSnapshot, {
        params: this.activeFilters(),
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (chartData) => {
          this.currentChartInstance?.destroy();
          const chartRootEl = this.chartRoot()?.nativeElement;
          if (!chartRootEl) {
            this.chartData.set(null);
            this.toastService.add('Chart konnte nicht initialisiert werden', 'error');
            return;
          }
          const chart = new ApexCharts(chartRootEl, {
            grid: {
              borderColor: 'var(--color-neutral)',
            },
            series: ((chartData) => {
              let result: ApexCharts.ApexOptions['series'] | null = null;
              switch (chartData['@type']) {
                case 'bar': {
                  const castedChartData = chartData as KokuDto.BarChartDto;
                  result = (castedChartData.series || []).map((s) => ({
                    name: s.name,
                    data: s.data || [],
                    group: s.group,
                  }));
                  break;
                }
                case 'line': {
                  const castedChartData = chartData as KokuDto.LineChartDto;
                  result = (castedChartData.series || []).map((s) => ({
                    name: s.name,
                    data: s.data || [],
                    group: s.group,
                  }));
                  break;
                }
                case 'pie': {
                  const castedChartData = chartData as KokuDto.PieChartDto;
                  result = castedChartData.series || [];
                  break;
                }
              }
              return result;
            })(chartData),
            colors: ['var(--color-primary)', 'var(--color-secondary)', 'var(--color-accent)'],
            plotOptions: {
              bar: {
                dataLabels: {
                  total: {
                    enabled: chartData['@type'] === 'bar' ? (chartData as KokuDto.BarChartDto).showTotals : false,
                    offsetY: -10,
                    style: {
                      color: 'var(--color-base-content)',
                    },
                  },
                },
              },
            },
            tooltip: {
              shared: true,
              intersect: false,
            },
            title: {
              text: chartData.title,
              align: 'left',
            },
            chart: {
              height: 500,
              foreColor: 'var(--color-base-content)',
              background: 'var(--color-base-100)',
              fontFamily: 'inherit',
              zoom: {
                enabled: false,
              },
              toolbar: {
                show: false,
              },
              ...((chartData) => {
                let result: ApexCharts.ApexOptions['chart'] | null = null;
                switch (chartData['@type']) {
                  case 'bar': {
                    const castedChart = chartData as KokuDto.BarChartDto;
                    result = {
                      type: 'bar',
                      stacked: castedChart.stacked,
                    };
                    break;
                  }
                  case 'line': {
                    result = {
                      type: 'line',
                    };
                    break;
                  }
                  case 'pie': {
                    result = {
                      type: 'pie',
                    };
                    break;
                  }
                }
                return result;
              })(chartData),
            },
            yaxis: ((chartData) => {
              let result: ApexCharts.ApexYAxis[] = [
                {
                  labels: {
                    style: {
                      colors: 'var(--color-base-content)',
                    },
                  },
                },
              ];
              switch (chartData['@type']) {
                case 'bar': {
                  const castedChart = chartData as KokuDto.BarChartDto;
                  const axes = castedChart.axes;
                  if (axes && axes.y) {
                    const tempResult: ApexCharts.ApexYAxis[] = [];
                    for (const currentAxis of axes.y) {
                      tempResult.push({
                        opposite: currentAxis.opposite,
                        seriesName: currentAxis.seriesName,
                        title: {
                          text: currentAxis.text,
                        },
                        labels: {
                          style: {
                            colors: 'var(--color-base-content)',
                          },
                        },
                      });
                    }
                    result = tempResult;
                  }
                  break;
                }
                case 'line': {
                  const castedChart = chartData as KokuDto.LineChartDto;
                  const axes = castedChart.axes;
                  if (axes && axes.y) {
                    const tempResult: ApexCharts.ApexYAxis[] = [];
                    for (const currentAxis of axes.y) {
                      tempResult.push({
                        opposite: currentAxis.opposite,
                        seriesName: currentAxis.seriesName,
                        title: {
                          text: currentAxis.text,
                        },
                        labels: {
                          style: {
                            colors: 'var(--color-base-content)',
                          },
                        },
                      });
                    }
                    result = tempResult;
                  }
                  break;
                }
              }
              return result;
            })(chartData),
            xaxis: {
              labels: {
                style: {
                  colors: 'var(--color-base-content)',
                },
              },
              ...((chartData) => {
                let result: ApexCharts.ApexOptions['xaxis'] = { categories: [] };
                switch (chartData['@type']) {
                  case 'bar': {
                    const castedChartData = chartData as KokuDto.BarChartDto;
                    result = {
                      categories:
                        castedChartData.axes?.x && castedChartData.axes.x['@type'] === 'categorical'
                          ? castedChartData.axes.x.categories
                          : [],
                    };
                    break;
                  }
                  case 'line': {
                    const castedChartData = chartData as KokuDto.LineChartDto;
                    result = {
                      categories:
                        castedChartData.axes?.x && castedChartData.axes.x['@type'] === 'categorical'
                          ? castedChartData.axes.x.categories
                          : [],
                    };
                    break;
                  }
                }
                return result;
              })(chartData),
            },
            labels: [],
            dataLabels: {
              enabled: true,
              style: {
                fontSize: '14px',
                fontFamily: 'inherit',
                colors: ['var(--color-base-content)'],
              },
            },
            legend: {
              show: true,
              fontSize: '14px',
              fontFamily: 'inherit',
            },
            annotations: ((annotations) => {
              let result: ApexCharts.ApexOptions['annotations'] | undefined = undefined;
              if (annotations) {
                result = {
                  xaxis: ((xaxis) => {
                    const result: NonNullable<ApexCharts.ApexOptions['annotations']>['xaxis'] = [];
                    if (xaxis) {
                      for (const currentAxis of xaxis) {
                        result.push({
                          x: currentAxis.x,
                          borderColor: this.getColor(currentAxis?.borderColor),
                          label: ((label) => {
                            return {
                              borderColor: this.getColor(label?.borderColor),
                              style: {
                                color: '',
                                background: this.getColor(label?.borderColor),
                              },
                              text: label?.text,
                              position: 'bottom',
                              offsetY: -10,
                              offsetX: 3,
                            };
                          })(currentAxis.label),
                        });
                      }
                    }

                    return result;
                  })(annotations.xasis),
                };
              }
              return result;
            })(chartData.annotations),
          });
          void chart.render();
          this.chartData.set(chartData);
          this.currentChartInstance = chart;
        },
        error: () => {
          this.toastService.add('Fehler beim Laden der Daten! Versuchs später erneut!', 'error');
          this.chartData.set(null);
        },
      });
  }

  ngOnDestroy(): void {
    this.lastChartSubscription?.unsubscribe();
    this.currentChartInstance?.destroy();
    this.currentChartInstance = undefined;
  }

  private getColor(borderColor: KokuDto.ColorsEnumDto | undefined) {
    return colorValue(borderColor);
  }

  onFilterValueChanged(filterConfig: KokuDto.AbstractChartFilterDto, value: string | number | boolean) {
    if (filterConfig.queryParamName) {
      const activeFiltersSnapshot = this.activeFilters();
      this.activeFilters.set({
        ...activeFiltersSnapshot,
        [filterConfig.queryParamName]: value,
      });
      this.loadChart();
    }
  }
}
