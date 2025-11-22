import {Component, inject, OnInit, signal} from '@angular/core';
import {ActivatedRoute, Route, Router} from '@angular/router';
import {NaviEntryComponent} from '../navi-entry/navi-entry.component';
import {Navi} from '../navi-entry/navi';

@Component({
  selector: 'koku-navi-list',
  imports: [
    NaviEntryComponent
  ],
  templateUrl: './navi-list.component.html',
  styleUrl: './navi-list.component.css'
})
export class NaviListComponent implements OnInit {

  activatedRoute = inject(ActivatedRoute);
  router = inject(Router);
  navi = signal<Navi[]>([]);

  ngOnInit(): void {
    if (this.activatedRoute.snapshot.routeConfig) {
      this.navi.set(this.convertMainMenuRouterEntries(this.activatedRoute.snapshot.routeConfig.children));
    }
  }

  private convertMainMenuRouterEntries(routes?: Route[], parentPath?: string) {
    const result: Navi[] = [];
    for (const currentRoute of routes || []) {
      if (
        (!currentRoute.data || !currentRoute.data["hideInNav"])
        && currentRoute.path && currentRoute.path !== '**'
      ) {
        const currentRoutePath = (parentPath || '') + (currentRoute.path || '');
        result.push({
          path: currentRoutePath,
          text: typeof currentRoute.title === 'string' ? currentRoute.title : '',
          children: this.convertMainMenuRouterEntries(currentRoute.children, currentRoutePath + '/'),
          bottom: currentRoute.data && currentRoute.data["naviAlign"] && currentRoute.data["naviAlign"] === 'bottom',
          divider: currentRoute.data && currentRoute.data["naviDivider"] && (currentRoute.data["naviDivider"] === 'before' || currentRoute.data["naviDivider"] === 'after') ? currentRoute.data["naviDivider"] : undefined,
          icon: currentRoute.data && currentRoute.data["naviIcon"] ? currentRoute.data["naviIcon"] : undefined
        });
      }
    }
    return result;
  }

}
