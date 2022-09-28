import {InjectionToken} from "@angular/core";
import {ComponentType} from "@angular/cdk/overlay";

export interface DataTableConfig {
  columnTypes: {
    [key: string]: {
      cellComponent: ComponentType<any>;
      filterComponent: ComponentType<any>;
    }
  }
}

export const DATA_TABLE_CONFIG = new InjectionToken<DataTableConfig>('DataTableConfig');
