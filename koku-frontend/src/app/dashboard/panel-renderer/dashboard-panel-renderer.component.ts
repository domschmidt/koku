import { Component, input, signal } from '@angular/core';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective } from 'ng-dynamic-component';
import { toObservable } from '@angular/core/rxjs-interop';
import { DashboardContentSetup } from '../dashboard.component';

@Component({
  selector: '[dashboard-panel-renderer],dashboard-panel-renderer',
  imports: [SignalComponentIoModule, DynamicIoDirective, ComponentOutletInjectorModule, DynamicComponent],
  templateUrl: './dashboard-panel-renderer.component.html',
})
export class DashboardPanelRendererComponent {
  content = input.required<KokuDto.AbstractDashboardPanel>();

  inputBindings = signal<Record<string, any>>({});
  outputBindings = signal<Record<string, any>>({});

  contentSetup = input.required<DashboardContentSetup>();

  constructor() {
    const updateBindings = () => {
      const contentSnapshot = this.content();
      const contentSetup = this.contentSetup().panelRegistry[contentSnapshot['@type']];
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
}
