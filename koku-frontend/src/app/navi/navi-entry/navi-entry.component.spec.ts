import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { NaviService } from '../navi.service';
import { NaviEntryComponent } from './navi-entry.component';

describe('NaviEntryComponent', () => {
  it('binds navigation entries and shares navigation state', async () => {
    const naviService = new NaviService();
    await TestBed.configureTestingModule({
      imports: [NaviEntryComponent],
      providers: [{ provide: NaviService, useValue: naviService }],
    })
      .overrideComponent(NaviEntryComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(NaviEntryComponent);
    fixture.componentRef.setInput('naviEntry', { type: 'link', title: 'Home' });
    fixture.detectChanges();
    expect(fixture.componentInstance.depth()).toBe(0);
    expect(fixture.componentInstance.naviService).toBe(naviService);
  });
});
