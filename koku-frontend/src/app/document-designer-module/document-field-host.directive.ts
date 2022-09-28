import {
  ComponentRef,
  Directive,
  InjectionToken,
  Injector,
  Input,
  OnInit,
  StaticProvider,
  ViewContainerRef
} from "@angular/core";
import {ComponentType} from "@angular/cdk/overlay";

export type DocumentFieldViewMode = 'CAPTURE' | 'DESIGN';

export interface DocumentFieldOptions {
  viewMode: DocumentFieldViewMode;
}

export const DOCUMENT_FIELD_DATA = new InjectionToken<any>('DocumentFieldData');

export const DOCUMENT_FIELD_OPTIONS = new InjectionToken<DocumentFieldOptions>('DocumentFieldOptions');

@Directive({
  selector: '[fieldHost]',
})
export class DocumentFieldHostDirective implements OnInit {

  @Input() componentType: ComponentType<any> | undefined;
  @Input() componentData: any;
  @Input() viewMode: DocumentFieldViewMode = 'DESIGN';
  private componentRef: ComponentRef<ComponentType<any>> | undefined;

  constructor(
    private readonly viewContainerRef: ViewContainerRef
  ) {
  }

  ngOnInit(): void {
    if (this.componentType !== undefined) {
      this.viewContainerRef.clear();
      const documentFieldOptions: DocumentFieldOptions = {
        viewMode: this.viewMode
      }
      const providers: StaticProvider[] = [
        {provide: DOCUMENT_FIELD_DATA, useValue: this.componentData},
        {
          provide: DOCUMENT_FIELD_OPTIONS, useValue: documentFieldOptions
        },
      ];
      this.componentRef = this.viewContainerRef.createComponent<typeof this.componentType>(this.componentType, {
        injector: Injector.create({
          providers
        })
      });
    }
  }


}
