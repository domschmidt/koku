import {Component, EventEmitter, Inject, Input, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import {TableService} from "./table.service";
import {MatPaginatorIntl, PageEvent} from "@angular/material/paginator";
import {Sort} from "@angular/material/sort";
import {debounce} from "lodash";
import {Subject, Subscription} from "rxjs";
import {PaginatorIntlService} from "../data-table/paginatorIntl.service";
import {MatTable} from "@angular/material/table";
import {SnackBarService} from "../snackbar/snack-bar.service";
import {DATA_TABLE_CONFIG, DataTableConfig} from "./data-table-config.injector";

export type DATATABLE_COLORS = 'RED'

export interface FullTextSearchSuffixButton {
  icon: string;
  title: string;
  onClick: (evt: MouseEvent, positiveCB: (newGlobalSearchTerm: string) => void) => void;
}

@Component({
  selector: 'data-table',
  templateUrl: './data-table.component.html',
  styleUrls: ['./data-table.component.scss'],
  providers: [
    {provide: MatPaginatorIntl, useClass: PaginatorIntlService}
  ]
})
export class DataTableComponent implements OnInit {

  @Input() endpoint: string | undefined;
  @Input() disabled: boolean | undefined;
  @Input() queryParams: { [key: string]: string[] | string } | undefined;
  @Input() selectMode: 'NONE' | 'SINGLE' | 'MULTI' = 'NONE';
  @Input() tableActionsContainer: TemplateRef<any> | undefined;
  @Input() tableRowActionsContainer: TemplateRef<any> | undefined;
  @Input() tableRowSelectionContainer: TemplateRef<any> | undefined;
  @Input() tableRowSelectionHeaderContainer: TemplateRef<any> | undefined;
  @Input() reloadSubject: Subject<DataTableDto.DataQuerySpecDto | void> | undefined;
  @Input() hideHeadline!: boolean;
  @Input() exportSubject: Subject<string> | undefined;
  @Input() pageSizeOptions: number[] = [10, 50, 100, 250, 1000];
  @Input() defaultPageSize: number = 10;
  @Input() colorizeRows!: (currentPageItems: { [index: string]: any }[]) => Map<{ [index: string]: any }, DATATABLE_COLORS>;
  @Output() rowSelected = new EventEmitter<{ key: string; value: any }[]>();
  @ViewChild(MatTable, {static: false}) table: MatTable<any> | undefined;

  displayedColumns: string[] = [];
  dataSource: DataTableDto.DataTableDto | undefined;
  dataSourceConfiguration: DataTableDto.DataQuerySpecDto = {};
  loading: boolean = false;
  exportRunning: boolean = false;
  hasAnySum: boolean = false;

  customRowColorClasses: Map<{ [index: string]: any }, DATATABLE_COLORS> = new Map<{ [p: string]: any }, DATATABLE_COLORS>();

  private currentSubscription: Subscription | undefined;
  private throttledLoad = debounce(() => {
    if (!this.endpoint) {
      throw new Error('endpoint not initialized.');
    }
    if (this.currentSubscription) {
      this.currentSubscription.unsubscribe();
    }

    if (!this.disabled) {
      this.loading = true;
      this.currentSubscription = this.tableService.loadTable(this.endpoint, {
        total: this.defaultPageSize,
        ...this.dataSourceConfiguration
      }, this.queryParams).subscribe((result) => {
        if (this.colorizeRows) {
          this.customRowColorClasses = this.colorizeRows(result.rows || []);
        }
        if (this.tableRowSelectionContainer && this.displayedColumns.indexOf('rowSelection') < 0) {
          this.displayedColumns.push('rowSelection');
        }
        if (this.tableRowActionsContainer && this.displayedColumns.indexOf('rowActions') < 0) {
          this.displayedColumns.push('rowActions');
        }
        if ((this.dataSourceConfiguration.page || 0) > (result.totalPages || 0)) {
          // reset page if out of bounds
          this.dataSourceConfiguration.page = 0;
          this.throttledLoad();
        } else {
          if (!this.dataSourceConfiguration.columnSpecByColumnId) {
            this.dataSourceConfiguration.columnSpecByColumnId = {};
          }
          for (const currentCol of result.columns || []) {
            if (!currentCol.hidden && this.displayedColumns.indexOf(currentCol.id || '') < 0) {
              this.displayedColumns.push(currentCol.id || '');
            }
            if (!this.dataSourceConfiguration.columnSpecByColumnId[currentCol.id]) {
              this.dataSourceConfiguration.columnSpecByColumnId[currentCol.id] = {
                sortIdx: currentCol.defaultSortIdx,
                sortDir: currentCol.defaultSortDir,
                search: currentCol.defaultSearchValue,
                advancedSearchSpec: []
              };
            }
            if (currentCol.footerSummary) {
              this.hasAnySum = true;
            }
          }
          this.loading = false;
          this.dataSource = result;
          setTimeout(() => {
            this.table?.updateStickyColumnStyles();
          });

        }
      }, (error) => {
        // this.snackBarService.openErrorSnack(error.error.message);
        this.loading = false;
      });
    }
  }, 500, {
    leading: false
  });

  constructor(
    private readonly tableService: TableService,
    private readonly snackBarService: SnackBarService,
    @Inject(DATA_TABLE_CONFIG) public readonly dataTableConfig: DataTableConfig
  ) {
  }

  ngOnInit(): void {
    this.loadTable();
    if (this.reloadSubject) {
      this.reloadSubject.subscribe((value) => {
        if (value !== undefined) {
          this.dataSourceConfiguration = {
            ...this.dataSourceConfiguration,
            ...value
          };
        }
        this.loadTable();
      });
    }
  }

  paginationChanged(pageChangeEvent: PageEvent) {
    this.dataSourceConfiguration.page = pageChangeEvent.pageIndex;
    this.dataSourceConfiguration.total = pageChangeEvent.pageSize;
    this.loadTable();
  }

  loadTable() {
    this.throttledLoad();
  }

  sortChanged(sortOptions: Sort) {
    if (!this.disabled) {
      if (this.dataSourceConfiguration.columnSpecByColumnId) {
        for (const currentColumn of this.dataSource?.columns || []) {
          if (currentColumn.id !== sortOptions.active) {
            if (this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id]) {
              delete this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id].sortDir;
              delete this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id].sortIdx;
            }
          } else {
            if (sortOptions.direction === '') {
              if (this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id]) {
                delete this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id].sortDir;
                delete this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id].sortIdx;
              }
            } else {
              this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id].sortDir = sortOptions.direction === 'asc' ? "ASC" : "DESC";
              this.dataSourceConfiguration.columnSpecByColumnId[currentColumn.id].sortIdx = 0;
            }
          }
        }
        this.throttledLoad();
      }
    }
  }

  trackByIndex(index: number, obj: any): any {
    return index;
  }
}
