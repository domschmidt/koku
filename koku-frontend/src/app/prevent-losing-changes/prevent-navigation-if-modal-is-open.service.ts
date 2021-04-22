import {Injectable} from "@angular/core";
import {ActivatedRouteSnapshot, CanDeactivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class PreventNavigationIfModalIsOpenService implements CanDeactivate<any> {
  constructor(private readonly matDialog: MatDialog,
              private readonly router: Router) {}

  canDeactivate(component: any,
                currentRoute: ActivatedRouteSnapshot,
                currentState: RouterStateSnapshot,
                nextState?: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    let result = false;
    const currentNavigation = this.router.getCurrentNavigation();
    if (currentNavigation?.extras?.state?.enforce === true || this.matDialog.openDialogs.length === 0) {
      result = true;
    }
    return result;
  }

}
