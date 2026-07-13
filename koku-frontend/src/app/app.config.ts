import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { UNAUTHORIZED_INTERCEPTOR_INSTANCE } from './auth/auth-interceptor';
import localeDe from '@angular/common/locales/de';
import localeDeExtra from '@angular/common/locales/extra/de';
import { registerLocaleData } from '@angular/common';
import { AuthService } from './auth/auth.service';
import { ThemingService } from './theme/theming.service';
import { FORMULAR_PLUGIN_PROVIDERS } from './formular.plugins';
import { CALENDAR_PLUGIN_PROVIDERS } from './calendar.plugins';

registerLocaleData(localeDe, 'de-DE', localeDeExtra);

export const appConfig: ApplicationConfig = {
  providers: [
    provideZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([UNAUTHORIZED_INTERCEPTOR_INSTANCE.interceptCalls])),
    provideAppInitializer(() => inject(AuthService).initialize()),
    { provide: LOCALE_ID, useValue: 'de-DE' },
    ThemingService,
    ...FORMULAR_PLUGIN_PROVIDERS,
    ...CALENDAR_PLUGIN_PROVIDERS,
  ],
};
