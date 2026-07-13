import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { CalendarInlineListContainerComponent } from './calendar-inline-list-container.component';

describe('CalendarInlineListContainerComponent', () => {
  it('forwards close requests', async () => {
    await TestBed.configureTestingModule({ imports: [CalendarInlineListContainerComponent] })
      .overrideComponent(CalendarInlineListContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarInlineListContainerComponent);
    fixture.componentRef.setInput('contentSetup', {});
    fixture.detectChanges();
    const closed = vi.fn();
    fixture.componentInstance.closeRequested.subscribe(closed);
    fixture.componentInstance.closeInlineContent();
    expect(closed).toHaveBeenCalledOnce();
  });
});
