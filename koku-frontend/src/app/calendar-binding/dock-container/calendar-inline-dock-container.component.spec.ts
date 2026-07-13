import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { NavigationEnd, Router } from '@angular/router';
import { of, Subject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { CalendarInlineDockContainerComponent } from './calendar-inline-dock-container.component';

describe('CalendarInlineDockContainerComponent', () => {
  it('loads titles, follows routed tabs, emits commands and handles empty content', async () => {
    const events = new Subject<unknown>();
    const router = { url: '/base/details/42', events, navigate: vi.fn(() => Promise.resolve(true)) };
    const http = { get: vi.fn(() => of({ customer: { name: 'Ada' } })) };
    await TestBed.configureTestingModule({
      imports: [CalendarInlineDockContainerComponent],
      providers: [
        { provide: Router, useValue: router },
        { provide: HttpClient, useValue: http },
      ],
    })
      .overrideComponent(CalendarInlineDockContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarInlineDockContainerComponent);
    const component = fixture.componentInstance;
    fixture.componentRef.setInput('content', [
      { id: 'overview', title: 'Overview', content: { '@type': 'text' } },
      { id: 'details', title: 'Details', icon: 'edit', route: 'details/:id', content: { '@type': 'formular' } },
      { id: undefined, content: { '@type': 'ignored' } },
      { id: 'missing-content' },
    ]);
    fixture.componentRef.setInput('contentSetup', {});
    fixture.componentRef.setInput('parentRoutePath', '/base');
    fixture.componentRef.setInput('urlSegments', { tenant: 'acme', ':id': '42' });
    fixture.componentRef.setInput('sourceUrl', '/customers/42');
    fixture.componentRef.setInput('titlePath', 'customer.name');
    fixture.detectChanges();
    expect(component.title()).toBe('Ada');
    expect(component.dockConfig()).toHaveLength(2);
    expect(component.activeContent()?.id).toBe('details');
    const closed = vi.fn();
    const routed = vi.fn();
    component.closeRequested.subscribe(closed);
    component.openRoutedContentRequested.subscribe(routed);
    component.closeInlineContent();
    component.openRoutedContent(['child']);
    expect(closed).toHaveBeenCalled();
    expect(routed).toHaveBeenCalledWith(['child']);
    component.onDockContentActivationRequested({ id: 'unknown' });
    component.onDockContentActivationRequested({ id: 'overview' });
    expect(component.activeContent()?.id).toBe('overview');
    component.onDockContentActivationRequested({ id: 'details' });
    await Promise.resolve();
    expect(router.navigate).toHaveBeenCalledWith(['', 'base', 'details', '42'], { queryParamsHandling: 'merge' });
    router.url = '/base/not-matched';
    events.next(new NavigationEnd(1, '/base/details/42', '/base/not-matched'));
    expect(component.activeContent()?.id).toBe('overview');
    fixture.componentRef.setInput('content', []);
    fixture.detectChanges();
    expect(component.activeContent()).toBeNull();
    expect(component.dockConfig()).toEqual([]);
    fixture.componentRef.setInput('sourceUrl', undefined);
    fixture.detectChanges();
    expect(component.title()).toBeNull();
    fixture.destroy();
  });
});
