import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { ToastService } from '../toast/toast.service';
import { from } from 'rxjs';

@Component({
  selector: 'koku-logout',
  templateUrl: './logout.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LogoutComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);
  loading = signal(false);

  ngOnInit(): void {
    const authService = this.authService;

    this.loading.set(true);
    from(authService.destroySession()).subscribe({
      next: () => {
        this.loading.set(false);
        this.toastService.add(`Erfolgreich abgemeldet`, 'success');
      },
      error: () => {
        this.loading.set(false);
      },
    });
  }
}
