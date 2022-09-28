import {Component, Inject} from "@angular/core";
import {TABLE_FIELD_COLUMN_SPEC, TABLE_FIELD_DATA} from "../../data-table-module/table-field-host.directive";
import * as moment from "moment";

@Component({
  selector: 'date-time-column',
  template: `
    <span>{{userReadableDate}}</span>
  `,
  styles: [
    `:host {
      display: flex;
    }`
  ]
})
export class DateTimeColumnComponent {

  userReadableDate: string | undefined;

  constructor(
    @Inject(TABLE_FIELD_DATA) public data: string,
    @Inject(TABLE_FIELD_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<string, undefined>,
  ) {
    if (data) {
      this.userReadableDate = moment(data).format('DD.MM.YYYY HH:mm [Uhr]');
    }
  }

}
