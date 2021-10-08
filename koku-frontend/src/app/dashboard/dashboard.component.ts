import {Component, OnInit} from "@angular/core";
import {DashboardService} from "./dashboard.service";

@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  loading: boolean = false;
  dashboardConfig: KokuDto.DashboardConfigDto | null = null;

  constructor(readonly dashboardService: DashboardService) {
  }

  ngOnInit(): void {
    this.loading = true;
    this.dashboardService.getConfig().subscribe((result) => {
      this.loading = false;
      this.dashboardConfig = result;
    })
  }
}
