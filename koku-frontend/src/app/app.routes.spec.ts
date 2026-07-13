import { describe, expect, it } from 'vitest';
import { routes } from './app.routes';

describe('application routes', () => {
  it('exposes principal routes and resolves every lazy component contract', async () => {
    const children = routes[0].children ?? [];
    expect(children.map((route) => route.path)).toEqual(
      expect.arrayContaining(['calendar', 'files', 'manage', 'administration', 'statistics', 'myprofile', 'logout']),
    );
    const calendar = children.find((route) => route.path === 'calendar')!;
    expect(calendar.children?.[0].data?.['config'].listSources).toHaveLength(4);
    const manage = children.find((route) => route.path === 'manage')!;
    expect(manage.children?.[0].children?.map((route) => route.path)).toEqual(
      expect.arrayContaining(['customers', 'products', 'activities', 'promotions']),
    );

    const lazyRoutes: any[] = [];
    const collect = (entries: any[]) => {
      for (const entry of entries) {
        if (entry.loadComponent) lazyRoutes.push(entry);
        collect(entry.children ?? []);
      }
    };
    collect(routes as any[]);
    const loaded = await Promise.all(lazyRoutes.map((route) => route.loadComponent()));
    expect(loaded).toHaveLength(lazyRoutes.length);
    expect(loaded.every((component) => typeof component === 'function')).toBe(true);
  });
});
