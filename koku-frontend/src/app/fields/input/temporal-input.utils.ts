import dayjs from 'dayjs';

export interface PickerPosition {
  left: number;
  top: number;
}

export function supportsNativeTemporalInput(type: 'date' | 'month' | 'time' | 'week'): boolean {
  const input = document.createElement('input');
  input.type = type;
  return input.type === type;
}

export function invalidDayjs(): dayjs.Dayjs {
  return dayjs('', 'YYYY-MM-DD', true);
}

export function normalizeShortYear(value: string): string {
  if (value.length === 4) {
    return value;
  }
  const year = Number(value);
  return year < 50 ? `20${value}` : `19${value}`;
}

export function parseIsoWeekValue(value: string): dayjs.Dayjs {
  const match = /^(\d{4})-W(\d{2})$/.exec(value);
  if (!match) {
    return invalidDayjs();
  }
  const year = Number(match[1]);
  const week = Number(match[2]);
  const parsedWeek = dayjs(new Date(year, 0, 4))
    .add(week - 1, 'week')
    .startOf('isoWeek');
  return parsedWeek.isoWeekYear() === year && parsedWeek.isoWeek() === week ? parsedWeek : invalidDayjs();
}

export function formatIsoWeekValue(value: dayjs.Dayjs): string {
  return `${value.isoWeekYear()}-W${String(value.isoWeek()).padStart(2, '0')}`;
}

export function formatIsoWeekDisplay(value: dayjs.Dayjs): string {
  return `KW ${String(value.isoWeek()).padStart(2, '0')} / ${value.isoWeekYear()}`;
}
