import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListInlineContentComponent } from './list-inline-content.component';

describe('ListInlineContentComponent', () => {
  it('creates its recipe and delegates close and routed output', async () => {
    const factory = vi.fn((context: any) => ({ component: class {}, inputs: () => ({ context }) }));
    await TestBed.configureTestingModule({ imports: [ListInlineContentComponent] })
      .overrideComponent(ListInlineContentComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListInlineContentComponent);
    fixture.componentRef.setInput('content', { '@type': 'stub' });
    fixture.componentRef.setInput('contentSetup', { inlineContentRegistry: { stub: factory } });
    fixture.componentRef.setInput('parentRoutePath', '/parent');
    fixture.detectChanges();
    expect(fixture.componentInstance.inlineContentRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    const context = factory.mock.calls[0][0];
    const closed = vi.fn();
    const routed = vi.fn();
    fixture.componentInstance.closeRequested.subscribe(closed);
    fixture.componentInstance.openRoutedContentRequested.subscribe(routed);
    context.close();
    context.openRoutedContent(['details']);
    expect(context.parentRoutePath()).toBe('/parent');
    expect(closed).toHaveBeenCalled();
    expect(routed).toHaveBeenCalledWith(['details']);
    fixture.componentRef.setInput('content', { '@type': 'stub', title: 'Changed' });
    expect(fixture.componentInstance.inlineContentRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
  });
});
