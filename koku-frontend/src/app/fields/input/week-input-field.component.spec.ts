import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { WeekInputFieldComponent } from './week-input-field.component';

const create = (value: string | null = '') => {
  const fixture = TestBed.createComponent(WeekInputFieldComponent);
  fixture.componentRef.setInput('value', value);
  fixture.detectChanges();
  return fixture;
};

describe('WeekInputFieldComponent', () => {
  it('normalizes compact, display and native values', () => {
    const component = create('2026-W02').componentInstance;
    const typed = vi.fn();
    const changed = vi.fn();
    component.typed.subscribe(typed);
    component.changed.subscribe(changed);
    component.typeWeekInput('022026');
    component.changeWeekInput('022026');
    component.changeWeekInput('992026');
    component.selectFromNativePicker({ target: { value: '2026-W03' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: '' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: '2026-W99' } } as unknown as Event);
    expect(typed).toHaveBeenCalledWith('2026-W02');
    expect(typed).toHaveBeenCalledWith('2026-W03');
    expect(changed).toHaveBeenCalledWith(null);
  });

  it('accepts and rejects alternate representations', () => {
    const component = create().componentInstance as any;
    for (const value of ['KW 02 / 2026', '02/26', '022026', '2026-W02', '2 2026', '992026', '', 'letters']) {
      component.normalizeValue(value);
      component.maskValue(value);
      component.formatValue(value);
      component.toOutputValue(value);
      component.toChangeValue(value);
    }
    expect(component.normalizeYear('26')).toBe('2026');
    expect(component.normalizeYear('2026')).toBe('2026');
    expect(component.normalizeValue('126')).toBe('2026-W01');
    expect(component.normalizeValue('0226')).toBe('2026-W02');
    expect(component.normalizeValue('9926')).toBe('1992-W09');
  });

  it('covers display, loading and validation states', () => {
    const fixture = create('2026-W02');
    const component = fixture.componentInstance as any;
    component.typeWeekInput('022026');
    expect(component.displayValue()).toContain('02');
    fixture.componentRef.setInput('defaultValue', '2027-W03');
    fixture.componentRef.setInput('loading', true);
    expect(component.nativePickerValue()).toBe('2027-W03');
    expect(component.defaultDisplayValue()).toContain('03');
    expect(component.placeholderValue()).toBe('KW WW / JJJJ');
    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('value', '');
    expect(component.validate()).toBe(true);
    fixture.componentRef.setInput('required', true);
    expect(component.validate()).toBe(false);
    fixture.componentRef.setInput('value', '2026-W99');
    expect(component.validate()).toBe(false);
    expect(component.formatValue('')).toBe('');
  });

  it('opens the native picker only while editable', () => {
    const fixture = create();
    const picker = fixture.nativeElement.querySelector('input[type="week"]');
    vi.spyOn(picker, 'focus');
    vi.spyOn(picker, 'click');
    const event = { stopPropagation: vi.fn() } as unknown as Event;
    fixture.componentInstance.openNativePicker(event);
    fixture.componentRef.setInput('disabled', true);
    fixture.componentInstance.openNativePicker(event);
    fixture.componentRef.setInput('disabled', false);
    fixture.componentRef.setInput('readonly', true);
    fixture.componentInstance.openNativePicker(event);
    expect(event.stopPropagation).toHaveBeenCalledTimes(3);
    expect(picker.focus).toHaveBeenCalledTimes(1);
    fixture.componentRef.setInput('readonly', false);
    const showPicker = vi.fn();
    picker.showPicker = showPicker;
    fixture.componentInstance.openNativePicker(event);
    expect(showPicker).toHaveBeenCalledOnce();
    (fixture.componentInstance as any).nativePicker = () => undefined;
    fixture.componentInstance.openNativePicker(event);
  });
});
