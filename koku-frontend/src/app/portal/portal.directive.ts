import { AfterViewInit, Directive, EmbeddedViewRef, input, OnDestroy, TemplateRef } from '@angular/core';
import { OutletDirective } from './outlet.directive';

@Directive({
  standalone: true,
  selector: '[koku-portal]',
})
export class PortalDirective implements AfterViewInit, OnDestroy {
  portalOutlet = input<OutletDirective>();
  append = input<boolean>();

  private viewRef?: EmbeddedViewRef<unknown>;

  constructor(private templateRef: TemplateRef<any>) {}

  ngAfterViewInit() {
    const outletSnapshot = this.portalOutlet();
    if (!outletSnapshot) return;

    if (!this.append()) {
      outletSnapshot.viewContainerRef.clear();
    }

    this.viewRef = outletSnapshot.viewContainerRef.createEmbeddedView(this.templateRef);
  }

  ngOnDestroy() {
    this.viewRef?.destroy();
  }
}
