import { HttpErrorResponse, HttpRequest, HttpResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, ReplaySubject, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { UNAUTHORIZED_INTERCEPTOR_INSTANCE } from './auth-interceptor';
import { AuthService } from './auth.service';

async function configureInterceptor() {
  const tokenSubject = new ReplaySubject<string>(1);
  tokenSubject.next('token');
  const auth = { tokenSubject, refreshSession: vi.fn(() => Promise.resolve()), destroySession: vi.fn() };
  await TestBed.configureTestingModule({
    providers: [{ provide: AuthService, useValue: auth }],
  }).compileComponents();
  return auth;
}

describe('AuthInterceptor', () => {
  it('adds bearer tokens and retries one unauthorized request after refresh', async () => {
    const auth = await configureInterceptor();
    const next = vi
      .fn()
      .mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 401 })))
      .mockReturnValueOnce(of(new HttpResponse({ status: 200 })));
    const response = await firstValueFrom(
      TestBed.runInInjectionContext(() =>
        UNAUTHORIZED_INTERCEPTOR_INSTANCE.interceptCalls(new HttpRequest('GET', '/secured'), next),
      ),
    );
    expect(response).toBeInstanceOf(HttpResponse);
    expect(auth.refreshSession).toHaveBeenCalled();
    expect(next).toHaveBeenCalledTimes(2);
    expect(next.mock.calls[0][0].headers.get('Authorization')).toBe('Bearer token');
  });

  it('ends persistent unauthorized sessions and forwards unrelated failures', async () => {
    const auth = await configureInterceptor();
    const unauthorized = vi.fn(() => throwError(() => new HttpErrorResponse({ status: 401 })));
    await expect(
      firstValueFrom(
        TestBed.runInInjectionContext(() =>
          UNAUTHORIZED_INTERCEPTOR_INSTANCE.interceptCalls(new HttpRequest('GET', '/secured'), unauthorized),
        ),
        { defaultValue: undefined },
      ),
    ).resolves.toBeUndefined();
    expect(auth.destroySession).toHaveBeenCalled();

    const forbidden = new HttpErrorResponse({ status: 403 });
    await expect(
      firstValueFrom(
        TestBed.runInInjectionContext(() =>
          UNAUTHORIZED_INTERCEPTOR_INSTANCE.interceptCalls(new HttpRequest('GET', '/forbidden'), () =>
            throwError(() => forbidden),
          ),
        ),
      ),
    ).rejects.toBe(forbidden);
    const networkError = new Error('network');
    await expect(
      firstValueFrom(
        TestBed.runInInjectionContext(() =>
          UNAUTHORIZED_INTERCEPTOR_INSTANCE.interceptCalls(new HttpRequest('GET', '/network'), () =>
            throwError(() => networkError),
          ),
        ),
      ),
    ).rejects.toBe(networkError);
  });

  it('swallows a failed refresh without destroying the session for non-HTTP failures', async () => {
    const auth = await configureInterceptor();
    auth.refreshSession.mockRejectedValueOnce(new Error('offline'));

    await expect(
      firstValueFrom(
        TestBed.runInInjectionContext(() =>
          UNAUTHORIZED_INTERCEPTOR_INSTANCE.interceptCalls(new HttpRequest('GET', '/secured'), () =>
            throwError(() => new HttpErrorResponse({ status: 401 })),
          ),
        ),
        { defaultValue: undefined },
      ),
    ).resolves.toBeUndefined();
    expect(auth.destroySession).not.toHaveBeenCalled();
  });
});
