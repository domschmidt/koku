import { describe, expect, it } from 'vitest';
import { get } from './get';

describe('get', () => {
  it('reads dotted, bracket and array paths', () => {
    const target = { customers: { 0: { name: 'Ada', active: true } } };
    expect(get(target, 'customers[0].name')).toBe('Ada');
    expect(get(target, ['customers', '1'], 'missing')).toBe('missing');
    expect(() => get(target, '')).toThrow('Unable to read path');
  });
});
