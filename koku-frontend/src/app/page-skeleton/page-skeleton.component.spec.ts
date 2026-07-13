import { TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { describe, expect, it } from 'vitest';
import { NaviService } from '../navi/navi.service';
import { PageSkeletonComponent } from './page-skeleton.component';

describe('PageSkeletonComponent', () => {
  it('maps drawer changes to navigation state', async () => {
    const navi = new NaviService();
    await TestBed.configureTestingModule({
      imports: [PageSkeletonComponent],
      providers: [
        { provide: NaviService, useValue: navi },
        { provide: ActivatedRoute, useValue: {} },
      ],
    })
      .overrideComponent(PageSkeletonComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(PageSkeletonComponent);
    const input = document.createElement('input');
    input.checked = true;
    fixture.componentInstance.onChange({ target: input } as any);
    expect(navi.openState()).toBe(true);
    input.checked = false;
    fixture.componentInstance.onChange({ target: input } as any);
    expect(navi.openState()).toBe(false);
    fixture.componentInstance.onChange({ target: null } as any);
  });
});
