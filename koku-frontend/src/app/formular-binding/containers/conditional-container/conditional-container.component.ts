import { booleanAttribute, Component, computed, forwardRef, input } from '@angular/core';
import { FormularContentRegistry, FormularRuntime } from '../../../formular/formular.component';
import { FormularContentRendererComponent } from '../../../formular/content-renderer/formular-content-renderer.component';
import { OutletDirective } from '../../../portal/outlet.directive';
import { FORM_OUTLET } from '../../../formular/form-outlet';

@Component({
  selector: '[conditional-container],conditional-container',
  imports: [forwardRef(() => FormularContentRendererComponent)],
  templateUrl: './conditional-container.component.html',
  host: {
    '[class.hidden]': '!matchesPositively()',
  },
})
export class ConditionalContainerComponent {
  readonly outlets = FORM_OUTLET;
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });
  runtime = input.required<FormularRuntime>();
  contentRegistry = input.required<FormularContentRegistry>();
  content = input.required<KokuDto.ConditionalContainer>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();
  matchesPositively = computed(() => {
    const contentSnapshot = this.content();
    const currentValue = this.runtime().sourceValue(contentSnapshot.compareValuePath, null);
    return (contentSnapshot.expectedValues || []).some(
      (currentPositiveCompareValue) => currentValue == currentPositiveCompareValue,
    );
  });
}
