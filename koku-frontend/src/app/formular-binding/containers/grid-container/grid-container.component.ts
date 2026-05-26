import { booleanAttribute, Component, forwardRef, input } from '@angular/core';
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

@Component({
  selector: '[grid-container],grid-container',
  imports: [
    forwardRef(() => ContainerRendererComponent),
    FieldRendererComponent,
    ButtonRendererComponent,
    LayoutRendererComponent,
  ],
  templateUrl: './grid-container.component.html',
})
export class GridContainerComponent {
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });
  layoutRegister = input.required<LayoutRegistrationType>();
  fieldRegister = input.required<FormularFieldRegistrationType>();
  buttonRegister = input.required<ButtonRegistrationType>();
  containerRegister = input.required<ContainerRegistrationType>();
  contentSetup = input.required<FormularContentSetup>();
  content = input.required<KokuDto.GridContainer>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();
  source = input<any>();

  getFieldConfig(id: string | undefined) {
    if (!id) {
      throw new Error('Missing field content id');
    }
    const registration = this.fieldRegister()[id];
    if (!registration) {
      throw new Error(`Field registration not found for '${id}'`);
    }
    return registration.config;
  }

  getButtonConfig(id: string | undefined) {
    if (!id) {
      throw new Error('Missing button content id');
    }
    const registration = this.buttonRegister()[id];
    if (!registration) {
      throw new Error(`Button registration not found for '${id}'`);
    }
    return registration.config;
  }
}
