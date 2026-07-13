import { signal } from '@angular/core';
import { describe, expect, it } from 'vitest';
import { DASHBOARD_CONTENT_REGISTRY } from './registry';

describe('dashboard content registry contract', () => {
  it('renders every dashboard content type', async () => {
    for (const [type, factory] of Object.entries(DASHBOARD_CONTENT_REGISTRY)) {
      const content = { '@type': type, chartUrl: '/chart', content: [] };
      const recipe = factory!({
        content: signal(content as any),
        contentRegistry: signal(DASHBOARD_CONTENT_REGISTRY),
      });
      expect(await recipe.loadComponent!()).toBeDefined();
      expect(recipe.inputs?.()).toBeTypeOf('object');
    }
  });
});
