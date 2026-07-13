import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListFilterComponent } from './list-filter.component';

describe('ListFilterComponent', () => {
  it('creates filter recipes and forwards predicates', async () => {
    const createRecipe = vi.fn((context: any) => ({ component: class {}, context }));
    await TestBed.configureTestingModule({ imports: [ListFilterComponent] })
      .overrideComponent(ListFilterComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListFilterComponent);
    fixture.componentRef.setInput('filter', { id: 'active', filterDefinition: { '@type': 'toggle' } });
    fixture.componentRef.setInput('contentSetup', { filterRegistry: { toggle: { createRecipe } } });
    fixture.detectChanges();
    const emitted = vi.fn();
    fixture.componentInstance.filterChanged.subscribe(emitted);
    const recipe = fixture.componentInstance.filterRecipe() as any;
    recipe.context.emit([{ searchOperator: 'EQ', searchExpression: 'TRUE' }]);
    expect(emitted).toHaveBeenCalled();
    fixture.componentRef.setInput('filter', { id: 'active', filterDefinition: recipe.context.filterDefinition() });
    expect(fixture.componentInstance.filterRecipe()).toBeTruthy();
  });

  it('rejects incomplete filter definitions', () => {
    const fixture = TestBed.createComponent(ListFilterComponent);
    fixture.componentRef.setInput('filter', {});
    fixture.componentRef.setInput('contentSetup', { filterRegistry: {} });
    expect(() => fixture.componentInstance.filterRecipe()).toThrow('requires an id and filter definition');
  });
});
