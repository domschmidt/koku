import {Component, Input} from '@angular/core';
import {TextService} from "./text.service";

@Component({
  selector: 'text-panel',
  templateUrl: './text-panel.component.html',
  styleUrls: ['./text-panel.component.scss']
})
export class TextPanelComponent {

  @Input() sourceUrl: string = '';
  loading: boolean = true;
  apiData: KokuDto.TextPanelDto | undefined;

  constructor(private readonly textService: TextService) {
  }

  ngAfterViewInit(): void {
    this.loadData();
  }

  private loadData() {
    if (this.sourceUrl) {
      this.loading = true;
      this.textService.getText(this.sourceUrl).subscribe((response: KokuDto.TextPanelDto) => {
        this.loading = false;
        this.apiData = response;
      }, () => {
        this.loading = false;
      });
    }
  }

}
