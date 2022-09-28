import {
  ComponentRef,
  Directive,
  InjectionToken,
  Injector,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges,
  StaticProvider,
  ViewContainerRef
} from "@angular/core";
import {ComponentType} from "@angular/cdk/overlay";

export const TABLE_FIELD_DATA = new InjectionToken<any>('TableFieldData');
export const TABLE_FIELD_COLUMN_SPEC = new InjectionToken<any>('TableFieldColumnSpec');

@Directive({
  selector: '[fieldHost]',
})
export class TableFieldHostDirective implements OnInit, OnChanges {

  @Input() componentType: ComponentType<any> | undefined;
  @Input() columnSpec: DataTableDto.DataTableColumnDto<any, any> | undefined;
  @Input() componentData: any;
  private componentRef: ComponentRef<ComponentType<any>> | undefined;

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
        {provide: TABLE_FIELD_DATA, useValue: this.componentData},
        {provide: TABLE_FIELD_COLUMN_SPEC, useValue: this.columnSpec},
      ];
      this.componentRef = this.viewContainerRef.createComponent<typeof this.componentType>(this.componentType, {
        injector: Injector.create({
          providers
        })
      });
    }
  }
}
