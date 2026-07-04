import { Component, input, output } from '@angular/core';
import {
  FormularComponent,
  FormularContentRegistry,
  FormularContentOverride,
} from '../../../formular/formular.component';
import { OutletDirective } from '../../../portal/outlet.directive';
import { executeInlineFormularSaveEvents } from '../../../formular/inline-formular-save-events';

@Component({
  selector: '[list-inline-formular-container],list-inline-formular-container',
  host: { class: 'flex w-full flex-col overflow-auto' },
  imports: [FormularComponent],
  templateUrl: './list-formular-container.component.html',
})
export class ListFormularContainerComponent {
  formularUrl = input.required<string>();
  sourceUrl = input<string>();
  urlSegments = input<Record<string, string> | null>(null);
  submitUrl = input<string>();
  maxWidth = input<string | number>();
  submitMethod = input<string>();
  onSaveEvents = input<KokuDto.AbstractListViewItemInlineFormularContentSaveEventDto[]>([]);
  contentOverrides = input<FormularContentOverride[]>([]);
  contentRegistry = input.required<FormularContentRegistry>();
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  closeInlineContent() {
    this.closeRequested.emit();
  }

  onFormularSave(payload: any) {
    executeInlineFormularSaveEvents(this.onSaveEvents(), payload, {
      openRoutedContent: (routes) => this.openRoutedContentRequested.emit(routes),
    });
  }
}
