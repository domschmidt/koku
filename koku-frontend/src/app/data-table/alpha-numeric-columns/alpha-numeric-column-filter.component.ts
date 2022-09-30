import {Component, EventEmitter, Inject, Output} from "@angular/core";
import {SummaryService} from "../../data-table-module/summary.service";
import {TABLE_COLUMN_QUERY, TABLE_COLUMN_SPEC} from "../../data-table-module/table-column-host.directive";
import {DataTableFilter} from "../../data-table-module/filter.interface";


@Component({
  selector: 'string-filter',
  template: `
    <mat-form-field class="string-filter">
      <input [(ngModel)]="columnQuery.search"
             (blur)="publishSimpleSpecChanges()"
             [disabled]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0"
             [placeholder]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0 ? buildSearchSummary(columnQuery.advancedSearchSpec) : ''"
             matInput
      >
      <button (click)="$event.stopPropagation(); columnQuery.search = undefined; publishSimpleSpecChanges()"
              [disabled]="!columnQuery.search"
              mat-icon-button
              matSuffix
              tabindex="-1"
              type="button">
        <mat-icon>clear</mat-icon>
      </button>
    </mat-form-field>
  `,
  styles: [
    `:host, .string-filter {
      display: flex;
      width: 100%;
      flex-direction: column;
    }`,
    `::ng-deep .mat-form-field-infix {
      width: auto;
    }`,
    `.string-filter {
      min-width: 60px;
    }`
  ],
})
export class AlphaNumericColumnFilterComponent implements DataTableFilter {

  @Output() filterChanged = new EventEmitter<void>();

  constructor(
    private readonly summaryService: SummaryService,
    @Inject(TABLE_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<String, KokuDto.AlphaNumericSettingsDto>,
    @Inject(TABLE_COLUMN_QUERY) public columnQuery: DataTableDto.DataQueryColumnSpecDto,
  ) {
  }

  publishSimpleSpecChanges() {
    if (this.columnQuery.advancedSearchSpec) {
      this.columnQuery.advancedSearchSpec.splice(0, this.columnQuery.advancedSearchSpec.length);
    }
    this.filterChanged.emit();
  }

  buildSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      return value + '';
    });
  }

}
