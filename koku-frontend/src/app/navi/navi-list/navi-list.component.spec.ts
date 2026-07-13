import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { describe, expect, it } from 'vitest';
import { NaviListComponent } from './navi-list.component';

describe('NaviListComponent', () => {
  it('converts visible nested routes into top and bottom navigation entries', async () => {
    const routeConfig = {
      children: [
        {
          path: 'customers',
          title: 'Customers',
          data: { naviIcon: 'users', naviDivider: true },
          children: [{ path: ':id', title: 'Details' }],
        },
        { path: 'settings', title: 'Settings', data: { naviAlign: 'bottom' } },
        { path: 'hidden', title: 'Hidden', data: { hideInNav: true } },
        { path: '**', title: 'Fallback' },
      ],
    };
    await TestBed.configureTestingModule({
      imports: [NaviListComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { routeConfig } } },
        { provide: Router, useValue: {} },
      ],
    })
      .overrideComponent(NaviListComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(NaviListComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.topNavi()).toEqual([
      expect.objectContaining({ path: 'customers', text: 'Customers', icon: 'users' }),
      expect.objectContaining({ type: 'divider' }),
    ]);
    expect((fixture.componentInstance.topNavi()[0] as any).children[0].path).toBe('customers/:id');
    expect(fixture.componentInstance.bottomNavi()).toEqual([expect.objectContaining({ path: 'settings' })]);
  });
});
