import {HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpRequest} from '@angular/common/http';
import {EMPTY, from, Observable, skipWhile, Subject, take} from 'rxjs';
import {inject} from '@angular/core';
import {AuthService} from './auth.service';
import {ToastService, ToastTypeUnion} from '../toast/toast.service';
import {catchError, mergeMap, tap} from 'rxjs/operators';

class AuthInterceptor {

  private static SUSPEND_ERROR_REPORTING = false

  public interceptCalls(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
    const authService = inject(AuthService);
    const toastService = inject(ToastService);

    const errorSubject = new Subject<{ msg: string, type: ToastTypeUnion }>();
    errorSubject.pipe(
      skipWhile(() => {
        return AuthInterceptor.SUSPEND_ERROR_REPORTING
      }),
      tap(() => {
        AuthInterceptor.SUSPEND_ERROR_REPORTING = true;
        setTimeout(() => {
            AuthInterceptor.SUSPEND_ERROR_REPORTING = false;
          },
          1000
        )
      })
    ).subscribe((details) => {
      toastService.add(details.msg, details.type);
    })
    const processRequest = (next: HttpHandlerFn, req: HttpRequest<any>, tryRefresh = true): Observable<HttpEvent<any>> => {
      return authService.tokenSubject.pipe(
        take(1),
        mergeMap((token) => {
          return next(req.clone({
            headers: req.headers.set("Authorization", `Bearer ${token}`),
          }))
        }),
        catchError((err: any) => {
          if (err instanceof HttpErrorResponse) {
            if (err.status === 401 && tryRefresh) {
              return from(authService.refreshSession()).pipe(mergeMap(() => {
                return processRequest(next, req, false);
              }), catchError((stillError) => {
                if (stillError instanceof HttpErrorResponse) {
                  if (stillError.status === 401) {
                    // refresh did not help. we must forward to login page
                    authService.destroySession();
                    return EMPTY;
                  }
                }
                return EMPTY;
              }));
            } else {
              throw err;
            }
          } else {
            throw err;
          }
        })
      );
    }

    return processRequest(next, req);
  }
}

export const UNAUTHORIZED_INTERCEPTOR_INSTANCE = new AuthInterceptor();
