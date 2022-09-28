import {Component, Inject} from "@angular/core";
import {TABLE_FIELD_COLUMN_SPEC, TABLE_FIELD_DATA} from "../../data-table-module/table-field-host.directive";


@Component({
  selector: 'string-column',
  template: `
    <span title="{{data}}">{{data}}</span>
  `,
})
export class AlphaNumericColumnComponent {

  constructor(
    @Inject(TABLE_FIELD_DATA) public data: String,
    @Inject(TABLE_FIELD_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<String, KokuDto.AlphaNumericSettingsDto>,
  ) {
  }

}
