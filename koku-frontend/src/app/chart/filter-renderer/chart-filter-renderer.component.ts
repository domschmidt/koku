import { Component, input, output, signal } from '@angular/core';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective } from 'ng-dynamic-component';
import { toObservable } from '@angular/core/rxjs-interop';
import { ChartContentSetup } from '../chart.component';

@Component({
  selector: '[chart-filter-renderer],chart-filter-renderer',
  imports: [SignalComponentIoModule, DynamicIoDirective, ComponentOutletInjectorModule, DynamicComponent],
  templateUrl: './chart-filter-renderer.component.html',
})
export class ChartFilterRendererComponent {
  content = input.required<KokuDto.AbstractChartFilterDto>();
  loading = input<boolean>(false);

  inputBindings = signal<Record<string, any>>({});
  outputBindings = signal<Record<string, any>>({});

  contentSetup = input.required<ChartContentSetup>();

  filterValueChanged = output<string | number | boolean>();

  constructor() {
    toObservable(this.content).subscribe(() => {
      const contentSnapshot = this.content();
      const contentSetup = this.contentSetup().filterRegistry[contentSnapshot['@type']];
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
