import {convertToString} from './convertToString';

export function flattenToArrayOfObjects(
  input: unknown,
  prefix = ''
): { [key: string]: string }[] {
  const result: { [key: string]: string }[] = [];

  if (Array.isArray(input)) {
    input.forEach((val, idx) => {
      const newKey = prefix ? `${prefix}.${idx}` : String(idx);
      result.push(...flattenToArrayOfObjects(val, newKey));
    });
  } else if (
    input !== null &&
    typeof input === 'object' &&
    Object.prototype.toString.call(input) === '[object Object]'
  ) {
    Object.entries(input).forEach(([key, val]) => {
      const newKey = prefix ? `${prefix}.${key}` : key;
      result.push(...flattenToArrayOfObjects(val, newKey));
    });
  } else {
    result.push({ [prefix]: convertToString(input) });
  }

  return result;
}
