import {Component, EventEmitter, Inject, Output} from "@angular/core";
import {SummaryService} from "../../data-table-module/summary.service";
import {TABLE_COLUMN_QUERY, TABLE_COLUMN_SPEC} from "../../data-table-module/table-column-host.directive";
import {MAT_CHECKBOX_DEFAULT_OPTIONS, MatCheckboxDefaultOptions} from "@angular/material/checkbox";
import {DataTableFilter} from "../../data-table-module/filter.interface";


@Component({
  selector: 'boolean-filter',
  template: `
    <mat-checkbox [checked]="columnQuery.search === true"
                  (click)="next()"
                  [indeterminate]="columnQuery.search === false"></mat-checkbox>
  `,
  styles: [
    `:host {
      display: flex;
      width: 100%;
      flex-direction: column;
    }`
  ],
  providers: [
    {provide: MAT_CHECKBOX_DEFAULT_OPTIONS, useValue: {clickAction: 'noop'} as MatCheckboxDefaultOptions}
  ]
})
export class BooleanColumnFilterComponent implements DataTableFilter {

  @Output() filterChanged = new EventEmitter<void>();
  private tape: (boolean | undefined)[] = [undefined, true, false];

  constructor(
    private readonly summaryService: SummaryService,
    @Inject(TABLE_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<Boolean, undefined>,
    @Inject(TABLE_COLUMN_QUERY) public columnQuery: DataTableDto.DataQueryColumnSpecDto,
  ) {
  }

  next() {
    this.publishSimpleSpecChanges(this.tape[(this.tape.indexOf(this.columnQuery.search) + 1) % this.tape.length]);
  }

  publishSimpleSpecChanges($event: boolean | undefined) {
    this.columnQuery.search = $event;
    if (this.columnQuery.advancedSearchSpec) {
      this.columnQuery.advancedSearchSpec.splice(0, this.columnQuery.advancedSearchSpec.length);
    }
    this.filterChanged.emit();
  }

  publishAdvancedChanges() {
    delete this.columnQuery.search;
    this.filterChanged.emit();
  }

  buildSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      return value + '';
    });
  }

}
