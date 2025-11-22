import {Component, inject, input, OnDestroy, output, signal} from '@angular/core';
import {DockContentItem} from '../../../dock/dock.component';
import {toObservable} from '@angular/core/rxjs-interop';
import {ListInlineContentComponent} from '../../../list/list-inline-content/list-inline-content.component';
import {ListContentSetup} from '../../../list/list.component';
import {OutletDirective} from '../../../portal/outlet.directive';
import {get} from '../../../utils/get';
import {IconComponent} from '../../../icon/icon.component';
import {HttpClient} from '@angular/common/http';
import {GLOBAL_EVENT_BUS} from '../../../events/global-events';
import {UNIQUE_REF_GENERATOR} from '../../../utils/uniqueRef';


interface ExtendedDockContentItem extends DockContentItem {
  content: KokuDto.AbstractListViewContentDto;
  route?: string;
}

@Component({
  selector: '[list-inline-header-container],list-inline-header-container',
  imports: [
    ListInlineContentComponent,
    IconComponent,
    OutletDirective
  ],
  templateUrl: './list-header-container.component.html',
  styleUrl: './list-header-container.component.css'
})
export class ListHeaderContainerComponent implements OnDestroy {

  content = input.required<KokuDto.ListViewHeaderContentDto>();
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<{ [key: string]: string } | null>(null);
  sourceUrl = input<string>();
  titlePath = input<string>();
  title = input<string>();
  parentRoutePath = input<string>('');
  context = input<{ [key: string]: any }>();

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
