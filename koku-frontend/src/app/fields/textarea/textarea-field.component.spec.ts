import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { TextareaFieldComponent } from './textarea-field.component';

describe('TextareaFieldComponent', () => {
  afterEach(() => vi.useRealTimers());

  it('emits, auto-sizes and validates values', () => {
    vi.useFakeTimers();
    Object.defineProperty(globalThis, 'CSS', { configurable: true, value: { supports: vi.fn(() => false) } });
    const fixture = TestBed.createComponent(TextareaFieldComponent);
    fixture.componentRef.setInput('value', '');
    fixture.componentRef.setInput('name', 'notes');
    fixture.componentRef.setInput('required', true);
    fixture.detectChanges();
    const typed = vi.fn();
    const changed = vi.fn();
    fixture.componentInstance.typed.subscribe(typed);
    fixture.componentInstance.changed.subscribe(changed);
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    textarea.value = 'Notes';
    Object.defineProperty(textarea, 'scrollHeight', { configurable: true, value: 80 });
    fixture.componentInstance.typeRaw({ target: textarea } as unknown as Event);
    fixture.componentInstance.changeRaw({ target: textarea } as unknown as Event);
    vi.runAllTimers();
    expect(typed).toHaveBeenCalledWith('Notes');
    expect(changed).toHaveBeenCalledWith('Notes');
    expect(textarea.style.height).toContain('80px');
    expect(fixture.componentInstance.validate()).toBe(false);
    fixture.componentRef.setInput('value', 'Notes');
    expect(fixture.componentInstance.validate()).toBe(true);
  });
});
