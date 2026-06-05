import { Component, input, output } from '@angular/core';
import { FormularComponent, FormularContentOverride, FormularSourceOverride } from '../../formular/formular.component';
import { OutletDirective } from '../../portal/outlet.directive';
import { FORMULAR_CONTENT_REGISTRY } from '../../formular-binding/registry';
import { executeInlineFormularSaveEvents } from '../../formular/inline-formular-save-events';

@Component({
  selector: '[formular-container],formular-container',
  imports: [FormularComponent],
  templateUrl: './calendar-inline-formular-container.component.html',
  styleUrl: './calendar-inline-formular-container.component.css',
})
export class CalendarInlineFormularContainerComponent {
  formularUrl = input.required<string>();
  sourceUrl = input<string>();
  submitUrl = input<string>();
  maxWidth = input<string | number>();
  submitMethod = input<string>();
  onSaveEvents = input<KokuDto.AbstractCalendarItemInlineFormularContentSaveEventDto[]>([]);
  contentOverrides = input<FormularContentOverride[]>([]);
  sourceOverrides = input<FormularSourceOverride[]>([]);
  buttonDockOutlet = input<OutletDirective>();

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

  protected readonly FORMULAR_CONTENT_REGISTRY = FORMULAR_CONTENT_REGISTRY;
}
