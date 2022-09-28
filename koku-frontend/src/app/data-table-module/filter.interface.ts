import {EventEmitter} from "@angular/core";

export interface DataTableFilter {
  filterChanged: EventEmitter<void>;
}
