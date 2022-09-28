import {Component, Inject} from "@angular/core";
import {TABLE_FIELD_COLUMN_SPEC, TABLE_FIELD_DATA} from "../../data-table-module/table-field-host.directive";


@Component({
  selector: 'bool-column',
  template: `
    <mat-icon *ngIf="data === true" inline>done</mat-icon>
    <mat-icon *ngIf="data !== true" inline>close</mat-icon>
  `,
  styles: [
    `:host {
      display: flex;
    }`
  ]
})
export class BooleanColumnComponent {

  constructor(
    @Inject(TABLE_FIELD_DATA) public data: Boolean,
    @Inject(TABLE_FIELD_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<Boolean, undefined>,
  ) {
  }

}
