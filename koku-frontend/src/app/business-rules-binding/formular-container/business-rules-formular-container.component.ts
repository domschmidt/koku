import { Component, input, output } from '@angular/core';
import { FormularComponent, FormularContentSetup, FormularFieldOverride } from '../../formular/formular.component';
import { OutletDirective } from '../../portal/outlet.directive';
import { GLOBAL_EVENT_BUS } from '../../events/global-events';

@Component({
  selector: '[business-rules-formular-container],business-rules-formular-container',
  imports: [FormularComponent],
  templateUrl: './business-rules-formular-container.component.html',
  styleUrl: './business-rules-formular-container.component.css',
})
export class BusinessRulesFormularContainerComponent {
  formularUrl = input.required<string>();
  sourceUrl = input<string>();
  submitUrl = input<string>();
  maxWidth = input<string | number>();
  submitMethod = input<string>();
  onSaveEvents = input<KokuDto.AbstractKokuBusinessRuleFormularContentSaveEventDto[]>([]);
  fieldOverrides = input<FormularFieldOverride[]>([]);
  contentSetup = input.required<FormularContentSetup>();
  buttonDockOutlet = input<OutletDirective>();

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  closeInlineContent() {
    this.onClose.emit();
  }

  onFormularSave(payload: any) {
    const onSaveSnapshot = this.onSaveEvents();
    for (const currentSaveEventJob of onSaveSnapshot || []) {
      switch (currentSaveEventJob['@type']) {
        case 'propagate-global-event': {
          const castedEventJob =
            currentSaveEventJob as KokuDto.KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto;
          if (!castedEventJob.eventName) {
            throw new Error(`Missing eventName in saveEvent`);
          }
          GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEventJob.eventName, payload);
          break;
        }
        default: {
          throw new Error(`Unknown onSave event type ${currentSaveEventJob['@type']}`);
        }
      }
    }
  }
}
