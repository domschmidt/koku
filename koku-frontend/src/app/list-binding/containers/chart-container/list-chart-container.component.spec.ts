import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { ListChartContainerComponent } from './list-chart-container.component';

describe('ListChartContainerComponent', () => {
  it('binds chart source and filter registry', async () => {
    await TestBed.configureTestingModule({ imports: [ListChartContainerComponent] })
      .overrideComponent(ListChartContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListChartContainerComponent);
    fixture.componentRef.setInput('chartUrl', '/statistics');
    fixture.componentRef.setInput('filterRegistry', {});
    fixture.detectChanges();
    expect(fixture.componentInstance.chartUrl()).toBe('/statistics');
  });
});
