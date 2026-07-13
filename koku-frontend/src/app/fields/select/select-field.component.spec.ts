import { ElementRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ToastService } from '../../toast/toast.service';
import { SelectFieldComponent } from './select-field.component';

async function createSelect(values: any[] = []) {
  const toast = { add: vi.fn() };
  await TestBed.configureTestingModule({
    imports: [SelectFieldComponent],
    providers: [{ provide: ToastService, useValue: toast }],
  })
    .overrideComponent(SelectFieldComponent, { set: { template: '' } })
    .compileComponents();
  const fixture = TestBed.createComponent(SelectFieldComponent);
  fixture.componentRef.setInput('name', 'customer');
  fixture.componentRef.setInput('value', '2');
  fixture.componentRef.setInput('possibleValues', values);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance, toast };
}

describe('SelectFieldComponent', () => {
  const values = [
    { id: '1', text: 'Ada Lovelace', category: 'Customers' },
    { id: '2', text: 'Grace Hopper', category: 'Customers' },
    { id: '3', text: 'Disabled', disabled: true },
  ];

  it('synchronizes values, filters options and groups visible results', async () => {
    const { fixture, component } = await createSelect(values);
    expect(component.selectedId()).toBe('2');
    expect(component.searchTerm()).toBe('Grace Hopper');
    expect(component.selectedIdx()).toBe(0);
    expect(component.filteredPossibleValues()).toHaveLength(1);
    component.searchTerm.set('');
    expect(component.filteredPossibleValuesGrouped()['Customers']).toHaveLength(2);

    component.typeRaw({ target: { value: 'ada love' } } as unknown as Event);
    expect(component.selectedId()).toBeNull();
    expect(component.filteredPossibleValues().map((item) => item.id)).toEqual(['1']);
    expect(component.showDropdown()).not.toBeNull();

    fixture.componentRef.setInput('value', 999);
    fixture.detectChanges();
    expect(component.value()).toBe('999');
    expect(component.selectedId()).toBeNull();
    expect(component.searchTerm()).toBe('');
    expect(component.validate()).toBe(true);
    fixture.componentRef.setInput('required', true);
    fixture.componentRef.setInput('value', '');
    fixture.detectChanges();
    expect(component.validate()).toBe(false);
  });

  it('supports keyboard navigation, selection modes and disabled options', async () => {
    const { fixture, component, toast } = await createSelect(values);
    const changed = vi.fn();
    component.changed.subscribe(changed);
    const focus = vi.fn();
    component.searchInput = new ElementRef({ focus }) as unknown as ElementRef<HTMLInputElement>;

    component.showDropdown.set(null);
    component.searchTerm.set('');
    component.onKeyDownRaw(new KeyboardEvent('keydown', { key: 'x' }));
    expect(component.showDropdown()).toBeNull();
    component.onKeyDownRaw(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    expect(component.showDropdown()).not.toBeNull();
    component.selectedId.set(null);
    component.onKeyDownRaw(new KeyboardEvent('keydown', { key: 'ArrowDown' }));
    expect(component.selectedId()).toBe('1');
    component.onKeyDownRaw(new KeyboardEvent('keydown', { key: 'ArrowUp' }));
    expect(component.selectedId()).toBe('2');
    component.onKeyDownRaw(new KeyboardEvent('keydown', { key: 'Enter' }));
    expect(changed).toHaveBeenCalledWith('2');
    expect(component.showDropdown()).toBeNull();

    component.showDropdown.set({ direction: 'bottom' });
    component.onKeyDownRaw(new KeyboardEvent('keydown', { key: 'Escape' }));
    expect(component.selectedId()).toBeNull();
    expect(component.showDropdown()).toBeNull();

    fixture.componentRef.setInput('clearOnSelect', true);
    fixture.componentRef.setInput('keepOpenOnSelect', true);
    fixture.detectChanges();
    component.selectOption(values[0]);
    expect(component.searchTerm()).toBe('');
    expect(focus).toHaveBeenCalled();
    component.selectOption(values[2]);
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Option deaktiviert'), 'error');
  });

  it('handles focus, blur, pointer selection and dropdown placement', async () => {
    const { component } = await createSelect(values);
    const changed = vi.fn();
    const blurred = vi.fn();
    const focused = vi.fn();
    component.changed.subscribe(changed);
    component.blurred.subscribe(blurred);
    component.focused.subscribe(focused);
    const focusEvent = new Event('focus');
    component.focusedRaw(focusEvent);
    expect(focused).toHaveBeenCalledWith(focusEvent);

    vi.spyOn(component.elementRef.nativeElement, 'getBoundingClientRect').mockReturnValue({
      bottom: globalThis.innerHeight - 100,
    } as DOMRect);
    component.typeRaw({ target: { value: 'unknown' } } as unknown as Event);
    expect(component.showDropdown()?.direction).toBe('top');
    const blurEvent = new Event('blur');
    component.blurredRaw(blurEvent);
    expect(changed).toHaveBeenCalledWith(null);
    expect(blurred).toHaveBeenCalledWith(blurEvent);

    component.onOptionPointerDown();
    component.showDropdown.set({ direction: 'bottom' });
    component.blurredRaw(blurEvent);
    expect(component.showDropdown()).toEqual({ direction: 'bottom' });
    component.searchTerm.set('Grace');
    component.selectedId.set('2');
    component.blurredRaw(blurEvent);
    expect(component.searchTerm()).toBe('Grace Hopper');
  });
});
