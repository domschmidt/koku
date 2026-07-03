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
          this.renderChart(chartData);
        },
        error: () => {
          this.toastService.add('Fehler beim Laden der Daten! Versuchs spÃƒÂ¤ter erneut!', 'error');
          this.chartData.set(null);
        },
      });
  }

  private renderChart(chartData: KokuDto.AbstractChartDto): void {
    this.currentChartInstance?.destroy();
    const chartRootEl = this.chartRoot()?.nativeElement;
    if (!chartRootEl) {
      this.chartData.set(null);
      this.toastService.add('Chart konnte nicht initialisiert werden', 'error');
      return;
    }

    const chart = new ApexCharts(chartRootEl, this.createChartOptions(chartData));
    void chart.render();
    this.chartData.set(chartData);
    this.currentChartInstance = chart;
  }

  private createChartOptions(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexOptions {
    return {
      grid: {
        borderColor: 'var(--color-neutral)',
      },
      series: this.createSeries(chartData),
      colors: ['var(--color-primary)', 'var(--color-secondary)', 'var(--color-accent)'],
      plotOptions: this.createPlotOptions(chartData),
      tooltip: {
        shared: true,
        intersect: false,
      },
      title: {
        text: chartData.title,
        align: 'left',
      },
      chart: this.createChartSettings(chartData),
      yaxis: this.createYAxis(chartData),
      xaxis: this.createXAxis(chartData),
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
      annotations: this.createAnnotations(chartData.annotations),
    };
  }

  private createSeries(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexOptions['series'] {
    if (chartData['@type'] === 'bar') {
      return this.createCartesianSeries((chartData as KokuDto.BarChartDto).series);
    }
    if (chartData['@type'] === 'line') {
      return this.createCartesianSeries((chartData as KokuDto.LineChartDto).series);
    }
    if (chartData['@type'] === 'pie') {
      return (chartData as KokuDto.PieChartDto).series || [];
    }
    return undefined;
  }

  private createCartesianSeries(series: any[] | undefined): ApexCharts.ApexOptions['series'] {
    return (series || []).map((s) => ({
      name: s.name,
      data: s.data || [],
      group: s.group,
    }));
  }

  private createPlotOptions(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexOptions['plotOptions'] {
    return {
      bar: {
        dataLabels: {
          total: {
            enabled: chartData['@type'] === 'bar' && Boolean((chartData as KokuDto.BarChartDto).showTotals),
            offsetY: -10,
            style: {
              color: 'var(--color-base-content)',
            },
          },
        },
      },
    };
  }

  private createChartSettings(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexOptions['chart'] {
    return {
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
      ...this.createChartTypeSettings(chartData),
    };
  }

  private createChartTypeSettings(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexOptions['chart'] {
    if (chartData['@type'] === 'bar') {
      return {
        type: 'bar',
        stacked: (chartData as KokuDto.BarChartDto).stacked,
      };
    }
    if (chartData['@type'] === 'line') {
      return { type: 'line' };
    }
    if (chartData['@type'] === 'pie') {
      return { type: 'pie' };
    }
    return {};
  }

  private createYAxis(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexYAxis[] {
    if (chartData['@type'] === 'bar') {
      return this.createYAxisFromAxes((chartData as KokuDto.BarChartDto).axes);
    }
    if (chartData['@type'] === 'line') {
      return this.createYAxisFromAxes((chartData as KokuDto.LineChartDto).axes);
    }
    return [this.createDefaultYAxis()];
  }

  private createYAxisFromAxes(axes: any): ApexCharts.ApexYAxis[] {
    if (!axes?.y) {
      return [this.createDefaultYAxis()];
    }

    return axes.y.map((currentAxis: any) => ({
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
    }));
  }

  private createDefaultYAxis(): ApexCharts.ApexYAxis {
    return {
      labels: {
        style: {
          colors: 'var(--color-base-content)',
        },
      },
    };
  }

  private createXAxis(chartData: KokuDto.AbstractChartDto): ApexCharts.ApexOptions['xaxis'] {
    return {
      labels: {
        style: {
          colors: 'var(--color-base-content)',
        },
      },
      categories: this.createXAxisCategories(chartData),
    };
  }

  private createXAxisCategories(chartData: KokuDto.AbstractChartDto): any[] {
    if (chartData['@type'] === 'bar') {
      return this.extractCategoricalXAxis((chartData as KokuDto.BarChartDto).axes);
    }
    if (chartData['@type'] === 'line') {
      return this.extractCategoricalXAxis((chartData as KokuDto.LineChartDto).axes);
    }
    return [];
  }

  private extractCategoricalXAxis(axes: any): any[] {
    return axes?.x?.['@type'] === 'categorical' ? axes.x.categories || [] : [];
  }

  ngOnDestroy(): void {
    this.lastChartSubscription?.unsubscribe();
    this.currentChartInstance?.destroy();
    this.currentChartInstance = undefined;
  }

  private getColor(borderColor: KokuDto.ColorsEnumDto | undefined) {
    return colorValue(borderColor);
  }

  private createAnnotations(
    annotations: KokuDto.AnnotationsDto | undefined,
  ): ApexCharts.ApexOptions['annotations'] | undefined {
    if (!annotations) {
      return undefined;
    }

    return {
      xaxis: this.createXAxisAnnotations(annotations.xasis),
    };
  }

  private createXAxisAnnotations(
    xaxis: KokuDto.AnnotationsAxesDto[] | undefined,
  ): NonNullable<ApexCharts.ApexOptions['annotations']>['xaxis'] {
    return (xaxis || []).map((currentAxis) => ({
      x: currentAxis.x,
      borderColor: this.getColor(currentAxis.borderColor),
      label: this.createAnnotationLabel(currentAxis.label),
    }));
  }

  private createAnnotationLabel(label: KokuDto.AnnotationsAxesLabelDto | undefined) {
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
