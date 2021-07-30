import {Component, OnInit} from "@angular/core";
import {Observable} from "rxjs";
import {NaviNode} from "../../navi/navigation-entry.component";
import {NaviService} from "../../navi/navi.service";
import {ActivatedRoute, Route, Router} from "@angular/router";
import {MatIconRegistry} from "@angular/material/icon";
import {DomSanitizer} from "@angular/platform-browser";
import {MyUserDetailsService} from "../../user/my-user-details.service";

@Component({
  templateUrl: './page-skeleton.component.html',
  styleUrls: ['./page-skeleton.component.scss']
})
export class PageSkeletonComponent implements OnInit {
  userDetails$: Observable<KokuDto.KokuUserDetailsDto>;
  navi: NaviNode[] | undefined;

  constructor(
    public readonly naviService: NaviService,
    public readonly activatedRoute: ActivatedRoute,
    private matIconRegistry: MatIconRegistry,
    private readonly domSanitizer: DomSanitizer,
    public readonly router: Router,
    private readonly userService: MyUserDetailsService
  ) {
    this.userDetails$ = this.userService.getDetails();
  }

  ngOnInit(): void {
    if (this.activatedRoute.snapshot.routeConfig) {
      this.navi = this.convertRouterEntries(this.activatedRoute.snapshot.routeConfig.children);
    }
  }

  private convertRouterEntries(routes?: Route[], parentPath?: string) {
    const result: NaviNode[] = [];
    for (const currentRoute of routes || []) {
      if (currentRoute.data && !currentRoute.data?.hideInNav) {
        const currentRoutePath = (parentPath || '') + (currentRoute.path || '');
        result.push({
          path: currentRoutePath,
          text: currentRoute.data?.name || '',
          showChildrenWithinTabs: currentRoute.data.showChildrenWithinTabs,
          children: this.convertRouterEntries(currentRoute.children, currentRoutePath + '/')
        });
      }
    }
    return result;
  }

}
