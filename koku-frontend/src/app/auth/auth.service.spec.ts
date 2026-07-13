import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ToastService } from '../toast/toast.service';

const keycloakMocks = vi.hoisted(() => {
  const instances: any[] = [];
  class Keycloak {
    token = 'access-token';
    init = vi.fn(() => Promise.resolve(true));
    login = vi.fn(() => Promise.resolve());
    logout = vi.fn(() => Promise.resolve());
    updateToken = vi.fn(() => Promise.resolve(true));
    constructor(readonly config: any) {
      instances.push(this);
    }
  }
  return { Keycloak, instances };
});

vi.mock('keycloak-js', () => ({ default: keycloakMocks.Keycloak }));

import { AuthService } from './auth.service';

async function configureAuth() {
  const http = { post: vi.fn(() => of(undefined)) };
  const toast = { add: vi.fn() };
  await TestBed.configureTestingModule({
    providers: [AuthService, { provide: HttpClient, useValue: http }, { provide: ToastService, useValue: toast }],
  }).compileComponents();
  return { service: TestBed.inject(AuthService), http, toast };
}

describe('AuthService', () => {
  it('loads configuration, initializes Keycloak, publishes tokens and resumes sessions', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ url: 'https://auth', realm: 'realm', clientId: 'client' }), {
        status: 200,
      }),
    );
    const { service, http } = await configureAuth();
    const tokens: string[] = [];
    service.tokenSubject.subscribe((token) => tokens.push(token));
    await service.initialize();
    const keycloak = keycloakMocks.instances.at(-1);
    expect(fetchMock).toHaveBeenCalledWith('authconfig.json');
    expect(keycloak.config).toEqual({ url: 'https://auth', realm: 'realm', clientId: 'client' });
    expect(keycloak.init).toHaveBeenCalledWith(
      expect.objectContaining({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: expect.stringContaining('silent-check-sso.html'),
      }),
    );
    expect(tokens).toEqual(['access-token']);
    expect(http.post).toHaveBeenCalledWith('services/users/users/@self/sync', {});

    await service.refreshSession();
    expect(keycloak.updateToken).toHaveBeenCalledWith(30);
    expect(tokens).toEqual(['access-token', 'access-token']);
    await service.destroySession();
    expect(keycloak.logout).toHaveBeenCalledWith({ redirectUri: globalThis.location.origin });
    const refresh = vi.spyOn(service, 'refreshSession').mockResolvedValue();
    Object.defineProperty(document, 'visibilityState', { configurable: true, value: 'visible' });
    document.dispatchEvent(new Event('visibilitychange'));
    expect(refresh).toHaveBeenCalled();
    fetchMock.mockRestore();
  });

  it('falls back to development config, logs in and handles refresh failures', async () => {
    const warn = vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    vi.spyOn(globalThis, 'fetch').mockRejectedValueOnce(new Error('offline'));
    const { service, toast } = await configureAuth();
    (service as any).config = { url: 'initial', realm: 'initial', clientId: 'initial' };
    const keycloak = service.keycloak as any;
    keycloak.init.mockResolvedValueOnce(false);
    await service.initialize();
    expect((service as any).config).toEqual(
      expect.objectContaining({ url: 'https://192.168.178.36:8443', realm: 'koku' }),
    );
    expect(keycloak.login).toHaveBeenCalled();
    keycloak.updateToken.mockRejectedValueOnce(new Error('expired'));
    await service.refreshSession();
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Nutzersitzung'), 'error');
    expect(keycloak.logout).toHaveBeenCalled();
    expect(warn).toHaveBeenCalled();
    vi.restoreAllMocks();
  });

  it('validates config and guards premature Keycloak access', async () => {
    const { service } = await configureAuth();
    expect(() => service.keycloak).toThrow('Keycloak config not loaded yet');
    const read = (service as any).readAuthConfig.bind(service);
    expect(() => read(null)).toThrow('Auth config must be an object');
    expect(() => read({ url: 'x' })).toThrow('Auth config is missing required values');
    expect(read({ url: 'x', realm: 'r', clientId: 'c' })).toEqual({ url: 'x', realm: 'r', clientId: 'c' });
    vi.spyOn(console, 'warn').mockImplementation(() => undefined);
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(new Response('', { status: 500 }));
    await expect((service as any).loadConfig()).resolves.toEqual(expect.objectContaining({ realm: 'koku' }));
    vi.restoreAllMocks();
  });
});
