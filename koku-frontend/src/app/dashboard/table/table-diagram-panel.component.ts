import {Component, Input, OnInit} from "@angular/core";

@Component({
  selector: 'dashboard-table-panel',
  templateUrl: './table-diagram-panel.component.html',
  styleUrls: ['./table-diagram-panel.component.scss']
})
export class TableDiagramPanelComponent implements OnInit {

  @Input() config: KokuDto.TableDashboardColumnContent | null = null;
  displayedColumns: string[] = [];

  ngOnInit(): void {
    if (this.config) {
      const displayedCols: string[] = [];
      for (const currentCol of this.config.columns || []) {
        displayedCols.push(currentCol.label || '');
      }
      this.displayedColumns = displayedCols;
    }
  }

}
