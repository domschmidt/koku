import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Subject, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ToastService } from '../../../toast/toast.service';
import { DashboardAsyncTextPanelComponent } from './dashboard-async-text-panel.component';

describe('DashboardAsyncTextPanelComponent', () => {
  it('loads panel content and reports source errors', async () => {
    const request = new Subject<any>();
    const http = { get: vi.fn(() => request) };
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [DashboardAsyncTextPanelComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: ToastService, useValue: toast },
      ],
    })
      .overrideComponent(DashboardAsyncTextPanelComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DashboardAsyncTextPanelComponent);
    fixture.componentRef.setInput('content', { sourceUrl: '/panel' });
    fixture.detectChanges();
    request.next({ text: 'Loaded' });
    expect(fixture.componentInstance.loadedContent()).toEqual({ text: 'Loaded' });
    (fixture.componentInstance as any).httpClient = { get: vi.fn(() => throwError(() => new Error('offline'))) };
    (fixture.componentInstance as any).loadContent();
    expect(toast.add).toHaveBeenCalledWith('Fehler beim Laden des Panels.', 'error');
    fixture.componentRef.setInput('content', {});
    expect(() => (fixture.componentInstance as any).loadContent()).toThrow('Missing sourceUrl');
  });
});
