import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NaviService {
  public openState = signal(false);

  public close(): void {
    this.openState.set(false);
  }

  public open(): void {
    this.openState.set(true);
  }
}
