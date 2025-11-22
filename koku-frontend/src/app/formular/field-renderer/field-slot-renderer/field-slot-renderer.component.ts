import {Component, input, signal} from '@angular/core';
import {FieldEvent, FormularContentSetup, FormularFieldRegistrationType} from '../../formular.component';
import {toObservable} from '@angular/core/rxjs-interop';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';

@Component({
  selector: '[field-slot-renderer],field-slot-renderer',
  imports: [
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './field-slot-renderer.component.html',
  styleUrl: './field-slot-renderer.component.css'
})
export class FieldSlotRendererComponent {

  content = input.required<KokuDto.AbstractFormField<any>>();
  contentSlot = input.required<KokuDto.IFormFieldSlot>();
  loading = input<boolean>(false);
  submitting = input<boolean>(false);
  fieldRegister = input.required<FormularFieldRegistrationType>()

  contentSetup = input.required<FormularContentSetup>();
  slotName = input.required<string>();
  focusEventName = input.required<FieldEvent>();
  blurEventName = input.required<FieldEvent>();
  clickEventName = input.required<FieldEvent>();

  inputBindings = signal<{ [key: string]: any }>({});
  outputBindings = signal<{ [key: string]: any }>({});

  constructor() {
    toObservable(this.content).subscribe(() => {
      const contentSlotSnapshot = this.contentSlot();
      const contentSetup = this.contentSetup().fieldSlotRegistry[contentSlotSnapshot['@type']];
      if (contentSetup) {
        let inputBindings = {}
        if (contentSetup.inputBindings) {
          inputBindings = contentSetup.inputBindings(this, contentSlotSnapshot);
        }
        this.inputBindings.set(inputBindings);
        let outputBindings = {}
        if (contentSetup.outputBindings) {
          outputBindings = contentSetup.outputBindings(this, contentSlotSnapshot);
        }
        this.outputBindings.set(outputBindings);
      }
    });
  }

  emitToFieldEventBus(eventName: FieldEvent, data: any) {
    const contentSnapshot = this.content();
    if (!contentSnapshot.id) {
      throw new Error(`Missing id for content: ${contentSnapshot}`)
    }
    const fieldRegisterSnapshot = this.fieldRegister()[contentSnapshot.id];
    if (fieldRegisterSnapshot.fieldEventBus) {
      fieldRegisterSnapshot.fieldEventBus.next({
        eventName,
        payload: data
      });
    }
  }

}
