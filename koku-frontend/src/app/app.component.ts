import {Component, OnInit} from '@angular/core';
import {NaviService} from "./navi/navi.service";
import {ActivatedRoute, Route, Router, RouterOutlet} from "@angular/router";
import {Observable} from "rxjs";
import {MyUserDetailsService} from "./user/my-user-details.service";
import {DomSanitizer} from "@angular/platform-browser";
import {NaviNode} from "./navi/navigation-entry.component";
import {MatIconRegistry} from "@angular/material/icon";
import {slideInAnimation} from "./animations";


@Component({
  selector: 'root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  animations: [
    slideInAnimation
  ]
})
export class AppComponent implements OnInit {
  userDetails$: Observable<KokuDto.KokuUserDetailsDto>;
  navi: NaviNode[] | undefined;

  constructor(
    public readonly naviService: NaviService,
    public readonly activatedRoute: ActivatedRoute,
    private matIconRegistry: MatIconRegistry,
    private readonly domSanitizer: DomSanitizer,
    public readonly router: Router,
    private readonly userService: MyUserDetailsService) {
    this.userDetails$ = this.userService.getDetails();

    this.matIconRegistry.addSvgIcon(
      'voip',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/voip.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_before',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_before.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_after',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_after.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_above',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_above.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'add_below',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/add_below.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'brick_block_add',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/brick_block_add.svg')
    );
    this.matIconRegistry.addSvgIcon(
      'account',
      this.domSanitizer.bypassSecurityTrustResourceUrl('assets/icons/account.svg')
    );
  }

  ngOnInit(): void {
    this.navi = this.convertRouterEntries(this.router.config);
  }

  getParentLink(parent: ActivatedRoute | null | undefined) {
    let result = '';

    if (parent) {
      result += '/' + parent.firstChild?.routeConfig?.path;
    }
    return result;
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

  prepareRoute(outlet: RouterOutlet) {
    return outlet && outlet.activatedRouteData && outlet.activatedRouteData.animation;
  }
}
