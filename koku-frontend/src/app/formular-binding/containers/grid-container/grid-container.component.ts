import { booleanAttribute, Component, forwardRef, input } from '@angular/core';
import { FormularContentRegistry, FormularRuntime } from '../../../formular/formular.component';
import { FormularContentRendererComponent } from '../../../formular/content-renderer/formular-content-renderer.component';
import { OutletDirective } from '../../../portal/outlet.directive';
import { FORM_OUTLET } from '../../../formular/form-outlet';

@Component({
  selector: '[grid-container],grid-container',
  imports: [forwardRef(() => FormularContentRendererComponent)],
  templateUrl: './grid-container.component.html',
})
export class GridContainerComponent {
  readonly outlets = FORM_OUTLET;
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });
  runtime = input.required<FormularRuntime>();
  contentRegistry = input.required<FormularContentRegistry>();
  content = input.required<KokuDto.GridContainer>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();
}
