import { booleanAttribute, Component, forwardRef, input } from '@angular/core';
import { FormularContentRegistry, FormularRuntime } from '../../../formular/formular.component';
import { FormularContentRendererComponent } from '../../../formular/content-renderer/formular-content-renderer.component';
import { OutletDirective } from '../../../portal/outlet.directive';
import { FORM_OUTLET } from '../../../formular/form-outlet';

@Component({
  selector: '[fieldset-container],fieldset-container',
  imports: [forwardRef(() => FormularContentRendererComponent)],
  templateUrl: './fieldset-container.component.html',
})
export class FieldsetContainerComponent {
  readonly outlets = FORM_OUTLET;
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });
  runtime = input.required<FormularRuntime>();
  contentRegistry = input.required<FormularContentRegistry>();
  content = input.required<KokuDto.FieldsetContainer>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();
}
