import { Component, input, signal } from '@angular/core';
import { FieldEvent, FormularContentSetup, FormularFieldRegistrationType } from '../formular.component';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective } from 'ng-dynamic-component';
import { toObservable } from '@angular/core/rxjs-interop';
import { FieldSlotRendererComponent } from './field-slot-renderer/field-slot-renderer.component';

@Component({
  selector: '[field-renderer],field-renderer',
  imports: [
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent,
    FieldSlotRendererComponent,
  ],
  templateUrl: './field-renderer.component.html',
})
export class FieldRendererComponent {
  content = input.required<KokuDto.AbstractFormField<any>>();
  loading = input<boolean>(false);
  submitting = input<boolean>(false);

  fieldRegister = input.required<FormularFieldRegistrationType>();

  inputBindings = signal<Record<string, any>>({});
  outputBindings = signal<Record<string, any>>({});

  contentSetup = input.required<FormularContentSetup>();
  context = input<Record<string, any>>();

  constructor() {
    const updateBindings = () => {
      const contentSnapshot = this.content();
      const contentSetup = this.contentSetup().fieldRegistry[contentSnapshot['@type']];
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
    };

    toObservable(this.content).subscribe(() => {
      updateBindings();
    });
  }

  emitToFieldEventBus(eventName: FieldEvent, data: any) {
    const contentSnapshot = this.content();
    if (contentSnapshot.id) {
      const fieldSnapshot = this.fieldRegister()[contentSnapshot.id];
      if (fieldSnapshot.fieldEventBus) {
        fieldSnapshot.fieldEventBus.next({ eventName, payload: data });
      }
    }
  }
}
