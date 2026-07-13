import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { TimeInputFieldComponent } from './time-input-field.component';

const create = (value: string | null = '') => {
  const fixture = TestBed.createComponent(TimeInputFieldComponent);
  fixture.componentRef.setInput('value', value);
  fixture.detectChanges();
  return fixture;
};

describe('TimeInputFieldComponent', () => {
  it('normalizes compact, seconds and native values', () => {
    const component = create('09:30').componentInstance;
    const typed = vi.fn();
    const changed = vi.fn();
    component.typed.subscribe(typed);
    component.changed.subscribe(changed);
    component.typeTimeInput('930');
    component.changeTimeInput('9:30:15');
    component.changeTimeInput('9999');
    component.selectFromNativePicker({ target: { value: '10:45' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: '' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: '99:99' } } as unknown as Event);
    expect(typed).toHaveBeenCalledWith('09:30');
    expect(typed).toHaveBeenCalledWith('10:45');
    expect(changed).toHaveBeenCalledWith(null);
  });

  it('accepts and rejects alternate representations', () => {
    const component = create().componentInstance as any;
    for (const value of ['9', '930', '09:30', '09:30:59', '2359', '2400', '', 'letters']) {
      component.normalizeValue(value);
      component.maskValue(value);
      component.formatValue(value);
      component.toOutputValue(value);
      component.toChangeValue(value);
    }
    expect(component.normalizeValue('09')).toBe('09:00');
    expect(component.normalizeValue('123')).toBe('12:30');
  });

  it('covers display, loading and validation states', () => {
    const fixture = create('09:30');
    const component = fixture.componentInstance as any;
    component.typeTimeInput('930');
    expect(component.displayValue()).toBe('09:30');
    fixture.componentRef.setInput('defaultValue', '10:15');
    fixture.componentRef.setInput('loading', true);
    expect(component.nativePickerValue()).toBe('10:15');
    expect(component.defaultDisplayValue()).toBe('10:15');
    expect(component.placeholderValue()).toBe('HH:MM');
    fixture.componentRef.setInput('loading', false);
    fixture.componentRef.setInput('value', '');
    expect(component.validate()).toBe(true);
    fixture.componentRef.setInput('required', true);
    expect(component.validate()).toBe(false);
    fixture.componentRef.setInput('value', '25:00');
    expect(component.validate()).toBe(false);
    fixture.componentRef.setInput('value', '23:00');
    expect(component.validate()).toBe(true);
    expect(component.formatValue('')).toBe('');
  });

  it('opens the native picker only while editable', () => {
    const fixture = create();
    const picker = fixture.nativeElement.querySelector('input[type="time"]');
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
