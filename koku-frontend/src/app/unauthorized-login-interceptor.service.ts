import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {EMPTY, Observable, throwError} from "rxjs";
import {AuthService} from "./auth.service";
import {Router} from "@angular/router";
import {catchError, mergeMap} from "rxjs/operators";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class UnauthorizedLoginInterceptorService implements HttpInterceptor {

  constructor(private readonly authService: AuthService,
              private readonly router: Router,
              private readonly snackBar: MatSnackBar) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (['/api/auth/login', '/api/auth/logout', '/api/auth/refresh'].indexOf(req.url) < 0) {
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
            this.snackBar.open('Nutzersitzung ist abgelaufen. Bitte erneut anmelden.',
              undefined,
              {
                duration: 10000,
                verticalPosition: 'bottom',
                politeness: "polite"
              }
            );
            this.router.navigate(['/login'], {
              state: {
                enforce: true
              }
            });
            return EMPTY;
          }));
        } else {
          this.snackBar.open(`Es ist ein Fehler bei der Anfrage aufgetreten. ${err.status}: ${err.statusText}`,
            undefined,
            {
              duration: 10000,
              verticalPosition: 'bottom',
              politeness: "polite"
            }
          );
          return throwError(err);
        }
      } else {
        this.snackBar.open(`Es ist ein Fehler bei der Anfrage aufgetreten. ${err.status}`,
          undefined,
          {
            duration: 10000,
            verticalPosition: 'bottom',
            politeness: "polite"
          }
        );
        return throwError(err);
      }
    }));
  }
}
