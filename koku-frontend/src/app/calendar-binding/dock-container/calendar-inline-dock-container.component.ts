import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { DockComponent, DockContentItem } from '../../dock/dock.component';
import { CalendarContentSetup } from '../../calendar/calendar.component';
import { OutletDirective } from '../../portal/outlet.directive';
import { get } from '../../utils/get';
import { CalendarInlineContentComponent } from '../calendar-inline-content/calendar-inline-content.component';
import { Subscription } from 'rxjs';
import { childRouteSegments, matchRouteSegments, resolvedRouteParts, resolvedRoutePath } from '../../utils/route.utils';

interface ExtendedDockContentItem extends DockContentItem {
  content: KokuDto.AbstractCalendarInlineContentDto;
  route?: string;
}

interface ExtendedCalendarInlineDockContentItemDto extends KokuDto.CalendarInlineDockContentItemDto {
  parentRoutePath?: string;
}

@Component({
  selector: '[calendar-dock-container],calendar-dock-container',
  imports: [DockComponent, CalendarInlineContentComponent],
  templateUrl: './calendar-inline-dock-container.component.html',
  styleUrl: './calendar-inline-dock-container.component.css',
})
export class CalendarInlineDockContainerComponent {
  content = input.required<KokuDto.CalendarInlineDockContentItemDto[]>();
  contentSetup = input.required<CalendarContentSetup>();
  urlSegments = input<Record<string, string> | null>(null);
  buttonDockOutlet = input<OutletDirective>();
  sourceUrl = input<string>();
  titlePath = input<string>();
  parentRoutePath = input<string>('');

  dockConfig = signal<ExtendedDockContentItem[]>([]);
  activeContent = signal<ExtendedCalendarInlineDockContentItemDto | null>(null);
  title = signal<string | null>(null);

  httpClient = inject(HttpClient);
  router = inject(Router);
  destroyRef = inject(DestroyRef);

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  private dockContentIndex: Record<string, ExtendedDockContentItem> = {};
  private routerUrlSubscription: Subscription | undefined;

  constructor() {
    this.observeTitleSource();
    toObservable(this.content).subscribe((content) => {
      this.configureDockContent(content);
    });
  }

  private observeTitleSource(): void {
    toObservable(this.sourceUrl).subscribe((sourceUrl) => {
      if (sourceUrl) {
        this.httpClient.get(sourceUrl).subscribe((detailSource) => {
          this.updateTitle(detailSource);
        });
      } else {
        this.title.set(null);
      }
    });
  }

  private updateTitle(detailSource: any): void {
    const titlePath = this.titlePath();
    this.title.set(titlePath ? get(detailSource, titlePath) : null);
  }

  private configureDockContent(content: KokuDto.CalendarInlineDockContentItemDto[]): void {
    const transformedContent = this.transformDockContent(content);
    this.dockContentIndex = this.indexDockContent(transformedContent);
    this.dockConfig.set(transformedContent);
    this.routerUrlSubscription?.unsubscribe();
    this.routerUrlSubscription = this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((evnt) => {
      if (evnt instanceof NavigationEnd) {
        this.applyNavigationState(content);
      }
    });
    this.applyNavigationState(content);
  }

  private transformDockContent(content: KokuDto.CalendarInlineDockContentItemDto[]): ExtendedDockContentItem[] {
    const items: ExtendedDockContentItem[] = [];
    for (const currentContent of content || []) {
      if (currentContent.id !== undefined && currentContent.content !== undefined) {
        items.push({
          active: false,
          id: currentContent.id,
          content: currentContent.content,
          icon: currentContent.icon,
          title: currentContent.title,
          route: currentContent.route,
        });
      }
    }
    return items;
  }

  private indexDockContent(items: ExtendedDockContentItem[]): Record<string, ExtendedDockContentItem> {
    const index: Record<string, ExtendedDockContentItem> = {};
    for (const item of items) {
      index[item.id] = item;
    }
    return index;
  }

  private applyNavigationState(content: KokuDto.CalendarInlineDockContentItemDto[]): void {
    const activeRoutedContent = this.findActiveRoutedContent(content);
    if (activeRoutedContent) {
      this.activateRoutedContent(activeRoutedContent.content, activeRoutedContent.segmentMapping);
      return;
    }
    this.activateFirstContent(content);
  }

  private findActiveRoutedContent(content: KokuDto.CalendarInlineDockContentItemDto[]): {
    content: KokuDto.CalendarInlineDockContentItemDto;
    segmentMapping: Record<string, string>;
  } | null {
    const segments = childRouteSegments(this.router.url, this.parentRoutePath());
    for (const currentRoutedContent of content || []) {
      const segmentMapping = matchRouteSegments(currentRoutedContent.route, segments);
      if (segmentMapping) {
        return { content: currentRoutedContent, segmentMapping };
      }
    }
    return null;
  }

  private activateRoutedContent(
    currentRoutedContent: KokuDto.CalendarInlineDockContentItemDto,
    segmentMapping: Record<string, string>,
  ): void {
    this.activateDockContent(currentRoutedContent.id);
    this.activeContent.set({
      ...currentRoutedContent,
      parentRoutePath: this.createParentRoutePath(currentRoutedContent.route, segmentMapping),
    });
  }

  private activateFirstContent(content: KokuDto.CalendarInlineDockContentItemDto[]): void {
    const firstEntryRaw = (content || [])[0];
    if (!firstEntryRaw) {
      this.activeContent.set(null);
      this.activateDockContent(undefined);
      return;
    }
    this.activeContent.set({
      ...firstEntryRaw,
      parentRoutePath: this.createParentRoutePath(firstEntryRaw.route, {}),
    });
    this.activateDockContent(firstEntryRaw.id);
  }

  private createParentRoutePath(route: string | undefined, segmentMapping: Record<string, string>): string {
    return resolvedRoutePath(this.parentRoutePath(), route, {
      ...segmentMapping,
      ...(this.urlSegments() || {}),
    });
  }

  private activateDockContent(contentId: string | undefined): void {
    const dockConfigSnapshot = this.dockConfig();
    for (const currentDockConfig of dockConfigSnapshot) {
      currentDockConfig.active = contentId !== undefined && contentId == currentDockConfig.id;
    }
    this.dockConfig.set(dockConfigSnapshot);
  }

  closeInlineContent() {
    this.closeRequested.emit();
  }

  openRoutedContent(routes: string[]) {
    this.openRoutedContentRequested.emit(routes);
  }

  onDockContentActivationRequested(requestedDockContent: DockContentItem) {
    const contentLookup = this.dockContentIndex[requestedDockContent.id];
    if (!contentLookup) {
      return;
    }
    if (contentLookup.route !== undefined) {
      this.navigateToRoutedContent(contentLookup);
    } else {
      this.activeContent.set(contentLookup);
      this.activateDockContent(contentLookup.id);
    }
  }

  private navigateToRoutedContent(contentLookup: ExtendedDockContentItem): void {
    const routeParts = resolvedRouteParts(contentLookup.route, this.urlSegments());
    this.router
      .navigate([...this.parentRoutePath().split('/'), ...routeParts], {
        queryParamsHandling: 'merge',
      })
      .then((success) => {
        if (success) {
          this.activateDockContent(contentLookup.id);
        }
      });
  }
}
