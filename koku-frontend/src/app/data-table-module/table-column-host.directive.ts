import {
  ComponentRef,
  Directive,
  EventEmitter,
  InjectionToken,
  Injector,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  StaticProvider,
  ViewContainerRef
} from "@angular/core";
import {ComponentType} from "@angular/cdk/overlay";
import {DataTableFilter} from "./filter.interface";

export const TABLE_COLUMN_SPEC = new InjectionToken<any>('TableColumnSpec');
export const TABLE_COLUMN_QUERY = new InjectionToken<any>('TableColumnQuery');

@Directive({
  selector: '[columnHost]',
})
export class TableColumnHostDirective implements OnInit, OnChanges {

  @Input() componentType: ComponentType<any> | undefined;
  @Input() columnSpec: DataTableDto.DataTableColumnDto<any, any> | undefined;
  @Input() columnQuery: DataTableDto.DataQueryColumnSpecDto | undefined;
  @Output() filterChanged = new EventEmitter<void>();
  private componentRef: ComponentRef<DataTableFilter> | undefined;

  constructor(
    private readonly viewContainerRef: ViewContainerRef
  ) {
  }

  ngOnInit(): void {
    this.renderContent();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.renderContent();
  }

  private renderContent() {
    if (this.componentType !== undefined) {
      this.viewContainerRef.clear();
      const providers: StaticProvider[] = [
        {provide: TABLE_COLUMN_SPEC, useValue: this.columnSpec},
        {provide: TABLE_COLUMN_QUERY, useValue: this.columnQuery},
      ];
      this.componentRef = this.viewContainerRef.createComponent<DataTableFilter>(this.componentType, {
        injector: Injector.create({
          providers
        })
      });
      if (this.componentRef !== undefined && this.componentRef.instance !== undefined) {
        this.componentRef.instance.filterChanged.subscribe(() => {
          this.filterChanged.emit();
        });
      }
    }
  }
}
