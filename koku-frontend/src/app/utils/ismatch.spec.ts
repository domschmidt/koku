import { describe, expect, it } from 'vitest';
import { isMatch } from './ismatch';

describe('isMatch', () => {
  it('matches nested partial objects and unordered array subsets', () => {
    const target = { id: 1, tags: ['a', 'b'], nested: { active: true } };
    expect(isMatch(target, target)).toBe(true);
    expect(isMatch(target, { tags: ['b'], nested: { active: true } })).toBe(true);
    expect(isMatch(['a', 'b'], ['b'])).toBe(true);
    expect(isMatch({}, ['a'])).toBe(false);
    expect(isMatch(target, { missing: true })).toBe(false);
    expect(isMatch(target, { tags: ['c'] })).toBe(false);
    expect(isMatch(target, { nested: { active: false } })).toBe(false);
    expect(isMatch(2, 1)).toBe(false);
  });
});
