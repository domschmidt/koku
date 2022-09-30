import {Component, ElementRef, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {cloneDeep} from "lodash";
import {DATA_TABLE_CONFIG, DataTableConfig} from "./data-table-config.injector";

export interface AdvancedFilterDialogData {
  advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[];
  columnDef: DataTableDto.DataTableColumnDto<any, any>;
}

export interface AdvancedFilterDialogResponse {
  advancedSearchSpec: DataTableDto.DataQueryAdvancedSearchDto[]
}

const itemStub: DataTableDto.DataQueryAdvancedSearchDto = {
  customOp: 'LIKE'
};

@Component({
  selector: 'advanced-filter-dialog',
  template: `
    <h1 mat-dialog-title class="dialog-header">
      Erweiterte Filtereinstellung für '{{data.columnDef.name}}'
      <button
        mat-icon-button
        tabindex="-1"
        (click)="dialogRef.close()"
      >
        <mat-icon>clear</mat-icon>
      </button>
    </h1>

    <form #advancedFilterForm="ngForm"
          (keydown.enter)="advancedFilterForm.onSubmit($event)"
          (ngSubmit)="applyChanges(advancedFilterForm)"
          class="advanced-filter-dialog-form"
    >
      <mat-dialog-content>
        <p>
          Spezifizieren Sie die Suchanfrage mit einer Folge von Filtern, die einzeln über ODER-Verknüpfung angewandt
          werden.
        </p>
        <div *ngFor="let currentSpec of tempAdvancedSearchSpec; let idx = index;"
             class="advanced-filter-dialog-form__row">

          <button color="warn"
                  (click)="deleteRow(idx)"
                  mat-icon-button
                  matSuffix
                  tabindex="-1"
                  class="advanced-filter-dialog-form__row__remove-btn"
                  type="button">
            <mat-icon>delete</mat-icon>
          </button>

          <mat-form-field class="advanced-filter-dialog-form__row__item"
                          floatLabel="always"
          >
            <mat-select #model="ngModel"
                        [(ngModel)]="currentSpec.customOp"
                        [name]="'advanced-filter-dialog-form' + idx + '_op_select'"
                        (keydown.enter)="$event.stopPropagation()"
            >
              <mat-option *ngFor="let possibleValue of possibleOpFieldOptions | keyvalue"
                          [value]="possibleValue.key">
                {{possibleValue.value}}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <div class="advanced-filter-dialog-form__row__item">

            <ng-template columnHost
                         *ngIf="dataTableConfig.columnTypes[data.columnDef.type] !== undefined"
                         [componentType]="dataTableConfig.columnTypes[data.columnDef.type].filterComponent"
                         [columnSpec]="data.columnDef"
                         [columnQuery]="currentSpec"
            ></ng-template>

          </div>
        </div>
        <button (click)="addNewRow()"
                color="primary"
                mat-raised-button
                (keydown.enter)="$event.stopPropagation()"
                type="button">
          <mat-icon>add</mat-icon>
          <span fxHide.lt-md style="margin-left: 4px;">Weiterer Filter</span>
        </button>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button mat-dialog-close>Abbrechen</button>
        <button mat-button (click)="advancedFilterForm.onSubmit($event)" [disabled]="!advancedFilterForm.valid">
          Übernehmen
        </button>
      </mat-dialog-actions>
    </form>
  `,
  styleUrls: ['./advanced-filter-dialog.component.scss'],
})
export class AdvancedFilterDialogComponent {

  constructor(@Inject(MAT_DIALOG_DATA) public data: AdvancedFilterDialogData,
              @Inject(DATA_TABLE_CONFIG) public readonly dataTableConfig: DataTableConfig,
              public dialogRef: MatDialogRef<AdvancedFilterDialogComponent, AdvancedFilterDialogResponse>,
              private readonly el: ElementRef,
              // private readonly htmlService: HtmlService todo
  ) {
    let tempSpec: {
      search?: any;
      customOp?: DataTableDto.DataQueryColumnOPDto;
    }[] = [];
    if (!data.advancedSearchSpec.length) {
      tempSpec.push(cloneDeep(itemStub));
    } else {
      tempSpec = cloneDeep(data.advancedSearchSpec);
    }
    this.tempAdvancedSearchSpec = tempSpec;
  }

  tempAdvancedSearchSpec: {
    search?: any;
    customOp?: DataTableDto.DataQueryColumnOPDto;
  }[];

  possibleOpFieldOptions: Record<Partial<DataTableDto.DataQueryColumnOPDto>, string> = {
    'LIKE': 'Enthält',
    'SW': 'Startet mit',
    'EQ': 'Entspricht exakt',
    'EW': 'Endet mit',
    'GOE': 'Größer oder gleich',
    'GT': 'Größer als',
    'LOE': 'Kleiner oder gleich',
    'LT': 'Kleiner als'
  };

  applyChanges(advancedFilterForm: NgForm) {
    if (advancedFilterForm.valid) {
      this.dialogRef.close({
        advancedSearchSpec: this.tempAdvancedSearchSpec
      });
    }
  }

  addNewRow() {
    this.tempAdvancedSearchSpec.push(cloneDeep(itemStub));

    setTimeout(() => {
      const advancedFilterRows = this.el.nativeElement.querySelectorAll('.advanced-filter-dialog-form__row');
      const lastAdvancedFilterRow = advancedFilterRows[advancedFilterRows.length - 1];
      // this.htmlService.focusFirstFocusableEl(lastAdvancedFilterRow); // todo
    });
  }

  deleteRow(idx: number) {
    this.tempAdvancedSearchSpec.splice(idx, 1);
  }
}
