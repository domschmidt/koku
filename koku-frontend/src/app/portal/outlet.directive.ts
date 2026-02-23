import { Directive, ViewContainerRef } from '@angular/core';

@Directive({
  standalone: true,
  selector: '[koku-outlet]',
  exportAs: 'koku-outlet',
})
export class OutletDirective {
  constructor(public viewContainerRef: ViewContainerRef) {}
}
