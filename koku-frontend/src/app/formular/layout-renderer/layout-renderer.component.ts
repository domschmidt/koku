import { Component, input, signal } from '@angular/core';
import { NgComponentOutlet } from '@angular/common';
import { FormularContentSetup, LayoutRegistrationType } from '../formular.component';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicIoDirective } from 'ng-dynamic-component';
import { toObservable } from '@angular/core/rxjs-interop';

@Component({
  selector: '[layout-renderer],layout-renderer',
  imports: [NgComponentOutlet, SignalComponentIoModule, DynamicIoDirective, ComponentOutletInjectorModule],
  templateUrl: './layout-renderer.component.html',
})
export class LayoutRendererComponent {
  content = input.required<KokuDto.AbstractFormLayout>();
  loading = input<boolean>(false);
  submitting = input<boolean>(false);

  layoutRegister = input.required<LayoutRegistrationType>();

  inputBindings = signal<Record<string, any>>({});
  outputBindings = signal<Record<string, any>>({});

  contentSetup = input.required<FormularContentSetup>();

  constructor() {
    toObservable(this.content).subscribe(() => {
      const contentSnapshot = this.content();
      const contentSetup = this.contentSetup().layoutRegistry[contentSnapshot['@type']];
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
