import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { MultiSelectFieldComponent } from './multi-select-field.component';

const possibleValues = [
  { id: 'one', valueMapping: { id: 'one', label: 'One' } },
  { id: 'two', valueMapping: { id: 'two', label: 'Two' } },
  { id: 'three' },
] as any[];

describe('MultiSelectFieldComponent', () => {
  afterEach(() => vi.useRealTimers());

  it('adds, moves, deletes and filters unique mapped selections', () => {
    vi.useFakeTimers();
    const fixture = TestBed.createComponent(MultiSelectFieldComponent);
    fixture.componentRef.setInput('name', 'items');
    fixture.componentRef.setInput('possibleValues', possibleValues);
    fixture.componentRef.setInput('uniqueValues', true);
    fixture.componentRef.setInput('value', [{ id: 'one', label: 'One' }]);
    fixture.detectChanges();
    const changed = vi.fn();
    fixture.componentInstance.changed.subscribe(changed);
    expect(fixture.componentInstance.selectionIds()).toEqual(['one']);
    expect(fixture.componentInstance.filteredPossibleValues().map((value) => value.id)).toEqual(['two', 'three']);
    fixture.componentInstance.selectById('two');
    fixture.componentInstance.selectById(null);
    fixture.componentInstance.drop({ previousIndex: 0, currentIndex: 1 } as any);
    const keyboardEvent = { key: 'ArrowUp', preventDefault: vi.fn() } as unknown as KeyboardEvent;
    fixture.componentInstance.selectItemKeyDownRaw(keyboardEvent, 'one');
    fixture.componentInstance.selectItemKeyDownRaw(
      { key: 'ArrowDown', preventDefault: vi.fn() } as unknown as KeyboardEvent,
      'one',
    );
    fixture.componentInstance.selectItemKeyDownRaw(
      { key: 'Delete', preventDefault: vi.fn(), stopPropagation: vi.fn() } as unknown as KeyboardEvent,
      'one',
    );
    vi.runAllTimers();
    expect(changed).toHaveBeenCalled();
    expect(fixture.componentInstance.selectionIds()).not.toContain('one');
    expect(keyboardEvent.preventDefault).toHaveBeenCalled();
    fixture.destroy();
  });

  it('normalizes numeric ids, emits scalar values and focuses adjacent badges', () => {
    vi.useFakeTimers();
    const fixture = TestBed.createComponent(MultiSelectFieldComponent);
    fixture.componentRef.setInput('name', 'items');
    fixture.componentRef.setInput('possibleValues', [
      { id: 1, valueMapping: { numeric: true } },
      { id: 'plain' },
    ] as any);
    fixture.componentRef.setInput('value', [{ numeric: true }]);
    fixture.detectChanges();
    const component = fixture.componentInstance as any;
    expect(component.selectionIds()).toEqual(['1']);
    expect(component.possibleValuesIdx()['1'].id).toBe(1);
    const focus = vi.fn();
    component.badgeEls = { get: vi.fn(() => ({ nativeElement: { focus } })) };
    component.selectionIds.set(['1', 'plain']);
    component.selectItemKeyDownRaw({ key: 'ArrowUp', preventDefault: vi.fn() } as unknown as KeyboardEvent, 'plain');
    component.selectItemKeyDownRaw(
      { key: 'Delete', preventDefault: vi.fn(), stopPropagation: vi.fn() } as unknown as KeyboardEvent,
      'plain',
    );
    vi.runAllTimers();
    expect(focus).toHaveBeenCalled();
    component.emitOnChange(['plain']);
    expect(() => component.emitOnChange(['missing'])).toThrow('selectionId not found');
    expect(() => component.requireMatchingValue(undefined)).toThrow('Unknown value to match');
  });

  it('maps scalar selections through an id path', () => {
    const fixture = TestBed.createComponent(MultiSelectFieldComponent);
    fixture.componentRef.setInput('name', 'items');
    fixture.componentRef.setInput('possibleValues', possibleValues);
    fixture.componentRef.setInput('idPathMapping', 'product.id');
    fixture.componentRef.setInput('value', [{ product: { id: 'three' } }]);
    fixture.detectChanges();
    const changed = vi.fn();
    fixture.componentInstance.changed.subscribe(changed);
    fixture.componentInstance.selectById('three');
    expect(changed).toHaveBeenCalledWith([{ product: { id: 'three' } }, { product: { id: 'three' } }]);
    fixture.destroy();
  });
});
