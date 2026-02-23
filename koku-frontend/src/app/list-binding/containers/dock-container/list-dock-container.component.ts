import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { DockComponent, DockContentItem } from '../../../dock/dock.component';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ListInlineContentComponent } from '../../../list/list-inline-content/list-inline-content.component';
import { ListContentSetup } from '../../../list/list.component';
import { NavigationEnd, Router } from '@angular/router';
import { OutletDirective } from '../../../portal/outlet.directive';
import { HttpClient } from '@angular/common/http';
import { get } from '../../../utils/get';
import { Subscription } from 'rxjs';

interface ExtendedDockContentItem extends DockContentItem {
  content: KokuDto.AbstractListViewContentDto;
  route?: string;
}

interface ExtendedCalendarInlineDockContentItemDto extends KokuDto.ListViewItemInlineDockContentItemDto {
  parentRoutePath?: string;
}

@Component({
  selector: '[list-inline-dock-container],list-inline-dock-container',
  imports: [DockComponent, ListInlineContentComponent],
  templateUrl: './list-dock-container.component.html',
  styleUrl: './list-dock-container.component.css',
})
export class ListDockContainerComponent {
  content = input.required<KokuDto.ListViewItemInlineDockContentItemDto[]>();
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<Record<string, string> | null>(null);
  parentRoutePath = input<string>('');
  buttonDockOutlet = input<OutletDirective>();
  sourceUrl = input<string>();
  titlePath = input<string>();
  context = input<Record<string, any>>();

  dockConfig = signal<ExtendedDockContentItem[]>([]);
  activeContent = signal<ExtendedCalendarInlineDockContentItemDto | null>(null);
  title = signal<string | null>(null);

  httpClient = inject(HttpClient);
  router = inject(Router);
  destroyRef = inject(DestroyRef);

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  private dockContentIndex: Record<string, ExtendedDockContentItem> = {};
  private routerUrlSubscription: Subscription | undefined;

  constructor() {
    toObservable(this.sourceUrl).subscribe((sourceUrl) => {
      if (sourceUrl) {
        this.httpClient.get(sourceUrl).subscribe((detailSource) => {
          const titlePath = this.titlePath();
          if (titlePath) {
            this.title.set(get(detailSource, titlePath));
          } else {
            this.title.set(null);
          }
        });
      } else {
        this.title.set(null);
      }
    });
    toObservable(this.content).subscribe((content) => {
      const newTransformedContent: ExtendedDockContentItem[] = [];
      const newDockContentIndex: Record<string, ExtendedDockContentItem> = {};
      for (const currentContent of content || []) {
        if (currentContent.id !== undefined && currentContent.content !== undefined) {
          const newDockContent: ExtendedDockContentItem = {
            active: false,
            id: currentContent.id,
            content: currentContent.content,
            icon: currentContent.icon,
            title: currentContent.title,
            route: currentContent.route,
          };
          newTransformedContent.push(newDockContent);
          newDockContentIndex[currentContent.id] = newDockContent;
        }
      }
      this.dockContentIndex = newDockContentIndex;
      this.dockConfig.set(newTransformedContent);

      if (this.routerUrlSubscription) {
        this.routerUrlSubscription.unsubscribe();
      }

      const afterNavigationUrlChange = () => {
        const segments = this.router.url
          .split('/')
          .filter((value) => value !== '')
          .slice(
            this.parentRoutePath()
              .split('/')
              .filter((value) => value !== '').length,
          );
        let newActiveContentFound = false;
        for (const currentRoutedContent of content || []) {
          if (currentRoutedContent.route) {
            let segmentIdx = 0;
            const segmentMapping: Record<string, string> = {};
            let failedLookup = false;
            for (const currentRoutePathToMatch of currentRoutedContent.route.split('/')) {
              const currentSegment = segments[segmentIdx++];
              if (!currentSegment) {
                failedLookup = true;
                break;
              }
              const currentSegmentPath = currentSegment;
              if (currentRoutePathToMatch.indexOf(':') === 0) {
                if (!segmentMapping[currentRoutePathToMatch]) {
                  segmentMapping[currentRoutePathToMatch] = currentSegmentPath;
                }
              } else if (currentRoutePathToMatch !== currentSegmentPath) {
                failedLookup = true;
                break;
              }
            }
            if (!failedLookup) {
              if (currentRoutedContent.id) {
                const dockConfigSnapshot = this.dockConfig();
                for (const currentDockConfig of dockConfigSnapshot) {
                  currentDockConfig.active = currentRoutedContent.id == currentDockConfig.id;
                }
                this.dockConfig.set(dockConfigSnapshot);
              }
              this.activeContent.set({
                ...currentRoutedContent,
                parentRoutePath: [
                  ...(this.parentRoutePath() + '/' + currentRoutedContent.route).split('/').map(
                    (value) =>
                      ({
                        ...segmentMapping,
                        ...this.urlSegments(),
                      })[value] || value,
                  ),
                ]
                  .filter((value) => value !== '')
                  .join('/'),
              });
              newActiveContentFound = true;
              break;
            }
          }
        }
        if (!newActiveContentFound) {
          let firstEntry: ExtendedCalendarInlineDockContentItemDto | null = null;
          const firstEntryRaw = (content || [])[0];
          if (firstEntryRaw) {
            firstEntry = {
              ...firstEntryRaw,
              parentRoutePath: [
                ...(this.parentRoutePath() + '/' + firstEntryRaw.route).split('/').map(
                  (value) =>
                    ({
                      ...this.urlSegments(),
                    })[value] || value,
                ),
              ]
                .filter((value) => value !== '')
                .join('/'),
            };
          }
          this.activeContent.set(firstEntry);
        }
      };

      this.routerUrlSubscription = this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((evnt) => {
        if (evnt instanceof NavigationEnd) {
          afterNavigationUrlChange();
        }
      });
      afterNavigationUrlChange();
    });
  }

  closeInlineContent() {
    this.onClose.emit();
  }

  openRoutedContent(routes: string[]) {
    this.onOpenRoutedContent.emit(routes);
  }

  onDockContentActivated(activatedDockContent: DockContentItem) {
    const afterContentActivated = () => {
      const dockConfigSnapshot = this.dockConfig();
      for (const currentDockConfig of dockConfigSnapshot) {
        currentDockConfig.active = activatedDockContent.id == currentDockConfig.id;
      }
      this.dockConfig.set(dockConfigSnapshot);
    };

    const contentLookup = this.dockContentIndex[activatedDockContent.id];
    if (contentLookup) {
      if (contentLookup.route !== undefined) {
        const routeParts: string[] = [];
        for (const currentRoutePart of contentLookup.route.split('/')) {
          let replacedRoutePart = currentRoutePart;
          for (const [segment, value] of Object.entries(this.urlSegments() || {})) {
            replacedRoutePart = replacedRoutePart.replace(segment, value);
          }
          routeParts.push(replacedRoutePart);
        }
        this.router.navigate([...this.parentRoutePath().split('/'), ...routeParts]).then((success) => {
          if (success) {
            afterContentActivated();
          }
        });
      } else {
        this.activeContent.set(contentLookup);
        afterContentActivated();
      }
    }
  }
}
