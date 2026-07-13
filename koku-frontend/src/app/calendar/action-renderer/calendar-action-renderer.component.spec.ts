import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { CalendarActionRendererComponent } from './calendar-action-renderer.component';

describe('CalendarActionRendererComponent', () => {
  it('creates action recipes with live callbacks', async () => {
    const factory = vi.fn(() => ({ component: class {} }));
    await TestBed.configureTestingModule({ imports: [CalendarActionRendererComponent] })
      .overrideComponent(CalendarActionRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarActionRendererComponent);
    fixture.componentRef.setInput('action', { id: 'create', '@type': 'button' });
    fixture.componentRef.setInput('contentSetup', { actionRegistry: { button: factory } });
    fixture.componentRef.setInput('openRoutedContent', vi.fn());
    fixture.componentRef.setInput(
      'getPluginApi',
      vi.fn(() => ({ ready: true })),
    );
    fixture.detectChanges();
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    fixture.componentRef.setInput('action', { id: 'create-again', '@type': 'button' });
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    fixture.componentRef.setInput('action', { '@type': 'button' });
    expect(() => fixture.componentInstance.recipe()).toThrow('stable id');
  });
});
