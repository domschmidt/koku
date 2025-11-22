import {Component, inject, input, OnDestroy, output, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {IconComponent} from '../../icon/icon.component';
import {OutletDirective} from '../../portal/outlet.directive';
import {toObservable} from '@angular/core/rxjs-interop';
import {get} from '../../utils/get';
import {GLOBAL_EVENT_BUS} from '../../events/global-events';
import {UNIQUE_REF_GENERATOR} from '../../utils/uniqueRef';
import {BusinessRulesContentRegistry} from '../registry';
import {BusinessRulesContentComponent} from '../business-rules-content/business-rules-content.component';


@Component({
  selector: '[business-rules-header-container],business-rules-header-container',
  imports: [
    IconComponent,
    OutletDirective,
    BusinessRulesContentComponent
  ],
  templateUrl: './business-rules-header-container.component.html',
  styleUrl: './business-rules-header-container.component.css'
})
export class BusinessRulesHeaderContainerComponent implements OnDestroy {
  content = input.required<KokuDto.KokuBusinessRuleHeaderContentDto>();
  contentSetup = input.required<BusinessRulesContentRegistry>();
  urlSegments = input<{ [key: string]: string } | null>(null);
  sourceUrl = input<string>();
  titlePath = input<string>();
  title = input<string>();
  parentRoutePath = input<string>('');

  loadedTitle = signal<string | null>(null);

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  httpClient = inject(HttpClient);

  componentRef = UNIQUE_REF_GENERATOR.generate();

  constructor() {
    toObservable(this.sourceUrl).subscribe((sourceUrl) => {
      if (sourceUrl) {
        this.httpClient.get(sourceUrl).subscribe((detailSource) => {
          const titlePath = this.titlePath();
          if (titlePath) {
            this.loadedTitle.set(get(detailSource, titlePath));
          } else {
            this.loadedTitle.set(null);
          }
        });
      } else {
        this.loadedTitle.set(null);
      }
    });
    toObservable(this.content).subscribe((content) => {
      this.clearGlobalEventListeners();
      for (const currentEventListener of content.globalEventListeners || []) {
        if (!currentEventListener.eventName) {
          throw new Error('Missing eventName in Global Listener Configuration');
        }
        GLOBAL_EVENT_BUS.addGlobalEventListener(String(this.componentRef), currentEventListener.eventName, (eventPayload) => {
          switch (currentEventListener['@type']) {
            case "event-payload": {
              const castedEventListener = currentEventListener as KokuDto.ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto;

              if (!castedEventListener.idPath) {
                throw new Error('Missing idPath configuration in EventListener');
              }

              if (castedEventListener.titleValuePath) {
                this.loadedTitle.set(String(get(eventPayload, castedEventListener.titleValuePath, '')));
              }
              break;
            }
            default: {
              throw new Error(`Unknown EventListenerType ${currentEventListener['@type']}`);
            }
          }
        });
      }
    });
  }

  ngOnDestroy(): void {
    this.clearGlobalEventListeners();
  }

  closeContent() {
    this.onClose.emit();
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }

}
