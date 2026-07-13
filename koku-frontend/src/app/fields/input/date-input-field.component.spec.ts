import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { DateInputFieldComponent } from './date-input-field.component';

const create = (value: string | null = '') => {
  const fixture = TestBed.createComponent(DateInputFieldComponent);
  fixture.componentRef.setInput('value', value);
  fixture.detectChanges();
  return fixture;
};

describe('DateInputFieldComponent', () => {
  it('formats, emits and validates localized and compact input', () => {
    const fixture = create('2026-07-13');
    const component = fixture.componentInstance;
    const typed: string[] = [];
    const changed: (string | null)[] = [];
    component.typed.subscribe((value) => typed.push(value));
    component.changed.subscribe((value) => changed.push(value));
    expect(component.nativePickerValue()).toBe('2026-07-13');
    expect(component.validate()).toBe(true);
    component.typeDateInput('7/13/26');
    expect(component.displayValue()).toContain('2026');
    component.changeDateInput('07132026');
    component.changeDateInput('99999999');
    component.selectFromNativePicker({ target: { value: '2026-08-14' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: '' } } as unknown as Event);
    component.selectFromNativePicker({ target: { value: 'invalid' } } as unknown as Event);
    expect(typed).toContain('2026-07-13');
    expect(typed).toContain('2026-08-14');
    expect(changed).toContain(null);
    fixture.componentRef.setInput('value', '');
    fixture.componentRef.setInput('required', true);
    expect(component.validate()).toBe(false);
    fixture.componentRef.setInput('required', false);
    fixture.componentRef.setInput('defaultValue', '2025-01-02');
    fixture.componentRef.setInput('loading', true);
    expect(component.nativePickerValue()).toBe('2025-01-02');
  });

  it('accepts alternate representations and validates real calendar dates', () => {
    const component = create().componentInstance as any;
    for (const value of ['13.07.2026', '7/3/26', '2026-07-13', '07132026', '71326', '', 'letters']) {
      component.normalizeDateInputValue(value);
      component.maskDateInput(value);
      component.formatDateInputValue(value);
      component.toDateInputOutputValue(value);
      component.toDateInputChangeValue(value);
    }
    expect(component.partsCreateValidDate({ year: '2026', month: '02', day: '29' })).toBe(false);
    expect(component.partsCreateValidDate({ year: '2024', month: '02', day: '29' })).toBe(true);
    expect(component.padDatePart(undefined, 2)).toBeNull();
    expect(component.normalizeYear(undefined)).toBeNull();
    expect(component.createFormatDefinition('de-DE')).toBeTruthy();
    expect(component.createFormatDefinition('en-US')).toBeTruthy();
    const formatToParts = vi
      .spyOn(Intl.DateTimeFormat.prototype, 'formatToParts')
      .mockReturnValue([{ type: 'literal', value: '-' }]);
    expect(component.createFormatDefinition('en-US')).toEqual(expect.objectContaining({ dayjsFormat: 'DD.MM.YYYY' }));
    formatToParts.mockRestore();
    expect(component.readNumericDateParts('130726')).toBeTruthy();
    expect(component.readNumericDateParts('1307')).toBeTruthy();
    expect(component.partsCreateValidDate({})).toBe(false);
    expect(component.formatDateInputValue('')).toBe('');
    expect(component.isDateInputValueValid('invalid')).toBe(false);
  });

  it('opens the native picker only while editable', () => {
    const fixture = create('2026-07-13');
    const picker = fixture.nativeElement.querySelector('input[type="date"]') as HTMLInputElement;
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
    fixture.componentRef.setInput('readonly', false);
    const showPicker = vi.fn();
    picker.showPicker = showPicker;
    fixture.componentInstance.openNativePicker(event);
    expect(showPicker).toHaveBeenCalledOnce();
    (fixture.componentInstance as any).nativePicker = () => undefined;
    fixture.componentInstance.openNativePicker(event);
  });
});
