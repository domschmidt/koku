import {Component, input} from '@angular/core';
import {ListContentSetup, ListItemSetup} from '../list.component';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';
import {get} from '../../utils/get';


@Component({
  selector: '[list-item-preview],list-item-preview',
  imports: [
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './list-item-preview.component.html',
  styleUrl: './list-item-preview.component.css'
})
export class ListItemPreviewComponent {

  register = input.required<ListItemSetup>()
  contentSetup = input.required<ListContentSetup>();

  get = get;
}
