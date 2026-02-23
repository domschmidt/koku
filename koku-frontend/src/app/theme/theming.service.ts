import { ApplicationRef, inject, Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ThemingService {
  private ref = inject(ApplicationRef);

  theme = new BehaviorSubject<'koku-light' | 'koku-dark'>('koku-light');

  constructor() {
    const darkModeOn = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (darkModeOn) {
      this.theme.next('koku-dark');
    }

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      this.theme.next(e.matches ? 'koku-dark' : 'koku-light');
      this.ref.tick();
    });
  }
}
