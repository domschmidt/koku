import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { CheckboxFieldComponent } from './checkbox-field.component';

describe('CheckboxFieldComponent', () => {
  it('emits checkbox state and is structurally valid', () => {
    const fixture = TestBed.createComponent(CheckboxFieldComponent);
    fixture.componentRef.setInput('value', false);
    fixture.componentRef.setInput('name', 'active');
    fixture.detectChanges();
    const typed = vi.fn();
    const changed = vi.fn();
    fixture.componentInstance.typed.subscribe(typed);
    fixture.componentInstance.changed.subscribe(changed);
    fixture.componentInstance.typeRaw({ target: { checked: true } } as unknown as Event);
    fixture.componentInstance.changeRaw({ target: { checked: false } } as unknown as Event);
    expect(typed).toHaveBeenCalledWith(true);
    expect(changed).toHaveBeenCalledWith(false);
    expect(fixture.componentInstance.validate()).toBe(true);
  });
});
