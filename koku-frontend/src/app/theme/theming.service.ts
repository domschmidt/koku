import { ApplicationRef, inject, Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ThemingService {
  private readonly ref = inject(ApplicationRef);

  readonly theme = new BehaviorSubject<'koku-light' | 'koku-dark'>('koku-light');

  constructor() {
    const darkModeQuery = globalThis.matchMedia?.('(prefers-color-scheme: dark)');

    if (darkModeQuery?.matches) {
      this.theme.next('koku-dark');
    }

    darkModeQuery?.addEventListener('change', (e) => {
      this.theme.next(e.matches ? 'koku-dark' : 'koku-light');
      this.ref.tick();
    });
  }
}
