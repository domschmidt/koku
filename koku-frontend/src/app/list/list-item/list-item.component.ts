import {Component, input, signal} from '@angular/core';
import {ListContentSetup, ListItemSetup} from '../list.component';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';

export type ListFieldEvent = 'onClick' | 'onChange' | 'onInput' | 'onFocus' | 'onBlur' | 'onInit';
export type ListFieldEventBus = (id: string, eventName: ListFieldEvent, payload: any) => Promise<any> | void;

@Component({
  selector: '[list-item],list-item',
  imports: [
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './list-item.component.html',
  styleUrl: './list-item.component.css'
})
export class ListItemComponent {

  register = input.required<ListItemSetup>()

  submitting = signal(false);
  contentSetup = input.required<ListContentSetup>();

  fieldEventBus: ListFieldEventBus = (key: string, eventName: string, payload: any) => {
    if (eventName === 'onInit') {
      this.register().fields[key].instance = payload;
    }
    if (eventName === 'onChange') {
      this.register().fields[key].value = payload;
    }
  }

}
