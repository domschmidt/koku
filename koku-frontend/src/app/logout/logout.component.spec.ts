import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { AuthService } from '../auth/auth.service';
import { ToastService } from '../toast/toast.service';
import { LogoutComponent } from './logout.component';

describe('LogoutComponent', () => {
  it.each([
    [of(undefined), true],
    [throwError(() => new Error('logout failed')), false],
  ])('settles logout state for success and failure', async (result, successful) => {
    const auth = { destroySession: vi.fn(() => firstValueFrom(result)) };
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [LogoutComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: ToastService, useValue: toast },
      ],
    })
      .overrideComponent(LogoutComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(LogoutComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.loading()).toBe(false);
    expect(toast.add).toHaveBeenCalledTimes(successful ? 1 : 0);
  });
});
