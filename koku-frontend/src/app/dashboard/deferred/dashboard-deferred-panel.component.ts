import {Component, Input, OnInit} from "@angular/core";
import {DashboardService} from "../dashboard.service";

@Component({
  selector: 'dashboard-deferred-panel',
  templateUrl: './dashboard-deferred-panel.component.html',
  styleUrls: ['./dashboard-deferred-panel.component.scss']
})
export class DashboardDeferredPanelComponent implements OnInit {

  @Input() config: KokuDto.DeferredDashboardColumnContent | null = null;
  loading: boolean = false;
  content: KokuDto.IDashboardColumnContentUnion | null = null;

  constructor(private readonly dashboardService: DashboardService) {
  }

  ngOnInit(): void {
    this.loading = true;
    setTimeout(() => {
      if (this.config && this.config.href) {
        this.dashboardService.getDeferredPanelContent(this.config.href).subscribe((result) => {
          this.loading = false;
          this.content = result;
        });
      }
    }, 0);
  }


}
