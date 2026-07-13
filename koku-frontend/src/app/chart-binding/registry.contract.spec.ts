import { signal } from '@angular/core';
import { describe, expect, it, vi } from 'vitest';
import { CHART_FILTER_REGISTRY } from './registry';

describe('chart filter registry contract', () => {
  it('renders every filter and forwards changes', async () => {
    for (const [type, factory] of Object.entries(CHART_FILTER_REGISTRY)) {
      const emit = vi.fn();
      const recipe = factory!({
        content: signal({ '@type': type, value: '2026-07-13', label: type, placeholder: 'value', type: 'TEXT' } as any),
        loading: signal(true),
        emit,
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      expect(recipe.inputs?.()).toEqual(expect.objectContaining({ loading: true, label: type }));
      recipe.outputs?.['changed']?.('updated');
      expect(emit).toHaveBeenCalledWith('updated');
    }
  });
});
