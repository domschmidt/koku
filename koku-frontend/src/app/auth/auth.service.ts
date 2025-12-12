import {inject, Injectable} from '@angular/core';
import Keycloak from 'keycloak-js';
import {from, mergeMap, ReplaySubject} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {ToastService} from '../toast/toast.service';

export interface AuthConfig {
  url: string;
  realm: string;
  clientId: string;
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private httpClient = inject(HttpClient);
  private toastService = inject(ToastService);

  private _keycloak: Keycloak | undefined;
  tokenSubject: ReplaySubject<string> = new ReplaySubject(1);

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

  constructor() {
    from(fetch('authconfig.json')).pipe(mergeMap(res => from(res.json()))).subscribe(
      (res: any) => {
        this.config = res;
        this.initKeycloak();
      },
      () => {
        console.warn('Using default dev configuration');
        this.config = {
          url: "https://192.168.178.36:8443",
          realm: "master",
          clientId: "koku"
        };
        this.initKeycloak();
      });
  }

  private registerResumeEvents() {
    document.addEventListener('visibilitychange', () => {
      if (document.visibilityState === 'visible') {
        this.refreshSession();
      }
    });
  }

  private initKeycloak() {
    from(this.keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    })).subscribe((authenticated) => {
      if (authenticated) {
        if (this.keycloak.token) {
          this.tokenSubject.next(this.keycloak.token);
          this.httpClient.post<void>('services/users/users/@self/sync', {}).subscribe();
          this.registerResumeEvents();
        }
      } else {
        this.keycloak.login();
      }
    });
  }

  destroySession() {
    return this.keycloak.logout({redirectUri: window.location.origin});
  }

  refreshSession() {
    return this.keycloak.updateToken(30)
      .then(() => {
        if (this.keycloak.token) {
          this.tokenSubject.next(this.keycloak.token);
        }
      })
      .catch(() => {
        this.toastService.add("Fehler beim Erneuern der Nutzersitzung. Melde dich neu an!", 'error');
        this.destroySession();
      });
  }

}
