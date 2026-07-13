import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { BusinessRulesContentComponent } from './business-rules-content.component';

describe('BusinessRulesContentComponent', () => {
  it('creates its content recipe and delegates close and routed output', async () => {
    const factory = vi.fn((context: any) => ({ component: class {}, inputs: () => ({ context }) }));
    await TestBed.configureTestingModule({ imports: [BusinessRulesContentComponent] })
      .overrideComponent(BusinessRulesContentComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(BusinessRulesContentComponent);
    fixture.componentRef.setInput('content', { '@type': 'stub' });
    fixture.componentRef.setInput('contentSetup', { contentRegistry: { stub: factory } });
    fixture.componentRef.setInput('parentRoutePath', '/parent');
    fixture.detectChanges();
    expect(fixture.componentInstance.contentRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    const context = factory.mock.calls[0][0];
    const closed = vi.fn();
    const routed = vi.fn();
    fixture.componentInstance.closeRequested.subscribe(closed);
    fixture.componentInstance.openRoutedContentRequested.subscribe(routed);
    context.close();
    context.openRoutedContent(['details']);
    expect(closed).toHaveBeenCalled();
    expect(routed).toHaveBeenCalledWith(['details']);
    fixture.componentRef.setInput('content', { '@type': 'stub', title: 'Changed' });
    expect(fixture.componentInstance.contentRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
  });
});
