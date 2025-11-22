import {Component, input, output} from '@angular/core';
import {FormularComponent, FormularFieldOverride, FormularSourceOverride} from '../../formular/formular.component';
import {OutletDirective} from '../../portal/outlet.directive';
import {GLOBAL_EVENT_BUS} from '../../events/global-events';
import {get} from '../../utils/get';
import {DashboardContentSetup} from '../../dashboard/dashboard.component';
import {FORMULAR_CONTENT_SETUP} from '../../formular-binding/registry';

@Component({
  selector: '[formular-container],formular-container',
  imports: [
    FormularComponent,

  ],
  templateUrl: './calendar-inline-formular-container.component.html',
  styleUrl: './calendar-inline-formular-container.component.css'
})
export class CalendarInlineFormularContainerComponent {

  formularUrl = input.required<string>();
  sourceUrl = input<string>();
  submitUrl = input<string>();
  maxWidth = input<string | number>();
  submitMethod = input<string>();
  onSaveEvents = input<KokuDto.AbstractListViewItemInlineFormularContentSaveEventDto[]>([]);
  fieldOverrides = input<FormularFieldOverride[]>([]);
  sourceOverrides = input<FormularSourceOverride[]>([]);
  contentSetup = input.required<DashboardContentSetup>();
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
        case "propagate-global-event": {
          const castedEventJob = currentSaveEventJob as KokuDto.ListViewInlineFormularContentAfterSavePropagateGlobalEventDto;
          if (!castedEventJob.eventName) {
            throw new Error(`Missing eventName in saveEvent`);
          }
          GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEventJob.eventName, payload);
          break;
        }
        case "open-routed-inline-formular": {
          const castedEventJob = currentSaveEventJob as KokuDto.ListViewOpenRoutedInlineFormularContentSaveEventDto;
          const paramReplacementMapping: { [key: string]: string } = {};
          for (const currentParamReplacementInfo of castedEventJob.params || []) {
            switch (currentParamReplacementInfo['@type']) {
              case "event-payload": {
                const castedReplacementInfo = currentParamReplacementInfo as KokuDto.ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto;
                if (castedReplacementInfo.param !== undefined && castedReplacementInfo.valuePath !== undefined) {
                  const valueRawOrNull = get(payload, castedReplacementInfo.valuePath, null);
                  if (valueRawOrNull !== null) {
                    paramReplacementMapping[castedReplacementInfo.param] = valueRawOrNull;
                  }
                }
                break;
              }
            }
          }

          const replacedRouteParts: string[] = [];
          if (castedEventJob.route) {
            for (const currentRouteRaw of castedEventJob.route?.split('/')) {
              const replaceMapping = paramReplacementMapping[currentRouteRaw];
              if (replaceMapping !== undefined) {
                replacedRouteParts.push(replaceMapping);
              } else {
                replacedRouteParts.push(currentRouteRaw);
              }
            }
          }

          this.onOpenRoutedContent.emit(replacedRouteParts);

          break;
        }
        default: {
          throw new Error(`Unknown onSave event type ${currentSaveEventJob['@type']}`);
        }
      }
    }
  }

  protected readonly FORMULAR_CONTENT_SETUP = FORMULAR_CONTENT_SETUP;
}
