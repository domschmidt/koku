import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {EMPTY, Observable, throwError} from "rxjs";
import {AuthService} from "./auth.service";
import {Router} from "@angular/router";
import {catchError, mergeMap} from "rxjs/operators";
import {SnackBarService} from "./snackbar/snack-bar.service";

@Injectable({
  providedIn: 'root'
})
export class UnauthorizedLoginInterceptorService implements HttpInterceptor {

  constructor(private readonly authService: AuthService,
              private readonly router: Router,
              private readonly snackBarService: SnackBarService) {
  }

  private readonly UNAUTHORIZED_ROUTES = ['/api/auth/login', '/api/auth/logout', '/api/auth/refresh'];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.UNAUTHORIZED_ROUTES.indexOf(req.url) < 0) {
      return this.handleRequest(next, req);
    } else {
      return next.handle(req);
    }
  }

  private handleRequest(next: HttpHandler, req: HttpRequest<any>): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(catchError((err: any) => {
      if (err instanceof HttpErrorResponse) {
        if (err.status === 401) {
          return this.authService.refreshToken().pipe(mergeMap(() => {
            return this.handleRequest(next, req);
          }), catchError(() => {
            this.snackBarService.openCommonSnack('Nutzersitzung ist abgelaufen. Bitte erneut anmelden.');
            this.router.navigate(['/login'], {
              state: {
                enforce: true
              }
            });
            return EMPTY;
          }));
        } else {
          this.snackBarService.openCommonSnack(`Es ist ein Fehler bei der Anfrage aufgetreten. ${err.status}: ${err.statusText}`);
          return throwError(err);
        }
      } else {
        this.snackBarService.openCommonSnack(`Es ist ein Fehler bei der Anfrage aufgetreten. ${err.status}`);
        return throwError(err);
      }
    }));
  }
}
