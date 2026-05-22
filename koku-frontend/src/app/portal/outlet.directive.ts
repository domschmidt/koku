import { Directive, inject, ViewContainerRef } from '@angular/core';

@Directive({
  standalone: true,
  selector: '[koku-outlet]',
  exportAs: 'koku-outlet',
})
export class OutletDirective {
  viewContainerRef = inject(ViewContainerRef);
}
