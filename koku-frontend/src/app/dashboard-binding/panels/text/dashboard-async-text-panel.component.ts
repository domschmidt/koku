import { Component, DestroyRef, inject, input, OnChanges, signal, SimpleChanges } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ToastService } from '../../../toast/toast.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subscription } from 'rxjs';
import { DashboardTextPanelComponent } from './dashboard-text-panel.component';
import { KokuDashboardAsyncTextPanel } from '../../../../types/generated/dashboard';
import { KokuDashboardTextPanel } from '../../../../types/generated/dashboard';

@Component({
  selector: 'dashboard-async-text-panel',
  imports: [DashboardTextPanelComponent],
  templateUrl: './dashboard-async-text-panel.component.html',
})
export class DashboardAsyncTextPanelComponent implements OnChanges {
  content = input.required<KokuDashboardAsyncTextPanel>();
  loadedContent = signal<KokuDashboardTextPanel | null>(null);
  httpClient = inject(HttpClient);
  toastService = inject(ToastService);
  destroyRef = inject(DestroyRef);
  private lastContentSubscription: Subscription | undefined;

  ngOnChanges(changes: SimpleChanges) {
    if (changes['content']) {
      this.loadContent();
    }
  }

  private loadContent() {
    const contentSnapshot = this.content();
    if (!contentSnapshot.sourceUrl) {
      throw new Error('Missing sourceUrl');
    }
    this.loadedContent.set(null);
    if (this.lastContentSubscription && !this.lastContentSubscription.closed) {
      this.lastContentSubscription.unsubscribe();
    }

    this.lastContentSubscription = this.httpClient
      .get<KokuDashboardTextPanel>(contentSnapshot.sourceUrl)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(
        (loadedContent) => {
          this.loadedContent.set(loadedContent);
        },
        () => {
          this.toastService.add('Fehler beim Laden des Panels.', 'error');
        },
      );
  }
}
