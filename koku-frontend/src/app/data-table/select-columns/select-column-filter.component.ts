import {Component, EventEmitter, Inject, Output} from "@angular/core";
import {SummaryService} from "../../data-table-module/summary.service";
import {TABLE_COLUMN_QUERY, TABLE_COLUMN_SPEC} from "../../data-table-module/table-column-host.directive";
import {DataTableFilter} from "../../data-table-module/filter.interface";


@Component({
  selector: 'select-filter',
  template: `
    <mat-form-field class="select-field">
      <mat-select [(ngModel)]="selectedVals"
                  [multiple]="true"
                  (closed)="publishSimpleSpecChanges()"
      >
        <mat-option [value]="currentValue"
                    *ngFor="let currentValue of columnSpec.possibleSelectValues">
          {{currentValue}}
        </mat-option>
      </mat-select>
      <button (click)="$event.stopPropagation(); selectedVals = undefined; publishSimpleSpecChanges()"
              [disabled]="selectedVals === undefined || selectedVals.length === 0"
              mat-icon-button
              matSuffix
              tabindex="-1"
              type="button">
        <mat-icon inline>clear</mat-icon>
      </button>
    </mat-form-field>
  `,
  styles: [
    `:host {
      display: flex;
      width: 100%;
      flex-direction: column;
    }`
  ],
})
export class SelectColumnFilterComponent implements DataTableFilter {

  @Output() filterChanged = new EventEmitter<void>();
  selectedVals: any[] | undefined;

  constructor(
    private readonly summaryService: SummaryService,
    @Inject(TABLE_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<String, KokuDto.AlphaNumericSettingsDto>,
    @Inject(TABLE_COLUMN_QUERY) public columnQuery: DataTableDto.DataQueryColumnSpecDto,
  ) {
    const selectedVals: any[] = [];
    for (const currentSpec of columnQuery.advancedSearchSpec || []) {
      selectedVals.push(currentSpec.search);
    }
    this.selectedVals = selectedVals;
  }

  publishSimpleSpecChanges() {
    const selectedVals: DataTableDto.DataQueryAdvancedSearchDto[] = [];
    for (const currentSelectedVal of this.selectedVals || []) {
      selectedVals.push({
        search: currentSelectedVal,
        customOp: 'EQ'
      });
    }
    this.columnQuery.advancedSearchSpec = selectedVals;
    this.filterChanged.emit();
  }

  buildSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      return value + '';
    });
  }
}
