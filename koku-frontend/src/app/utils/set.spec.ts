import { describe, expect, it } from 'vitest';
import { set } from './set';

describe('set', () => {
  it('writes dotted, bracket and array paths', () => {
    const target: Record<string, any> = {};
    set(target, 'customers[0].name', 'Ada');
    set(target, ['customers', '0', 'active'], true);
    expect(target).toEqual({ customers: { 0: { name: 'Ada', active: true } } });
    expect(() => set(target, '', 1)).toThrow('Unable to read path');
  });
});
