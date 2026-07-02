import { inject, Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { ReplaySubject } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../toast/toast.service';

export interface AuthConfig {
  url: string;
  realm: string;
  clientId: string;
}

const DEFAULT_DEV_AUTH_CONFIG: AuthConfig = {
  url: 'https://192.168.178.36:8443',
  realm: 'master',
  clientId: 'koku',
};

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly httpClient = inject(HttpClient);
  private readonly toastService = inject(ToastService);

  private _keycloak: Keycloak | undefined;
  readonly tokenSubject = new ReplaySubject<string>(1);

  private config: AuthConfig | undefined;

  get keycloak(): Keycloak {
    if (!this._keycloak) {
      if (!this.config) {
        throw new Error('Keycloak config not loaded yet');
      }
      this._keycloak = new Keycloak({
        url: this.config.url,
        realm: this.config.realm,
        clientId: this.config.clientId,
      });
    }
    return this._keycloak;
  }

  async initialize(): Promise<void> {
    this.config = await this.loadConfig();
    await this.initKeycloak();
  }

  private registerResumeEvents() {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        this.refreshSession();
      }
    });
  }

  private async loadConfig(): Promise<AuthConfig> {
    try {
      const response = await fetch('authconfig.json');
      if (!response.ok) {
        throw new Error(`Auth config request failed with status ${response.status}`);
      }
      return this.readAuthConfig(await response.json());
    } catch {
      console.warn('Using default dev configuration');
      return DEFAULT_DEV_AUTH_CONFIG;
    }
  }

  private readAuthConfig(config: unknown): AuthConfig {
    if (!config || typeof config !== 'object') {
      throw new Error('Auth config must be an object');
    }
    const candidate = config as Partial<AuthConfig>;
    if (!candidate.url || !candidate.realm || !candidate.clientId) {
      throw new Error('Auth config is missing required values');
    }
    return {
      url: candidate.url,
      realm: candidate.realm,
      clientId: candidate.clientId,
    };
  }

  private async initKeycloak(): Promise<void> {
    const authenticated = await this.keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    });
    if (authenticated) {
      this.publishToken();
      this.httpClient.post<void>('services/users/users/@self/sync', {}).subscribe();
      this.registerResumeEvents();
    } else {
      void this.keycloak.login();
    }
  }

  private publishToken(): void {
    if (this.keycloak.token) {
      this.tokenSubject.next(this.keycloak.token);
    }
  }

  destroySession() {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }

  refreshSession() {
    return this.keycloak
      .updateToken(30)
      .then(() => {
        this.publishToken();
      })
      .catch(() => {
        this.toastService.add('Fehler beim Erneuern der Nutzersitzung. Melde dich neu an!', 'error');
        this.destroySession();
      });
  }
}
