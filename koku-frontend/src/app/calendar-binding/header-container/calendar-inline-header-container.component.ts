import {Component, inject, input, OnDestroy, output, signal} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {CalendarInlineContentComponent} from '../calendar-inline-content/calendar-inline-content.component';
import {IconComponent} from '../../icon/icon.component';
import {OutletDirective} from '../../portal/outlet.directive';
import {CalendarContentSetup} from '../../calendar/calendar.component';
import {toObservable} from '@angular/core/rxjs-interop';
import {get} from '../../utils/get';
import {GLOBAL_EVENT_BUS} from '../../events/global-events';
import {UNIQUE_REF_GENERATOR} from '../../utils/uniqueRef';


@Component({
  selector: '[calendar-inline-header-container],calendar-inline-header-container',
  imports: [
    CalendarInlineContentComponent,
    IconComponent,
    OutletDirective
  ],
  templateUrl: './calendar-inline-header-container.component.html',
  styleUrl: './calendar-inline-header-container.component.css'
})
export class CalendarInlineHeaderContainerComponent implements OnDestroy {
  content = input.required<KokuDto.CalendarHeaderInlineContentDto>();
  contentSetup = input.required<CalendarContentSetup>();
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

  closeInlineContent() {
    this.onClose.emit();
  }

  openRoutedContent(routes: string[]) {
    this.onOpenRoutedContent.emit(routes);
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }

}
