import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { DashboardContentRendererComponent } from './dashboard-content-renderer.component';

describe('DashboardContentRendererComponent', () => {
  it('creates a stable dashboard recipe and rejects content without an id', async () => {
    const factory = vi.fn((context) => ({ component: class {}, context }));
    await TestBed.configureTestingModule({ imports: [DashboardContentRendererComponent] })
      .overrideComponent(DashboardContentRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(DashboardContentRendererComponent);
    fixture.componentRef.setInput('content', { id: 'revenue', '@type': 'chart' });
    fixture.componentRef.setInput('contentRegistry', { chart: factory });
    fixture.detectChanges();
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    expect(factory.mock.calls[0][0].content().id).toBe('revenue');
    fixture.componentRef.setInput('content', { id: 'costs', '@type': 'chart' });
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    fixture.componentRef.setInput('content', { '@type': 'chart' });
    expect(() => fixture.componentInstance.recipe()).toThrow('stable id');
  });
});
