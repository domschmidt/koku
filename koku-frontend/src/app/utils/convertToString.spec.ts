import { describe, expect, it } from 'vitest';
import { convertToString } from './convertToString';

describe('convertToString', () => {
  it('converts primitives, functions and objects to stable strings', () => {
    expect(convertToString(null)).toBe('null');
    expect(convertToString(undefined)).toBe('undefined');
    expect(convertToString('text')).toBe('text');
    expect(convertToString(42)).toBe('42');
    expect(convertToString(true)).toBe('true');
    expect(convertToString(12n)).toBe('12');
    expect(convertToString(Symbol('value'))).toBe('Symbol(value)');
    expect(
      convertToString(function named() {
        return undefined;
      }),
    ).toBe('[function named]');
    expect(convertToString(() => undefined)).toBe('[function anonymous]');
    expect(convertToString({ value: 1 })).toBe('{"value":1}');
    const circular: Record<string, unknown> = {};
    circular['self'] = circular;
    expect(convertToString(circular)).toBe('[object Object]');
  });
});
