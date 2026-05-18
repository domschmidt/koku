import { booleanAttribute, Component, effect, forwardRef, input, signal } from '@angular/core';
import {
  ButtonRegistrationType,
  ContainerRegistrationType,
  FormularContentSetup,
  FormularFieldRegistrationType,
  LayoutRegistrationType,
} from '../../../formular/formular.component';
import { LayoutRendererComponent } from '../../../formular/layout-renderer/layout-renderer.component';
import { ButtonRendererComponent } from '../../../formular/button-renderer/button-renderer.component';
import { FieldRendererComponent } from '../../../formular/field-renderer/field-renderer.component';
import { ContainerRendererComponent } from '../../../formular/container-renderer/container-renderer.component';
import { OutletDirective } from '../../../portal/outlet.directive';
import { get } from '../../../utils/get';

@Component({
  selector: '[conditional-container],conditional-container',
  imports: [
    forwardRef(() => ContainerRendererComponent),
    FieldRendererComponent,
    ButtonRendererComponent,
    LayoutRendererComponent,
  ],
  templateUrl: './conditional-container.component.html',
  host: {
    '[class.hidden]': '!matchesPositively()',
  },
})
export class ConditionalContainerComponent {
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });
  layoutRegister = input.required<LayoutRegistrationType>();
  fieldRegister = input.required<FormularFieldRegistrationType>();
  buttonRegister = input.required<ButtonRegistrationType>();
  containerRegister = input.required<ContainerRegistrationType>();
  contentSetup = input.required<FormularContentSetup>();
  content = input.required<KokuDto.ConditionalContainer>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();
  source = input<any>();
  matchesPositively = signal<boolean | null>(null);

  private sourceEffect = effect(() => {
    const contentSnapshot = this.content();
    const source = this.source();
    let result = false;
    const currentValue = get(source, contentSnapshot.compareValuePath || '', null);
    for (const currentPositiveCompareValue of contentSnapshot.expectedValues || []) {
      if (currentValue == currentPositiveCompareValue) {
        result = true;
        break;
      }
    }
    this.matchesPositively.set(result);
  });
}
