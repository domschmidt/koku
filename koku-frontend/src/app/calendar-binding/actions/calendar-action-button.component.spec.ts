import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { CalendarActionButtonComponent } from './calendar-action-button.component';

describe('CalendarActionButtonComponent', () => {
  it('binds action presentation and emits click events', async () => {
    await TestBed.configureTestingModule({ imports: [CalendarActionButtonComponent] })
      .overrideComponent(CalendarActionButtonComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarActionButtonComponent);
    fixture.componentRef.setInput('title', 'Create');
    fixture.componentRef.setInput('loading', true);
    fixture.componentRef.setInput('icon', 'PLUS');
    fixture.detectChanges();
    const clicked = vi.fn();
    fixture.componentInstance.clicked.subscribe(clicked);
    const event = new MouseEvent('click');
    fixture.componentInstance.clicked.emit(event);
    expect(fixture.componentInstance.title()).toBe('Create');
    expect(clicked).toHaveBeenCalledWith(event);
  });
});
