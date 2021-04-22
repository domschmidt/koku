import {Component, Input} from '@angular/core';
import {GaugeService} from "./gauge.service";

@Component({
  selector: 'gauge-panel',
  templateUrl: './gauge-panel.component.html',
  styleUrls: ['./gauge-panel.component.scss']
})
export class GaugePanelComponent {

  @Input() sourceUrl: string = '';
  loading: boolean = true;
  apiData: KokuDto.GaugePanelDto | undefined;

  constructor(private readonly gaugeService: GaugeService) {
  }

  ngAfterViewInit(): void {
    this.loadData();
  }

  private loadData() {
    if (this.sourceUrl) {
      this.loading = true;
      this.gaugeService.getGauge(this.sourceUrl).subscribe((response: KokuDto.GaugePanelDto) => {
        this.loading = false;
        this.apiData = response;
      }, () => {
        this.loading = false;
      });
    }
  }

  getPercentageLabel(percentage: number): string {
    return Math.round(percentage) + '%';
  }
}
