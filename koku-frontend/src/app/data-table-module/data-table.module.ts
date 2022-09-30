import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {DataTableComponent} from "./data-table.component";
import {AdvancedFilterDialogComponent} from "./advanced-filter-dialog.component";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {FormsModule} from "@angular/forms";
import {AdvancedFilterComponent} from "./advanced-filter.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {FlexLayoutModule} from "@angular/flex-layout";
import {MatInputModule} from "@angular/material/input";
import {MatTableModule} from "@angular/material/table";
import {MatSortModule} from "@angular/material/sort";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {MatPaginatorModule} from "@angular/material/paginator";
import {TableFieldHostDirective} from "./table-field-host.directive";
import {TableColumnHostDirective} from "./table-column-host.directive";

@NgModule({
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    FormsModule,
    MatFormFieldModule,
    MatSelectModule,
    FlexLayoutModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatProgressBarModule,
    MatPaginatorModule
  ],
  declarations: [
    DataTableComponent,
    AdvancedFilterDialogComponent,
    AdvancedFilterComponent,
    TableFieldHostDirective,
    TableColumnHostDirective
  ],
  exports: [
    DataTableComponent
  ],
  bootstrap: []
})
export class DataTableModule {
}
