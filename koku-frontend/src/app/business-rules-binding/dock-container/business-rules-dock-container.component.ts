import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { DockComponent, DockContentItem } from '../../dock/dock.component';
import { OutletDirective } from '../../portal/outlet.directive';
import { get } from '../../utils/get';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { BusinessRulesContentRegistry } from '../registry';
import { BusinessRulesContentComponent } from '../business-rules-content/business-rules-content.component';

interface ExtendedDockContentItem extends DockContentItem {
  content: KokuDto.AbstractKokuBusinessRuleContentDto;
  route?: string;
}

interface ExtendedKokuBusinessRuleDockContentItemDto extends KokuDto.KokuBusinessRuleDockContentItemDto {
  parentRoutePath?: string;
}

@Component({
  selector: '[calendar-dock-container],calendar-dock-container',
  imports: [DockComponent, BusinessRulesContentComponent],
  templateUrl: './business-rules-dock-container.component.html',
  styleUrl: './business-rules-dock-container.component.css',
})
export class BusinessRulesDockContainerComponent {
  content = input.required<KokuDto.KokuBusinessRuleDockContentItemDto[]>();
  contentSetup = input.required<BusinessRulesContentRegistry>();
  urlSegments = input<Record<string, string> | null>(null);
  buttonDockOutlet = input<OutletDirective>();
  sourceUrl = input<string>();
  titlePath = input<string>();
  parentRoutePath = input<string>('');

  dockConfig = signal<ExtendedDockContentItem[]>([]);
  activeContent = signal<ExtendedKokuBusinessRuleDockContentItemDto | null>(null);
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
          .split('?')[0]
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
          let firstEntry: ExtendedKokuBusinessRuleDockContentItemDto | null = null;
          const firstEntryRaw = (content || [])[0];
          if (firstEntryRaw) {
            const segmentMapping: Record<string, string> = { ...(this.urlSegments() || {}) };
            firstEntry = {
              ...firstEntryRaw,
              parentRoutePath: [
                ...(this.parentRoutePath() + '/' + firstEntryRaw.route).split('/').map(
                  (value) =>
                    ({
                      ...segmentMapping,
                      ...this.urlSegments(),
                    })[value] || value,
                ),
              ]
                .filter((value) => value !== '')
                .join('/'),
            };
          }
          this.activeContent.set(firstEntry);
          const dockConfigSnapshot = this.dockConfig();
          for (const currentDockConfig of dockConfigSnapshot) {
            currentDockConfig.active = firstEntryRaw.id == currentDockConfig.id;
          }
          this.dockConfig.set(dockConfigSnapshot);
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
        this.router
          .navigate([...this.parentRoutePath().split('/'), ...routeParts], {
            queryParamsHandling: 'merge',
          })
          .then((success) => {
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
