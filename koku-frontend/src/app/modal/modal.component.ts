import {Component, inject} from '@angular/core';
import {ModalService} from './modal.service';
import {ButtonComponent} from '../button/button.component';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';

@Component({
  selector: 'koku-modal',
  imports: [
    ButtonComponent,
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css'
})
export class ModalComponent {
  readonly modalService = inject(ModalService);
}
