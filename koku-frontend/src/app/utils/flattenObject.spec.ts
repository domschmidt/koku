import { describe, expect, it } from 'vitest';
import { flattenToArrayOfObjects } from './flattenObject';

describe('flattenToArrayOfObjects', () => {
  it('flattens arrays, plain objects and terminal values', () => {
    expect(flattenToArrayOfObjects({ customer: { names: ['Ada', 'Grace'], active: true } })).toEqual([
      { 'customer.names.0': 'Ada' },
      { 'customer.names.1': 'Grace' },
      { 'customer.active': 'true' },
    ]);
    expect(flattenToArrayOfObjects(new Date(0), 'created')).toEqual([{ created: '"1970-01-01T00:00:00.000Z"' }]);
  });
});
