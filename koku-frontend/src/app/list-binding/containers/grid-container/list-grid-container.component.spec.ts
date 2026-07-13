import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListGridContainerComponent } from './list-grid-container.component';

describe('ListGridContainerComponent', () => {
  it('forwards close and routed-content requests', async () => {
    await TestBed.configureTestingModule({ imports: [ListGridContainerComponent] })
      .overrideComponent(ListGridContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListGridContainerComponent);
    fixture.componentRef.setInput('content', {});
    fixture.componentRef.setInput('contentSetup', {});
    fixture.detectChanges();
    const closed = vi.fn();
    const routed = vi.fn();
    fixture.componentInstance.closeRequested.subscribe(closed);
    fixture.componentInstance.openRoutedContentRequested.subscribe(routed);
    fixture.componentInstance.closeInlineContent();
    fixture.componentInstance.openRoutedContent(['details', '42']);
    expect(closed).toHaveBeenCalledOnce();
    expect(routed).toHaveBeenCalledWith(['details', '42']);
  });
});
