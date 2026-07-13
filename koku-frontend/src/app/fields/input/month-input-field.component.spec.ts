import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { MonthInputFieldComponent } from './month-input-field.component';

const create = (value: string | null = '') => {
  const fixture = TestBed.createComponent(MonthInputFieldComponent);
  fixture.componentRef.setInput('value', value);
  fixture.detectChanges();
  return fixture;
};

describe('MonthInputFieldComponent', () => {
  it('normalizes numeric, named and native values', () => {
    const fixture = create('2026-07');
    const component = fixture.componentInstance;
    const typed = vi.fn();
    const changed = vi.fn();
    component.typed.subscribe(typed);
    component.changed.subscribe(changed);
    component.typeMonthInput('July 26');
    component.changeMonthInput('7/26');
    component.changeMonthInput('072026');
    component.changeMonthInput('invalid');
    component.selectFromNativePicker({ target: { value: '2026-08' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: '' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: 'invalid' } } as unknown as Event);
    expect(typed).toHaveBeenCalledWith('2026-07');
    expect(typed).toHaveBeenCalledWith('2026-08');
    expect(changed).toHaveBeenCalledWith(null);
    fixture.componentRef.setInput('value', '');
    fixture.componentRef.setInput('required', true);
    expect(component.validate()).toBe(false);
  });

  it('accepts alternate localized representations', () => {
    const component = create().componentInstance as any;
    for (const value of ['Juli 2026', 'jul 26', '07/26', '202607', '072026', '7', '', 'unknown']) {
      component.normalizeValue(value);
      component.maskValue(value);
      component.formatValue(value);
      component.toOutputValue(value);
      component.toChangeValue(value);
      component.readNamedMonthValue(value);
    }
    expect(component.normalizeYear('26')).toBe('2026');
    expect(component.normalizeText('M\u00c4RZ')).toBe('marz');
    expect(component.normalizeYear('2026')).toBe('2026');
    expect(component.normalizeValue('726')).toBe('2026-07');
    expect(component.normalizeValue('0726')).toBe('2026-07');
    expect(component.maskValue('07/2026')).toBe('07/2026');
    expect(component.readNamedMonthValue('July nope')).toBeNull();
    expect(component.readNamedMonthValue('Unknown 2026')).toBeNull();
  });

  it('covers display, loading and validation states', () => {
    const fixture = create('2026-07');
    const component = fixture.componentInstance as any;
    component.typeMonthInput('July 2026');
    expect(component.displayValue()).toMatch(/2026/);
    fixture.componentRef.setInput('defaultValue', '2027-08');
    fixture.componentRef.setInput('loading', true);
    expect(component.nativePickerValue()).toBe('2027-08');
    expect(component.defaultDisplayValue()).toMatch(/2027/);
    expect(component.placeholderValue()).toMatch(/JJJJ/);
    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('value', '');
    expect(component.validate()).toBe(true);
    fixture.componentRef.setInput('required', true);
    expect(component.validate()).toBe(false);
    fixture.componentRef.setInput('value', '2026-99');
    expect(component.validate()).toBe(false);
    expect(component.formatValue('')).toBe('');
  });

  it('opens the native picker only while editable', () => {
    const fixture = create();
    const picker = fixture.nativeElement.querySelector('input[type="month"]');
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
