import {
  ComponentRef,
  Directive,
  InjectionToken,
  Injector,
  Input,
  OnInit,
  StaticProvider,
  ViewContainerRef
} from '@angular/core';
import {ComponentType} from '@angular/cdk/overlay';


export const DOCUMENT_FIELD_DATA = new InjectionToken<any>('DocumentFieldData');
export const DOCUMENT_FIELD_META = new InjectionToken<any>('DocumentFieldMeta');

@Directive({
  selector: '[fieldHost]',
})
export class DocumentFieldHostDirective implements OnInit {

  @Input() componentType: ComponentType<any> | undefined;
  @Input() componentMeta: any;
  @Input() componentData: any;
  private componentRef: ComponentRef<ComponentType<any>> | undefined;

  constructor(
    private readonly viewContainerRef: ViewContainerRef
  ) {
  }

  ngOnInit(): void {
    if (this.componentType !== undefined) {
      this.viewContainerRef.clear();
      const providers: StaticProvider[] = [
        {provide: DOCUMENT_FIELD_DATA, useValue: this.componentData},
        {provide: DOCUMENT_FIELD_META, useValue: this.componentMeta}
      ];
      this.componentRef = this.viewContainerRef.createComponent<typeof this.componentType>(this.componentType, {
        injector: Injector.create({
          providers
        })
      });
    }
  }


}
