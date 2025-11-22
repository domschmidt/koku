import {booleanAttribute, Component, input, output} from '@angular/core';
import {ListContentSetup} from '../list.component';
import {SignalComponentIoModule} from 'ng-dynamic-component/signal-component-io';
import {ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective} from 'ng-dynamic-component';
import {OutletDirective} from '../../portal/outlet.directive';

@Component({
  selector: '[list-inline-content],list-inline-content',
  imports: [
    SignalComponentIoModule,
    DynamicIoDirective,
    ComponentOutletInjectorModule,
    DynamicComponent
  ],
  templateUrl: './list-inline-content.component.html',
  styleUrl: './list-inline-content.component.css'
})
export class ListInlineContentComponent {

  content = input.required<KokuDto.AbstractListViewContentDto>();
  loading = input(false, {transform: booleanAttribute});
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<{ [key: string]: string } | null>(null);
  parentRoutePath = input<string>('');
  buttonDockOutlet = input<OutletDirective>();
  context = input<{ [key: string]: any }>();

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  closeInlineContent() {
    this.onClose.emit();
  }

  openRoutedContent(routes: string[]) {
    this.onOpenRoutedContent.emit(routes);
  }

}
