import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { ToggleFilterComponent } from './toggle-filter.component';

describe('ToggleFilterComponent', () => {
  it('cycles through tri-state values and emits each state', async () => {
    await TestBed.configureTestingModule({ imports: [ToggleFilterComponent] })
      .overrideComponent(ToggleFilterComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ToggleFilterComponent);
    fixture.componentRef.setInput('label', 'Deleted');
    fixture.detectChanges();
    const input = document.createElement('input');
    (fixture.componentInstance.inputElement as any) = () => ({ nativeElement: input });
    const states: string[] = [];
    fixture.componentInstance.filterChanged.subscribe((state) => states.push(state));
    fixture.componentInstance.onToggle();
    expect(input.indeterminate).toBe(true);
    fixture.componentInstance.onToggle();
    expect(input.checked).toBe(true);
    fixture.componentInstance.onToggle();
    expect(fixture.componentInstance.state()).toBe('unchecked');
    expect(states).toEqual(['indeterminate', 'checked', 'unchecked']);
  });
});
