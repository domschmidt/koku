import {Component, EventEmitter, Inject, Output} from "@angular/core";
import {SummaryService} from "../../data-table-module/summary.service";
import {TABLE_COLUMN_QUERY, TABLE_COLUMN_SPEC} from "../../data-table-module/table-column-host.directive";
import {DataTableFilter} from "../../data-table-module/filter.interface";


@Component({
  selector: 'file-size-filter',
  template: `
    <div class="wrapper">
      <mat-form-field class="size-field">
        <input [(ngModel)]="size"
               (change)="writeValue()"
               (blur)="publishSimpleSpecChanges()"
               matInput
               step="0.01"
               min="0"
               [dropSpecialCharacters]="false"
               [mask]="'0*,00'"
               [disabled]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0"
               [placeholder]="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0 ? buildSizeSearchSummary(columnQuery.advancedSearchSpec) : ''"
        >
        <button (click)="$event.stopPropagation(); size = undefined; writeValue(); publishSimpleSpecChanges()"
                [disabled]="!size"
                mat-icon-button
                matSuffix
                tabindex="-1"
                type="button">
          <mat-icon inline>clear</mat-icon>
        </button>
        <mat-hint>Größe</mat-hint>
      </mat-form-field>
      <mat-form-field class="scale-field">
        <mat-select [(value)]="scale"
                    (valueChange)="writeValue()"
                    (blur)="publishSimpleSpecChanges()"
                    *ngIf="columnQuery.advancedSearchSpec === undefined || columnQuery.advancedSearchSpec.length === 0">
          <mat-option [value]="currentScale.scale" *ngFor="let currentScale of scales">
            {{currentScale.text}}
          </mat-option>
        </mat-select>
        <input *ngIf="columnQuery.advancedSearchSpec !== undefined && columnQuery.advancedSearchSpec.length > 0"
               [disabled]="true"
               [placeholder]="buildScaleSearchSummary(columnQuery.advancedSearchSpec)"
               matInput
        >
        <mat-hint>Skalierung</mat-hint>
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
    `.size-field, .scale-field {
      width: 100%;
    }`,
    `.size-field {
      min-width: 90px;
    }`,
    `.scale-field {
      margin-left: 10px;
      min-width: 50px;
    }`,
  ],
})
export class FileSizeColumnFilterComponent implements DataTableFilter {

  size: string | undefined;
  private readonly GB = 1024 * 1024 * 1024;
  private readonly MB = 1024 * 1024;
  private readonly KB = 1024;
  scale: number = this.MB;

  scales = [{
    scale: 1,
    text: 'B'
  }, {
    scale: this.KB,
    text: 'KB'
  }, {
    scale: this.MB,
    text: 'MB'
  }, {
    scale: this.GB,
    text: 'GB'
  }]

  @Output() filterChanged = new EventEmitter<void>();

  constructor(
    private readonly summaryService: SummaryService,
    @Inject(TABLE_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<String, KokuDto.AlphaNumericSettingsDto>,
    @Inject(TABLE_COLUMN_QUERY) public columnQuery: DataTableDto.DataQueryColumnSpecDto,
  ) {
    if (columnQuery.search !== undefined) {
      if (columnQuery.search > this.GB) {
        this.size = (columnQuery.search / this.GB).toFixed(2).replace('.', ',');
        this.scale = this.GB;
      } else if (columnQuery.search > this.MB) {
        this.size = (columnQuery.search / this.MB).toFixed(2).replace('.', ',');
        this.scale = this.MB;
      } else if (columnQuery.search > this.KB) {
        this.size = (columnQuery.search / this.KB).toFixed(2).replace('.', ',');
        this.scale = this.KB;
      } else {
        this.size = columnQuery.search.toFixed(2).replace('.', ',');
        this.scale = 1;
      }
    }
  }

  writeValue() {
    let valueToBePublished: number | undefined = undefined;
    if (this.size !== undefined) {
      valueToBePublished = Number((Number(this.size.replace(',', '.')) * this.scale).toFixed(2));
    }
    this.columnQuery.search = valueToBePublished;
    if (this.columnQuery.advancedSearchSpec) {
      this.columnQuery.advancedSearchSpec.splice(0, this.columnQuery.advancedSearchSpec.length);
    }
  }

  publishSimpleSpecChanges() {
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

  buildSizeSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      if (value > this.GB) {
        return (value / this.GB).toFixed(2).replace('.', ',');
      } else if (value > this.MB) {
        return (value / this.MB).toFixed(2).replace('.', ',');
      } else if (value > this.KB) {
        return (value / this.KB).toFixed(2).replace('.', ',');
      } else {
        return value.toFixed(2).replace('.', ',');
      }
    });
  }

  buildScaleSearchSummary(advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]) {
    return this.summaryService.printSummary(advancedSearchSpec, (value: any) => {
      if (value > this.GB) {
        return 'GB';
      } else if (value > this.MB) {
        return 'MB';
      } else if (value > this.KB) {
        return 'KB';
      } else {
        return 'B';
      }
    });
  }
}
