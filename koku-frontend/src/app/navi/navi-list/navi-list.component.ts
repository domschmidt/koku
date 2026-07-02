import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Route, Router } from '@angular/router';
import { NaviEntryComponent } from '../navi-entry/navi-entry.component';
import { Navi, NaviLink } from '../navi-entry/navi';

@Component({
  selector: 'koku-navi-list',
  imports: [NaviEntryComponent],
  templateUrl: './navi-list.component.html',
})
export class NaviListComponent implements OnInit {
  private readonly activatedRoute = inject(ActivatedRoute);
  readonly router = inject(Router);
  readonly navi = signal<Navi[]>([]);
  readonly topNavi = computed(() => this.navi().filter((currentNaviEntry) => !currentNaviEntry.bottom));
  readonly bottomNavi = computed(() => this.navi().filter((currentNaviEntry) => currentNaviEntry.bottom));

  ngOnInit(): void {
    const routeConfig = this.activatedRoute.snapshot.routeConfig;
    if (routeConfig) {
      this.navi.set(this.convertMainMenuRouterEntries(routeConfig.children));
    }
  }

  private convertMainMenuRouterEntries(routes?: Route[], parentPath?: string): Navi[] {
    const result: Navi[] = [];
    for (const currentRoute of routes || []) {
      const routeData = currentRoute.data;
      if (!routeData?.['hideInNav'] && currentRoute.path && currentRoute.path !== '**') {
        const currentRoutePath = (parentPath || '') + (currentRoute.path || '');
        const bottom = routeData?.['naviAlign'] === 'bottom';
        const naviEntry: NaviLink = {
          type: 'link',
          path: currentRoutePath,
          text: typeof currentRoute.title === 'string' ? currentRoute.title : '',
          children: this.convertMainMenuRouterEntries(currentRoute.children, currentRoutePath + '/'),
          bottom,
          icon: routeData?.['naviIcon'] || undefined,
        };

        result.push(naviEntry);

        if (routeData?.['naviDivider'] === true) {
          result.push({
            type: 'divider',
            bottom,
          });
        }
      }
    }
    return result;
  }
}
