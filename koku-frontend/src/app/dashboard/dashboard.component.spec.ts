import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Subject, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ToastService } from '../toast/toast.service';
import { DASHBOARD_PLUGIN, DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  it('loads dashboards, reports errors and destroys plugins', async () => {
    const requests = new Subject<any>();
    const http = { get: vi.fn(() => requests) };
    const toast = { add: vi.fn() };
    const plugin = { destroy: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: ToastService, useValue: toast },
        { provide: DASHBOARD_PLUGIN, useValue: vi.fn(() => plugin) },
      ],
    })
      .overrideComponent(DashboardComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.componentRef.setInput('contentRegistry', {});
    fixture.componentRef.setInput('dashboardUrl', '/dashboard');
    fixture.detectChanges();
    requests.next({ root: { '@type': 'grid' } });
    expect(fixture.componentInstance.dashboardData()).toEqual({ root: { '@type': 'grid' } });
    (fixture.componentInstance as any).httpClient = { get: vi.fn(() => throwError(() => new Error('offline'))) };
    (fixture.componentInstance as any).loadDashboard();
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Laden der Daten', 'error', undefined, Number.POSITIVE_INFINITY);
    fixture.componentRef.setInput('dashboardUrl', '');
    fixture.detectChanges();
    expect(fixture.componentInstance.dashboardData()).toBeNull();
    fixture.destroy();
    expect(plugin.destroy).toHaveBeenCalled();
  });
});
