import { describe, expect, it } from 'vitest';
import { deepEqual } from './deepEqual';

describe('deepEqual', () => {
  it('compares nested objects and arrays structurally', () => {
    expect(deepEqual({ a: [1, { b: true }] }, { a: [1, { b: true }] })).toBe(true);
    expect(deepEqual(1, 1)).toBe(true);
    expect(deepEqual(1, '1')).toBe(false);
    expect(deepEqual(null, {})).toBe(false);
    expect(deepEqual([], {})).toBe(false);
    expect(deepEqual({ a: 1 }, { a: 1, b: 2 })).toBe(false);
    expect(deepEqual({ a: 1 }, { b: 1 })).toBe(false);
    expect(deepEqual({ a: 1 }, { a: 2 })).toBe(false);
  });
});
