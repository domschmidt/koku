import {AfterViewInit, Component, ElementRef, Input, ViewChild} from "@angular/core";
import {Chart, ChartConfiguration, ChartDataset} from "chart.js";
import {replace} from "lodash";
import ChartDataLabels from 'chartjs-plugin-datalabels';
import fitty from 'fitty';
import ChartDataLabelsConfig = KokuDto.ChartDataLabelsConfig;

@Component({
  selector: 'dashboard-diagram-panel',
  templateUrl: './dashboard-diagram-panel.component.html',
  styleUrls: ['./dashboard-diagram-panel.component.scss']
})
export class DashboardDiagramPanelComponent implements AfterViewInit {

  @Input() config: KokuDto.DiagramDashboardColumnContent | null = null;
  @ViewChild('canvasEl') canvasEl: ElementRef<HTMLCanvasElement> | undefined;
  @ViewChild('mainEl') mainEl: ElementRef<HTMLDivElement> | undefined;
  @ViewChild('subEl') subEl: ElementRef<HTMLDivElement> | undefined;

  ngAfterViewInit(): void {
    if (this.canvasEl && this.config) {
      const canvasCtx = this.canvasEl.nativeElement.getContext('2d');
      if (canvasCtx) {
        const chartConfig: ChartConfiguration = {
          plugins: [ChartDataLabels],
          type: this.config.type || 'bar',
          data: {
            datasets: ((dataSets: KokuDto.ChartDataSet[] | undefined) => {
              const result: ChartDataset[] = [];
              for (const currentDataSet of dataSets || []) {
                result.push({
                  backgroundColor: this.getBackgroundColors(currentDataSet.colors),
                  borderColor: this.getBorderColors(currentDataSet.colors),
                  label: currentDataSet.label,
                  data: currentDataSet.data || [],
                  fill: currentDataSet.fill,
                  segment: {
                    backgroundColor: ((ctx) => {
                      let result;
                      const segmentedData = (currentDataSet.segmentedData || [])[ctx.p1DataIndex];
                      if (segmentedData !== null && segmentedData !== undefined) {
                        result = this.getBackgroundColor(segmentedData.backgroundColor);
                      }
                      return result;
                    }),
                    borderColor: ((ctx) => {
                      let result;
                      const segmentedData = (currentDataSet.segmentedData || [])[ctx.p1DataIndex];
                      if (segmentedData !== null && segmentedData !== undefined) {
                        result = this.getBorderColor(segmentedData.borderColor);
                      }
                      return result;
                    }),
                    borderDash: ((ctx) => {
                      let result;
                      const segmentedData = (currentDataSet.segmentedData || [])[ctx.p1DataIndex];
                      if (segmentedData !== null && segmentedData !== undefined && segmentedData.borderDashed === true) {
                        result = [6, 6];
                      }
                      return result;
                    })
                  },
                  datalabels: ((config?: ChartDataLabelsConfig) => {
                    return {
                      color: config?.color || '',
                      textAlign: config?.textAlign || 'left',
                      formatter: (value: any, context: any) => {
                        let label = config?.formatter || '';
                        label = replace(
                          label,
                          new RegExp('%LABEL%', 'g'),
                          context.chart.data.labels[context.dataIndex]
                        );
                        label = replace(
                          label,
                          new RegExp('%VALUE%', 'g'),
                          value
                        );
                        return label;
                      },
                    };
                  })(currentDataSet.datalabels)
                });
              }
              return result;
            })(this.config.data?.datasets),
            labels: this.config.data?.labels
          },
          options: {
            scales: {
              y: ((this.config.options || {}).scales || {}).y || {},
              x: ((this.config.options || {}).scales || {}).x || {}
            },
            elements: (this.config.options || {}).elements || {},
            plugins: (this.config.options || {}).plugins || {}
          }
        }
        new Chart(canvasCtx, chartConfig);
      }
    }

    (fitty as any).observeWindowDelay = 0
    if (this.mainEl) {
      fitty(this.mainEl.nativeElement, {
        minSize: 1,
        maxSize: 72
      });
    }
    if (this.subEl) {
      fitty(this.subEl.nativeElement, {
        minSize: 1,
        maxSize: 72
      });
    }
  }

  private getBackgroundColors(colors: KokuDto.KokuColor[] | undefined) {
    let result: string[] = [];

    for (const color of colors || []) {
      const backgroundColor = this.getBackgroundColor(color);
      if (backgroundColor) {
        result.push(backgroundColor);
      }
    }
    return result;
  }

  private getBackgroundColor(color?: KokuDto.KokuColor) {
    let result: string | undefined = undefined;
    switch (color) {
      case "PRIMARY":
        result = 'rgba(103, 58, 183, 0.25)';
        break;
      case "SECONDARY":
        result = 'rgba(213, 9, 255, 0.25)';
        break;
      case "TRANSPARENT":
        result = 'rgba(0,0,0,0.25)';
        break;
      case "TERTIARY":
        result = 'rgba(14, 57, 180, 0.25)';
        break;
      default:
        break;
    }
    return result;
  }

  private getBorderColors(colors: KokuDto.KokuColor[] | undefined) {
    let result: string[] = [];
    for (const color of colors || []) {
      const borderColor = this.getBorderColor(color);
      if (borderColor) {
        result.push(borderColor);
      }
    }
    return result;
  }

  private getBorderColor(color?: KokuDto.KokuColor) {
    let result: string | undefined = undefined;
    switch (color) {
      case "PRIMARY":
        result = 'rgba(103, 58, 183)';
        break;
      case "TRANSPARENT":
        result = 'rgba(0,0,0,0.25)';
        break;
      case "SECONDARY":
        result = 'rgba(213, 9, 255)';
        break;
      case "TERTIARY":
        result = 'rgba(14, 57, 180)';
        break;
      default:
        break;
    }
    return result;
  }
}
