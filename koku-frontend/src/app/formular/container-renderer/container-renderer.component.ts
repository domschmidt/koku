import { booleanAttribute, Component, input, signal } from '@angular/core';
import { NgComponentOutlet } from '@angular/common';
import {
  ButtonRegistrationType,
  ContainerRegistrationType,
  FormularContentSetup,
  FormularFieldRegistrationType,
  LayoutRegistrationType,
} from '../formular.component';
import { ComponentOutletInjectorModule } from 'ng-dynamic-component';
import { toObservable } from '@angular/core/rxjs-interop';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { OutletDirective } from '../../portal/outlet.directive';

@Component({
  selector: '[container-renderer],container-renderer',
  imports: [NgComponentOutlet, SignalComponentIoModule, ComponentOutletInjectorModule],
  templateUrl: './container-renderer.component.html',
})
export class ContainerRendererComponent {
  inputBindings = signal<Record<string, any>>({});
  outputBindings = signal<Record<string, any>>({});

  content = input.required<KokuDto.AbstractFormContainer>();
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });

  fieldRegister = input.required<FormularFieldRegistrationType>();
  buttonRegister = input.required<ButtonRegistrationType>();
  containerRegister = input.required<ContainerRegistrationType>();
  layoutRegister = input.required<LayoutRegistrationType>();
  contentSetup = input.required<FormularContentSetup>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();

  constructor() {
    toObservable(this.content).subscribe(() => {
      const contentSnapshot = this.content();
      const contentSetup = this.contentSetup().containerRegistry[contentSnapshot['@type']];
      if (contentSetup) {
        let inputBindings = {};
        if (contentSetup.inputBindings) {
          inputBindings = contentSetup.inputBindings(this, contentSnapshot);
        }
        this.inputBindings.set(inputBindings);
        let outputBindings = {};
        if (contentSetup.outputBindings) {
          outputBindings = contentSetup.outputBindings(this, contentSnapshot);
        }
        this.outputBindings.set(outputBindings);
      }
    });
  }
}
