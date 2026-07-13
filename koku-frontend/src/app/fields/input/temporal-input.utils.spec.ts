import dayjs from 'dayjs';
import isoWeek from 'dayjs/plugin/isoWeek';
import { describe, expect, it } from 'vitest';
import {
  formatIsoWeekDisplay,
  formatIsoWeekValue,
  invalidDayjs,
  normalizeShortYear,
  parseIsoWeekValue,
  supportsNativeTemporalInput,
} from './temporal-input.utils';

dayjs.extend(isoWeek);

describe('temporal input utilities', () => {
  it('normalizes years and validates ISO weeks', () => {
    expect(normalizeShortYear('2026')).toBe('2026');
    expect(normalizeShortYear('26')).toBe('2026');
    expect(normalizeShortYear('76')).toBe('1976');
    expect(parseIsoWeekValue('2026-W01').isValid()).toBe(true);
    expect(parseIsoWeekValue('invalid').isValid()).toBe(false);
    expect(parseIsoWeekValue('2026-W99').isValid()).toBe(false);
    const week = parseIsoWeekValue('2026-W02');
    expect(formatIsoWeekValue(week)).toBe('2026-W02');
    expect(formatIsoWeekDisplay(week)).toBe('KW 02 / 2026');
    expect(invalidDayjs().isValid()).toBe(false);
    expect(supportsNativeTemporalInput('date')).toBe(true);
  });
});
