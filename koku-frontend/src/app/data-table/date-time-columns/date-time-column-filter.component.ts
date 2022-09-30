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
               (change)="writeValue()"
               (blur)="publishSimpleSpecChanges()"
               [disabled]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0"
               [placeholder]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0 ? buildDateSearchSummary(columnQuery.advancedSearchSpec) : ''"
               matInput
               [type]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0 ? '' : 'date'"
               max="9999-12-31"
        >
        <button (click)="$event.stopPropagation(); date = undefined; writeValue(); publishSimpleSpecChanges()"
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
               (change)="writeValue()"
               (blur)="publishSimpleSpecChanges()"
               [disabled]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0"
               [placeholder]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0 ? buildTimeSearchSummary(columnQuery.advancedSearchSpec) : ''"
               matInput
               [type]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0 ? '' : 'time'"
        >
        <button (click)="$event.stopPropagation(); time = undefined; writeValue(); publishSimpleSpecChanges()"
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
    public readonly summaryService: SummaryService,
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

  writeValue() {
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
  }

  publishSimpleSpecChanges() {
    this.filterChanged.emit();
  }

  buildTimeSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      const date = moment(value, 'YYYY-MM-DD', true);
      if (date.isValid()) {
        return '';
      } else {
        const time = moment(value, 'HH:mm', true);
        if (time.isValid()) {
          return time.format('HH:mm');
        } else {
          const dateAndTime = moment(value, moment.ISO_8601, true);
          if (dateAndTime.isValid()) {
            return dateAndTime.format('HH:mm');
          } else {
            return '';
          }
        }
      }
    });
  }

  buildDateSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      const time = moment(value, 'HH:mm', true);
      if (time.isValid()) {
        return '';
      } else {
        const date = moment(value, 'YYYY-MM-DD', true);
        if (date.isValid()) {
          return date.format('YYYY-MM-DD');
        } else {
          const dateAndTime = moment(value, moment.ISO_8601, true);
          if (dateAndTime.isValid()) {
            return dateAndTime.format('YYYY-MM-DD');
          } else {
            return '';
          }
        }
      }
    });
  }

}
