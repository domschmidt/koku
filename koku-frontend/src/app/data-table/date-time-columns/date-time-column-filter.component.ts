import {Component, EventEmitter, Inject, Output} from "@angular/core";
import {SummaryService} from "../../data-table-module/summary.service";
import {TABLE_COLUMN_QUERY, TABLE_COLUMN_SPEC} from "../../data-table-module/table-column-host.directive";
import {MAT_CHECKBOX_DEFAULT_OPTIONS, MatCheckboxDefaultOptions} from "@angular/material/checkbox";
import {DataTableFilter} from "../../data-table-module/filter.interface";
import * as moment from "moment";


@Component({
  selector: 'date-time-filter',
  template: `
    <div class="wrapper">
      <mat-form-field class="date-field">
        <input [(ngModel)]="date"
               (blur)="publishSimpleSpecChanges()"
               matInput
               type="date"
               max="9999-12-31"
        >
        <button (click)="$event.stopPropagation(); date = undefined; publishSimpleSpecChanges()"
                [disabled]="!date"
                mat-icon-button
                matSuffix
                tabindex="-1"
                type="button">
          <mat-icon inline>clear</mat-icon>
        </button>
        <mat-hint>Datum</mat-hint>
      </mat-form-field>
      <mat-form-field class="time-field">
        <input [(ngModel)]="time"
               (blur)="publishSimpleSpecChanges()"
               matInput
               type="time"
        >
        <button (click)="$event.stopPropagation(); time = undefined; publishSimpleSpecChanges()"
                [disabled]="!time"
                mat-icon-button
                matSuffix
                tabindex="-1"
                type="button">
          <mat-icon>clear</mat-icon>
        </button>
        <mat-hint>Uhrzeit</mat-hint>
      </mat-form-field>
    </div>
  `,
  styles: [
    `:host {
      display: flex;
      width: 100%;
      flex-direction: column;
    }`,
    `.wrapper {
      display: flex;
      width: 100%;
      flex-direction: row;
      align-items: baseline;
    }`,
    `.date-field, .time-field {
      width: 100%;
    }`,
    `.time-field {
      margin-left: 10px;
      min-width: 60px;
    }`,
    `.date-field {
      min-width: 90px;
    }`,
  ],
  providers: [
    {provide: MAT_CHECKBOX_DEFAULT_OPTIONS, useValue: {clickAction: 'noop'} as MatCheckboxDefaultOptions}
  ]
})
export class DateTimeColumnFilterComponent implements DataTableFilter {

  @Output() filterChanged = new EventEmitter<void>();
  date: string | undefined;
  time: string | undefined;

  constructor(
    private readonly summaryService: SummaryService,
    @Inject(TABLE_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<Boolean, undefined>,
    @Inject(TABLE_COLUMN_QUERY) public columnQuery: DataTableDto.DataQueryColumnSpecDto,
  ) {
    if (columnQuery.search) {
      const date = moment(columnQuery.search, 'YYYY-MM-DD', true);
      if (date.isValid()) {
        this.date = date.format('YYYY-MM-DD');
        this.time = undefined;
      } else {
        const time = moment(columnQuery.search, 'HH:mm', true);
        if (time.isValid()) {
          this.date = undefined;
          this.time = time.format('HH:mm');
        } else {
          const dateAndTime = moment(columnQuery.search, moment.ISO_8601, true);
          if (dateAndTime.isValid()) {
            this.date = dateAndTime.format('YYYY-MM-DD');
            this.time = dateAndTime.format('HH:mm');
          } else {
            this.date = undefined;
            this.time = undefined;
          }
        }
      }
    } else {
      this.date = undefined;
      this.time = undefined;
    }
  }

  publishSimpleSpecChanges() {
    if (this.date && this.time) {
      this.columnQuery.search = this.date + 'T' + this.time;
    } else if (this.date) {
      this.columnQuery.search = this.date;
    } else if (this.time) {
      this.columnQuery.search = this.time;
    } else {
      this.columnQuery.search = undefined;
    }
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
