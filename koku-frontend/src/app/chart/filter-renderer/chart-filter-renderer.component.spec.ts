import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ChartFilterRendererComponent } from './chart-filter-renderer.component';

describe('ChartFilterRendererComponent', () => {
  it('creates filter recipes and forwards changed values', async () => {
    const factory = vi.fn((context: any) => ({ component: class {}, context }));
    await TestBed.configureTestingModule({ imports: [ChartFilterRendererComponent] })
      .overrideComponent(ChartFilterRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ChartFilterRendererComponent);
    fixture.componentRef.setInput('content', { '@type': 'date' });
    fixture.componentRef.setInput('filterRegistry', { date: factory });
    fixture.detectChanges();
    const changed = vi.fn();
    fixture.componentInstance.filterValueChanged.subscribe(changed);
    const recipe = fixture.componentInstance.filterRecipe() as any;
    recipe.context.emit('2027-01-01');
    expect(changed).toHaveBeenCalledWith('2027-01-01');
    fixture.componentRef.setInput('content', { '@type': 'date', label: 'Changed' });
    expect(fixture.componentInstance.filterRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
  });
});
