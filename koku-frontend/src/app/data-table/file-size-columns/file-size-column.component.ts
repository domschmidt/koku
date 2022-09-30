import {Component, Inject} from "@angular/core";
import {TABLE_FIELD_COLUMN_SPEC, TABLE_FIELD_DATA} from "../../data-table-module/table-field-host.directive";


@Component({
  selector: 'file-size-column',
  template: `
    <span title="{{value}}">{{value}}</span>
  `,
})
export class FileSizeColumnComponent {

  value: string | undefined;

  private readonly GB = 1024 * 1024 * 1024;
  private readonly MB = 1024 * 1024;
  private readonly KB = 1024;

  constructor(
    @Inject(TABLE_FIELD_DATA) public data: number,
    @Inject(TABLE_FIELD_COLUMN_SPEC) public columnSpec: DataTableDto.DataTableColumnDto<String, KokuDto.AlphaNumericSettingsDto>,
  ) {
    if (data !== undefined && data !== null) {
      if (data > this.GB) {
        this.value = (data / this.GB).toFixed(2).replace('.', ',') + ' GB';
      } else if (data > this.MB) {
        this.value = (data / this.MB).toFixed(2).replace('.', ',') + ' MB';
      } else if (data > this.KB) {
        this.value = (data / this.KB).toFixed(2).replace('.', ',') + ' KB';
      } else {
        this.value = data.toFixed(2).replace('.', ',') + ' B';
      }
    } else {
      this.value = undefined;
    }
  }

}
