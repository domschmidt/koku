declare namespace DataTableDto {

  interface DataTableColumnDto<T, S> {
    id: string;
    name: string;
    type: string;
    isKey?: boolean;
    canSort?: boolean;
    canFilter?: boolean;
    defaultSortDir?: DataQueryColumnSortDirDto;
    defaultSortIdx?: number;
    hidden?: boolean;
    footerSummary?: T;
    defaultSearchValue?: T;
    typeSpecificSettings?: S;
  }

  interface DataTableDto {
    columns?: DataTableColumnDto<any, any>[];
    rows?: { [index: string]: any }[];
    tableName?: string;
    pageSize?: number;
    page?: number;
    total?: number;
    totalPages?: number;
  }

  interface DataQueryColumnSpecDto {
    search?: any;
    advancedSearchSpec?: DataQueryAdvancedSearchDto[];
    selectValues?: any[];
    sortDir?: DataQueryColumnSortDirDto;
    sortIdx?: number;
  }

  interface DataQuerySpecDto {
    page?: number;
    total?: number;
    columnSpecByColumnId?: { [index: string]: DataQueryColumnSpecDto };
    globalSearch?: string;
  }

  interface DataQueryAdvancedSearchDto {
    search?: any;
    customOp?: string;
  }

  type DataQueryColumnSortDirDto = "ASC" | "DESC";

  type DataQueryColumnOPDto = "EQ" | "LT" | "LOE" | "GT" | "GOE" | "LIKE" | "SW" | "EW";

}

