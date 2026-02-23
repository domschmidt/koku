import { booleanAttribute, Component, input, signal } from '@angular/core';
import { NgComponentOutlet } from '@angular/common';
import { ButtonEvent, ButtonRegistrationType, FormularContentSetup } from '../formular.component';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicIoDirective } from 'ng-dynamic-component';
import { toObservable } from '@angular/core/rxjs-interop';
import { OutletDirective } from '../../portal/outlet.directive';
import { PortalDirective } from '../../portal/portal.directive';

@Component({
  selector: '[button-renderer],button-renderer',
  imports: [
    NgComponentOutlet,
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    PortalDirective,
  ],
  templateUrl: './button-renderer.component.html',
})
export class ButtonRendererComponent {
  inputBindings = signal<Record<string, any>>({});
  outputBindings = signal<Record<string, any>>({});
  content = input.required<KokuDto.KokuFormButton>();
  loading = input(false, { transform: booleanAttribute });
  submitting = input(false, { transform: booleanAttribute });

  buttonRegister = input.required<ButtonRegistrationType>();
  contentSetup = input.required<FormularContentSetup>();
  buttonDockOutlet = input<OutletDirective>();

  enableDockedOutput = signal(false);

  emitToButtonEventBus(eventName: ButtonEvent, data: any) {
    const contentSnapshot = this.content();
    if (contentSnapshot.id) {
      const fieldSnapshot = this.buttonRegister()[contentSnapshot.id];
      if (fieldSnapshot.buttonEventBus) {
        fieldSnapshot.buttonEventBus.next({ eventName, payload: data });
      }
    }
  }

  constructor() {
    toObservable(this.content).subscribe(() => {
      const contentSnapshot = this.content();
      if (contentSnapshot.dockable && this.buttonDockOutlet()) {
        this.enableDockedOutput.set(true);
      } else {
        this.enableDockedOutput.set(false);
      }
      const contentSetup = this.contentSetup().buttonRegistry[contentSnapshot['@type']];
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
