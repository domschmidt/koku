import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { YearMonthFieldComponent } from './year-month-field.component';

describe('YearMonthFieldComponent', () => {
  it('commits changes on blur only when dirty', () => {
    const fixture = TestBed.createComponent(YearMonthFieldComponent);
    fixture.componentRef.setInput('value', '2026-07');
    fixture.detectChanges();
    const typed = vi.fn();
    const changed = vi.fn();
    const blurred = vi.fn();
    fixture.componentInstance.typed.subscribe(typed);
    fixture.componentInstance.changed.subscribe(changed);
    fixture.componentInstance.blurred.subscribe(blurred);
    fixture.componentInstance.blurredRaw();
    fixture.componentInstance.typeRaw({ target: { value: '2026-08' } } as unknown as Event);
    fixture.componentInstance.blurredRaw();
    expect(typed).toHaveBeenCalledWith('2026-08');
    expect(changed).toHaveBeenCalledOnce();
    expect(changed).toHaveBeenCalledWith('2026-08');
    expect(blurred).toHaveBeenCalledTimes(2);
  });
});
