import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { GLOBAL_EVENT_BUS } from '../../events/global-events';
import { CalendarInlineHeaderContainerComponent } from './calendar-inline-header-container.component';

describe('CalendarInlineHeaderContainerComponent', () => {
  it('loads and updates titles and emits close commands', async () => {
    const http = { get: vi.fn(() => of({ customer: { name: 'Before' } })) };
    await TestBed.configureTestingModule({
      imports: [CalendarInlineHeaderContainerComponent],
      providers: [{ provide: HttpClient, useValue: http }],
    })
      .overrideComponent(CalendarInlineHeaderContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarInlineHeaderContainerComponent);
    fixture.componentRef.setInput('contentSetup', {});
    fixture.componentRef.setInput('sourceUrl', '/customers/42');
    fixture.componentRef.setInput('titlePath', 'customer.name');
    fixture.componentRef.setInput('content', {
      globalEventListeners: [
        {
          '@type': 'event-payload',
          eventName: 'calendar-header-update',
          idPath: 'id',
          titleValuePath: 'customer.name',
        },
      ],
    });
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.loadedTitle()).toBe('Before');
    GLOBAL_EVENT_BUS.propagateGlobalEvent('calendar-header-update', { id: 42, customer: { name: 'After' } });
    expect(component.loadedTitle()).toBe('After');
    fixture.componentRef.setInput('titlePath', undefined);
    fixture.componentRef.setInput('sourceUrl', '/customers/43');
    fixture.detectChanges();
    expect(component.loadedTitle()).toBeNull();
    fixture.componentRef.setInput('content', { globalEventListeners: [{ '@type': 'bad', eventName: 'bad' }] });
    fixture.detectChanges();
    expect(() => GLOBAL_EVENT_BUS.propagateGlobalEvent('bad', {})).toThrow('Global event listener failed: bad');
    fixture.componentRef.setInput('content', {
      globalEventListeners: [{ '@type': 'event-payload', eventName: 'missing-id' }],
    });
    fixture.detectChanges();
    expect(() => GLOBAL_EVENT_BUS.propagateGlobalEvent('missing-id', {})).toThrow(
      'Global event listener failed: missing-id',
    );
    const closed = vi.fn();
    component.closeRequested.subscribe(closed);
    component.closeInlineContent();
    expect(closed).toHaveBeenCalled();
    fixture.componentRef.setInput('sourceUrl', undefined);
    fixture.detectChanges();
    expect(component.loadedTitle()).toBeNull();
    expect(() => (component as any).requireEventName(undefined)).toThrow('Missing eventName');
    const remove = vi.spyOn(GLOBAL_EVENT_BUS, 'removeGlobalEventListener');
    fixture.destroy();
    expect(remove).toHaveBeenCalledWith(component.componentRef);
    vi.restoreAllMocks();
  });
});
