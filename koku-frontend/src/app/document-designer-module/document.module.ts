import {NgModule} from "@angular/core";

import {DocumentDesignerComponent} from "./document-designer.component";
import {DOCUMENT_CONFIG} from "./document-field-config.injector";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";
import {MatMenuModule} from "@angular/material/menu";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {FormsModule} from "@angular/forms";
import {MatDialogModule} from "@angular/material/dialog";
import {SortablejsModule} from "ngx-sortablejs";
import {MatButtonToggleModule} from "@angular/material/button-toggle";
import {CommonModule, KeyValuePipe} from "@angular/common";
import {FlexLayoutModule} from "@angular/flex-layout";
import {DocumentFieldHostDirective} from "./document-field-host.directive";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {DocumentCaptureComponent} from "./document-capture.component";

@NgModule({
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatProgressSpinnerModule,
    FormsModule,
    MatDialogModule,
    SortablejsModule,
    MatButtonToggleModule,
    KeyValuePipe,
    FlexLayoutModule,
    MatFormFieldModule,
    MatInputModule
  ],
  providers: [{
    provide: DOCUMENT_CONFIG, useValue: {}
  }],
  declarations: [
    DocumentDesignerComponent,
    DocumentFieldHostDirective,
    DocumentCaptureComponent
  ],
  exports: [
    DocumentDesignerComponent,
    DocumentFieldHostDirective,
    DocumentCaptureComponent
  ],
  bootstrap: []
})
export class DocumentModule {
}
