import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { createStableRecipe, requireRecipeFactory } from './recipe.utils';

describe('recipe utilities', () => {
  it('requires registered factories and preserves recipes for equal identities', () => {
    expect(requireRecipeFactory({ known: 'factory' }, 'known', 'test')).toBe('factory');
    expect(() => requireRecipeFactory({}, 'missing', 'test')).toThrow(
      'No test recipe registered for content type: missing',
    );

    TestBed.runInInjectionContext(() => {
      const value = signal({ id: 1 });
      const equal = vi.fn((previous, current) => previous.id === current.id);
      const create = vi.fn((identity) => ({ identity }));
      const recipe = createStableRecipe({ identity: value, equal, create });
      const first = recipe();
      value.set({ id: 1 });
      expect(recipe()).toBe(first);
      expect(equal).toHaveBeenCalled();
      expect(create).toHaveBeenCalledOnce();
    });
  });
});
