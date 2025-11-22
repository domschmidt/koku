import {Component, DestroyRef, inject, input, output} from '@angular/core';
import {ListContentSetup, ListInlineItem, ListItemSetup} from '../list.component';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';
import {HttpClient} from '@angular/common/http';
import {ModalService} from '../../modal/modal.service';
import {ToastService} from '../../toast/toast.service';
import {UNIQUE_REF_GENERATOR} from '../../utils/uniqueRef';

@Component({
  selector: '[list-item-action],list-item-action',
  imports: [
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './list-item-action.component.html',
  styleUrl: './list-item-action.component.css'
})
export class ListItemActionComponent {

  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);
  modalService = inject(ModalService);
  toastService = inject(ToastService);

  register = input.required<ListItemSetup>();
  listRegister = input.required<ListItemSetup[]>();
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<{ [key: string]: string } | null>(null);

  componentRef = UNIQUE_REF_GENERATOR.generate();

  onOpenInlineContent = output<ListInlineItem>();
  onOpenRoutedContent = output<string[]>();
  onReload = output<void>();

}
