import {Component, input, signal} from '@angular/core';
import {NgComponentOutlet} from '@angular/common';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule} from 'ng-dynamic-component';
import {DashboardContentSetup} from '../dashboard.component';
import {toObservable} from '@angular/core/rxjs-interop';


@Component({
  selector: '[dashboard-container-renderer],dashboard-container-renderer',
  imports: [
    NgComponentOutlet,
    SignalComponentIoModule,
    ComponentOutletInjectorModule
  ],
  templateUrl: './dashboard-container-renderer.component.html',
  styleUrl: './dashboard-container-renderer.component.css'
})
export class DashboardContainerRendererComponent {

  content = input.required<KokuDto.AbstractDashboardContainer>();
  contentSetup = input.required<DashboardContentSetup>();

  inputBindings = signal<{ [key: string]: any }>({});
  outputBindings = signal<{ [key: string]: any }>({});

  constructor() {
    toObservable(this.content).subscribe(() => {
      const contentSnapshot = this.content();
      const contentSetup = this.contentSetup().containerRegistry[contentSnapshot['@type']];
      if (contentSetup) {
        let inputBindings = {}
        if (contentSetup.inputBindings) {
          inputBindings = contentSetup.inputBindings(this, contentSnapshot);
        }
        this.inputBindings.set(inputBindings);
        let outputBindings = {}
        if (contentSetup.outputBindings) {
          outputBindings = contentSetup.outputBindings(this, contentSnapshot);
        }
        this.outputBindings.set(outputBindings);
      }
    });
  }

}
