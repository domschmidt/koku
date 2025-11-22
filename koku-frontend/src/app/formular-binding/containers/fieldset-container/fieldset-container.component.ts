import {booleanAttribute, Component, forwardRef, input} from '@angular/core';
import {
  ButtonRegistrationType,
  ContainerRegistrationType,
  FormularContentSetup,
  FormularFieldRegistrationType,
  LayoutRegistrationType
} from '../../../formular/formular.component';
import {LayoutRendererComponent} from '../../../formular/layout-renderer/layout-renderer.component';
import {ButtonRendererComponent} from '../../../formular/button-renderer/button-renderer.component';
import {FieldRendererComponent} from '../../../formular/field-renderer/field-renderer.component';
import {ContainerRendererComponent} from '../../../formular/container-renderer/container-renderer.component';
import {OutletDirective} from '../../../portal/outlet.directive';

@Component({
  selector: '[fieldset-container],fieldset-container',
  imports: [
    forwardRef(() => ContainerRendererComponent),
    FieldRendererComponent,
    ButtonRendererComponent,
    LayoutRendererComponent,
  ],
  templateUrl: './fieldset-container.component.html',
  styleUrl: './fieldset-container.component.css'
})
export class FieldsetContainerComponent {

  loading = input(false, {transform: booleanAttribute});
  submitting = input(false, {transform: booleanAttribute});
  layoutRegister = input.required<LayoutRegistrationType>()
  fieldRegister = input.required<FormularFieldRegistrationType>()
  buttonRegister = input.required<ButtonRegistrationType>()
  containerRegister = input.required<ContainerRegistrationType>()
  contentSetup = input.required<FormularContentSetup>();
  content = input.required<KokuDto.FieldsetContainer>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<{ [key: string]: any }>();

}
