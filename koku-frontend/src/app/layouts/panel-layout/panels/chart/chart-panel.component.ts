import {Component, ElementRef, Input, ViewChild} from '@angular/core';
import {Chart, ChartConfiguration, ChartDataset} from "chart.js";
import {ChartService} from "./chart.service";

@Component({
  selector: 'chart-panel',
  templateUrl: './chart-panel.component.html',
  styleUrls: ['./chart-panel.component.scss']
})
export class ChartPanelComponent {
  @Input() sourceUrl: string = '';
  @ViewChild('canvasEl') canvasEl: ElementRef<HTMLCanvasElement> | undefined;
  loading: boolean = true;
  apiData: KokuDto.ChartPanelDto | undefined;
  private activeChart: Chart | undefined;

  constructor(private readonly chartService: ChartService) {
  }

  ngAfterViewInit(): void {
    this.loadData();
  }

  filterChanged() {
    this.loadData();
  }

  private loadData() {
    if (this.sourceUrl) {
      this.loading = true;

      const params: {
        [param: string]: string | string[];
      } = {};
      if (this.apiData?.filters) {
        for (const filter of this.apiData.filters) {
          params[filter.queryParam || ''] = filter.value || ''
        }
      }

      this.chartService.getChart(this.sourceUrl, params).subscribe((response: KokuDto.ChartPanelDto) => {
        if (this.canvasEl) {
          const yearRevenueTwoDimensionalContext = this.canvasEl.nativeElement.getContext('2d');
          if (yearRevenueTwoDimensionalContext) {
            const chartConfig: ChartConfiguration = {
              type: response.type || 'bar',
              data: {
                datasets: ((dataSets: KokuDto.ChartDataSet[] | undefined) => {
                  const result: ChartDataset[] = [];
                  for (const currentDataSet of dataSets || []) {
                    result.push({
                      backgroundColor: this.getBackgroundColor(currentDataSet.colors),
                      borderColor: this.getBorderColor(currentDataSet.colors),
                      pointRadius: 10,
                      pointHitRadius: 10,
                      pointHoverRadius: 15,
                      label: currentDataSet.label,
                      data: currentDataSet.data || []
                    });
                  }
                  return result;
                })(response.data?.datasets),
                labels: response.data?.labels
              },
              options: {
                animation: {
                  duration: 1000,
                },
                maintainAspectRatio: false,
                scales: {
                  y: {
                    min: 0,
                    beginAtZero: true,
                    ticks: {
                      precision: 0,
                      maxTicksLimit: 10
                    }
                  }
                }
              }
            };

            if (this.activeChart) {
              this.activeChart.destroy();
            }

            this.activeChart = new Chart(yearRevenueTwoDimensionalContext, chartConfig);
          }
        }
        this.loading = false;
        this.apiData = response;
      }, () => {
        this.loading = false;
      });
    }
  }

  private getBackgroundColor(colors: KokuDto.KokuColor[] | undefined) {
    let result: string[] = [];

    for (const color of colors || []) {
      switch (color) {
        case "PRIMARY":
          result.push('rgba(103, 58, 183, 0.25)');
          break;
        case "SECONDARY":
          result.push('rgba(213, 9, 255, 0.25)');
          break;
        case "TERTIARY":
          result.push('rgba(14, 57, 180, 0.25)');
          break;
        default:
          break;
      }
    }
    return result;
  }

  private getBorderColor(colors: KokuDto.KokuColor[] | undefined) {
    let result: string[] = [];
    for (const color of colors || []) {
      switch (color) {
        case "PRIMARY":
          result.push('rgba(103, 58, 183)');
          break;
        case "SECONDARY":
          result.push('rgba(213, 9, 255)');
          break;
        case "TERTIARY":
          result.push('rgba(14, 57, 180)');
          break;
        default:
          break;
      }
    }
    return result;
  }

}
