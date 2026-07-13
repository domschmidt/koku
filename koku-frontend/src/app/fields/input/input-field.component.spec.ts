import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { InputFieldComponent } from './input-field.component';

describe('InputFieldComponent', () => {
  it('emits and validates plain input values', () => {
    const fixture = TestBed.createComponent(InputFieldComponent);
    fixture.componentRef.setInput('value', '');
    fixture.componentRef.setInput('required', true);
    fixture.detectChanges();
    const typed = vi.fn();
    const changed = vi.fn();
    fixture.componentInstance.typed.subscribe(typed);
    fixture.componentInstance.changed.subscribe(changed);
    fixture.componentInstance.typeRaw({ target: { value: 'Ada' } } as unknown as Event);
    fixture.componentInstance.changeRaw({ target: { value: 'Grace' } } as unknown as Event);
    fixture.componentInstance.typeRaw(new Event('input'));
    fixture.componentInstance.changeRaw(new Event('change'));
    expect(typed).toHaveBeenCalledWith('Ada');
    expect(changed).toHaveBeenCalledWith('Grace');
    expect(fixture.componentInstance.validate()).toBe(false);
    fixture.componentRef.setInput('value', 'Ada');
    expect(fixture.componentInstance.validate()).toBe(true);
  });
});
