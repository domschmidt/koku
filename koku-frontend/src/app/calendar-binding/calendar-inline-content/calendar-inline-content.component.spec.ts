import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { CalendarInlineContentComponent } from './calendar-inline-content.component';

describe('CalendarInlineContentComponent', () => {
  it('creates its recipe with query params and delegates outputs', async () => {
    const queryParams = new BehaviorSubject({ date: '2027-01-01' });
    const factory = vi.fn((context: any) => ({ component: class {}, inputs: () => ({ context }) }));
    await TestBed.configureTestingModule({
      imports: [CalendarInlineContentComponent],
      providers: [{ provide: ActivatedRoute, useValue: { queryParams, snapshot: { queryParams: {} } } }],
    })
      .overrideComponent(CalendarInlineContentComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(CalendarInlineContentComponent);
    fixture.componentRef.setInput('content', { '@type': 'stub' });
    fixture.componentRef.setInput('contentSetup', { inlineContentRegistry: { stub: factory } });
    fixture.componentRef.setInput('parentRoutePath', '/parent');
    fixture.detectChanges();
    expect(fixture.componentInstance.inlineContentRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    const context = factory.mock.calls[0][0];
    expect(context.queryParams()).toEqual({ date: '2027-01-01' });
    const closed = vi.fn();
    const routed = vi.fn();
    fixture.componentInstance.closeRequested.subscribe(closed);
    fixture.componentInstance.openRoutedContentRequested.subscribe(routed);
    context.close();
    context.openRoutedContent(['details']);
    expect(closed).toHaveBeenCalled();
    expect(routed).toHaveBeenCalledWith(['details']);
    fixture.componentRef.setInput('content', { '@type': 'stub', title: 'Changed' });
    expect(fixture.componentInstance.inlineContentRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
  });
});
