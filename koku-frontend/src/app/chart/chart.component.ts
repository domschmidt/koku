import {ChangeDetectionStrategy, Component, ElementRef, inject, input, signal, viewChild} from "@angular/core";
import {toObservable} from '@angular/core/rxjs-interop';
import ApexCharts from 'apexcharts'

import {Observable, Subscription} from 'rxjs';
import {tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {ToastService} from '../toast/toast.service';
import {ChartFilterRendererComponent} from './filter-renderer/chart-filter-renderer.component';
import {delayAtLeast} from '../rxjs/delay-at-least';

export interface ChartContentSetup {
  filterRegistry: Partial<Record<KokuDto.AbstractChartFilterDto["@type"] | string, {
    componentType: any;
    inputBindings?(instance: any, filterConfig: KokuDto.AbstractChartFilterDto): { [key: string]: any }
    outputBindings?(instance: any, filterConfig: KokuDto.AbstractChartFilterDto): { [key: string]: any }
  }>>;
}

@Component({
  selector: 'koku-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css'],
  imports: [
    ChartFilterRendererComponent
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChartComponent {

  httpClient = inject(HttpClient);
  toastService = inject(ToastService);
  chartRoot = viewChild<ElementRef<HTMLDivElement>>("chartRoot");

  chartUrl = input.required<string>();
  contentSetup = input.required<ChartContentSetup>();

  loading = signal(false);
  chartData = signal<KokuDto.AbstractChartDto | null>(null);

  activeFilters = signal<{ [key: string]: string | number | boolean }>({});

  private lastChartSubscription: Subscription | undefined;
  private currentChartInstance: ApexCharts | undefined;

  constructor() {
    toObservable(this.chartUrl).subscribe(() => {
      this.activeFilters.set({});
      this.loadChart().subscribe();
    })
  }

  private loadChart() {
    return new Observable(subscriber => {
        const chartUrlSnapshot = this.chartUrl();
        if (chartUrlSnapshot) {
          if (this.lastChartSubscription && !this.lastChartSubscription.closed) {
            this.lastChartSubscription.unsubscribe();
          }
          this.lastChartSubscription = this.httpClient.get<KokuDto.AbstractChartDto>(chartUrlSnapshot, {
            params: this.activeFilters()
          })
            .pipe(
              tap(value => {
                this.loading.set(true);
              }),
              delayAtLeast(700)
            ).subscribe({
              next: (chartData) => {
                if (this.currentChartInstance) {
                  this.currentChartInstance.destroy();
                }
                const chart = new ApexCharts(this.chartRoot()?.nativeElement, {
                  grid: {
                    borderColor: 'var(--color-neutral)'
                  },
                  series: ((chartData) => {
                    let result: ApexAxisChartSeries | ApexNonAxisChartSeries | null = null;
                    switch (chartData["@type"]) {
                      case 'bar': {
                        const castedChartData = chartData as KokuDto.BarChartDto;
                        result = (castedChartData.series || []).map(s => ({
                          name: s.name,
                          data: s.data || [],
                          group: s.group
                        }));
                        break;
                      }
                      case 'line': {
                        const castedChartData = chartData as KokuDto.LineChartDto;
                        result = (castedChartData.series || []).map(s => ({
                          name: s.name,
                          data: s.data || [],
                          group: s.group
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
                  colors: [
                    'var(--color-primary)',
                    'var(--color-secondary)',
                    'var(--color-accent)'
                  ],
                  plotOptions: {
                    bar: {
                      dataLabels: {
                        total: {
                          enabled: ((chartData) => {
                            switch (chartData["@type"]) {
                              case 'bar': {
                                const castedChartData = chartData as KokuDto.BarChartDto;
                                return castedChartData.showTotals;
                              }
                            }
                            return false;
                          })(chartData),
                          offsetY: -10,
                          style: {
                            color: 'var(--color-base-content)',
                          }
                        }
                      }
                    }
                  },
                  tooltip: {
                    shared: true,
                    intersect: false,
                  },
                  title: {
                    text: chartData.title,
                    align: 'left'
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
                      let result: ApexChart | null = null;
                      switch (chartData["@type"]) {
                        case 'bar': {
                          const castedChart = chartData as KokuDto.BarChartDto;
                          result = {
                            type: 'bar',
                            stacked: castedChart.stacked
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
                            type: 'pie'
                          };
                          break;
                        }
                      }
                      return result;
                    })(chartData)
                  },
                  yaxis: ((chartData) => {
                    let result: ApexYAxis[] = [{
                      labels: {
                        style: {
                          colors: 'var(--color-base-content)',
                        }
                      }
                    }]
                    switch (chartData["@type"]) {
                      case 'bar': {
                        const castedChart = chartData as KokuDto.BarChartDto;
                        const axes = castedChart.axes;
                        if (axes && axes.y) {
                          const tempResult: ApexYAxis[] = []
                          for (const currentAxis of axes.y) {
                            tempResult.push({
                              opposite: currentAxis.opposite,
                              seriesName: currentAxis.seriesName,
                              title: {
                                text: currentAxis.text
                              },
                              labels: {
                                style: {
                                  colors: 'var(--color-base-content)',
                                }
                              }
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
                          const tempResult: ApexYAxis[] = []
                          for (const currentAxis of axes.y) {
                            tempResult.push({
                              opposite: currentAxis.opposite,
                              seriesName: currentAxis.seriesName,
                              title: {
                                text: currentAxis.text
                              },
                              labels: {
                                style: {
                                  colors: 'var(--color-base-content)',
                                }
                              }
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
                      }
                    },
                    ...((chartData) => {
                      let result: ApexXAxis = {categories: []};
                      switch (chartData["@type"]) {
                        case 'bar': {
                          const castedChartData = chartData as KokuDto.BarChartDto;
                          result = {categories: castedChartData.axes?.x && castedChartData.axes.x['@type'] === 'categorical' ? castedChartData.axes.x.categories : []};
                          break;
                        }
                        case 'line': {
                          const castedChartData = chartData as KokuDto.LineChartDto;
                          result = {categories: castedChartData.axes?.x && castedChartData.axes.x['@type'] === 'categorical' ? castedChartData.axes.x.categories : []};
                          break;
                        }
                      }
                      return result;
                    })(chartData)
                  },
                  labels: [],
                  dataLabels: {
                    enabled: true,
                    style: {
                      fontSize: '14px',
                      fontFamily: 'inherit',
                      colors: [
                        'var(--color-base-content)'
                      ]
                    },
                  },
                  legend: {
                    show: true,
                    fontSize: '14px',
                    fontFamily: 'inherit',
                  },
                  annotations: ((annotations) => {
                    let result: ApexAnnotations | undefined = undefined;
                    if (annotations) {
                      result = {
                        xaxis: ((xaxis) => {
                          const result: XAxisAnnotations[] = [];
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
                                      background: this.getColor(label?.borderColor)
                                    },
                                    text: label?.text,
                                    position: 'bottom',
                                    offsetY: -10,
                                    offsetX: 3
                                  }
                                })(currentAxis.label)
                              })
                            }
                          }

                          return result;
                        })(annotations.xasis)
                      }
                    }
                    return result;
                  })(chartData.annotations)
                });
                chart.render();
                subscriber.next(chartData);
                subscriber.complete();
                this.loading.set(false);
                this.chartData.set(chartData);
                this.currentChartInstance = chart;
              },
              error: (err) => {
                this.toastService.add("Fehler beim Laden der Daten! Versuchs später erneut!", 'error');
                subscriber.error(err);
                this.loading.set(false);
                this.chartData.set(null);
              }
            });
        } else {
          subscriber.error('missing charturl');
          this.loading.set(false);
          this.chartData.set(null);
        }
      }
    );
  }

  private BORDER_COLORS: Record<Partial<KokuDto.ColorsEnumDto>, string> = {
    "PRIMARY": 'var(--color-primary-600)',
    "SECONDARY": 'var(--color-secondary-600)',
    "ACCENT": 'var(--color-accent-600)',
    "INFO": 'var(--color-info-600)',
    "SUCCESS": 'var(--color-success-600)',
    "WARNING": 'var(--color-warning-600)',
    "ERROR": 'var(--color-error-600)',
    "RED": 'var(--color-red-600)',
    "ORANGE": 'var(--color-orange-600)',
    "AMBER": 'var(--color-amber-600)',
    "YELLOW": 'var(--color-yellow-600)',
    "LIME": 'var(--color-lime-600)',
    "GREEN": 'var(--color-green-600)',
    "EMERALD": 'var(--color-emerald-600)',
    "TEAL": 'var(--color-teal-600)',
    "CYAN": 'var(--color-cyan-600)',
    "SKY": 'var(--color-sky-600)',
    "BLUE": 'var(--color-blue-600)',
    "INDIGO": 'var(--color-indigo-600)',
    "VIOLET": 'var(--color-violet-600)',
    "PURPLE": 'var(--color-purple-600)',
    "FUCHSIA": 'var(--color-fuchsia-600)',
    "PINK": 'var(--color-pink-600)',
    "ROSE": 'var(--color-rose-600)',
    "SLATE": 'var(--color-slate-600)',
    "GRAY": 'var(--color-gray-600)',
    "ZINC": 'var(--color-zinc-600)',
    "NEUTRAL": 'var(--color-neutral-600)',
    "STONE": 'var(--color-stone-600)',
  }

  private getColor(borderColor: KokuDto.ColorsEnumDto | undefined) {
    let result = '';

    if (borderColor !== undefined) {
      result = this.BORDER_COLORS[borderColor];
    }

    return result;
  }

  onFilterValueChanged(filterConfig: KokuDto.AbstractChartFilterDto, value: string | number | boolean) {
    if (filterConfig.queryParamName) {
      const activeFiltersSnapshot = this.activeFilters();
      this.activeFilters.set({
        ...activeFiltersSnapshot,
        [filterConfig.queryParamName]: value
      });
      this.loadChart().subscribe();
    }
  }

}
