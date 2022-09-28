import {Component, EventEmitter, Input, Output} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {
  AdvancedFilterDialogComponent,
  AdvancedFilterDialogData,
  AdvancedFilterDialogResponse
} from "./advanced-filter-dialog.component";


@Component({
  selector: 'advanced-filter',
  template: `
    <button (click)="openAdvancedFilter()"
            class="mat-primary"
            color="primary"
            mat-icon-button
            tabindex="-1"
            [disabled]="disabled"
            type="button">
      <mat-icon class="custom-svg-icon"
                svgIcon="filter"
                *ngIf="!advancedSearchSpec || advancedSearchSpec.length == 0"
                color="primary"></mat-icon>
      <mat-icon class="custom-svg-icon"
                svgIcon="filter_filled"
                *ngIf="advancedSearchSpec && advancedSearchSpec.length > 0"
                color="accent"></mat-icon>
    </button>
  `,
})
export class AdvancedFilterComponent {

  @Input() advancedSearchSpec!: DataTableDto.DataQueryAdvancedSearchDto[];
  @Input() columnDef!: DataTableDto.DataTableColumnDto<any, any>;
  @Output() afterChanged = new EventEmitter<void>();
  @Input() disabled: boolean | undefined;

  constructor(
    private readonly dialog: MatDialog
  ) {
  }

  openAdvancedFilter() {
    if (!this.disabled) {
      const dialogRef = this.dialog.open<AdvancedFilterDialogComponent, AdvancedFilterDialogData, AdvancedFilterDialogResponse>(AdvancedFilterDialogComponent, {
        data: {
          advancedSearchSpec: this.advancedSearchSpec,
          columnDef: this.columnDef
        },
        closeOnNavigation: false,
        position: {
          top: '20px'
        }
      });
      dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          this.advancedSearchSpec.splice(0, this.advancedSearchSpec.length);
          for (const currentSearchSpec of result.advancedSearchSpec || []) {
            this.advancedSearchSpec.push(currentSearchSpec);
          }
          this.afterChanged.emit();
        }
      })
    }
  }
}
