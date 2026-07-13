import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';

const apexMocks = vi.hoisted(() => {
  class ApexCharts {
    render = vi.fn();
    destroy = vi.fn();
    constructor(
      readonly element: HTMLElement,
      readonly options: unknown,
    ) {}
  }
  return { ApexCharts };
});
vi.mock('apexcharts', () => ({ default: apexMocks.ApexCharts }));
import { ToastService } from '../toast/toast.service';
import { ChartComponent } from './chart.component';

describe('ChartComponent', () => {
  it('builds complete Apex options for bar, line, pie and unknown charts', async () => {
    await TestBed.configureTestingModule({
      imports: [ChartComponent],
      providers: [
        { provide: HttpClient, useValue: { get: vi.fn(() => of({ '@type': 'pie', series: [] })) } },
        { provide: ToastService, useValue: { add: vi.fn() } },
      ],
    })
      .overrideComponent(ChartComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ChartComponent);
    fixture.componentRef.setInput('chartUrl', '');
    fixture.componentRef.setInput('filterRegistry', {});
    fixture.detectChanges();
    const component = fixture.componentInstance as any;

    const bar = {
      '@type': 'bar',
      title: 'Revenue',
      stacked: true,
      showTotals: true,
      series: [{ name: 'Net', data: [1, 2], group: 'money' }, { name: 'Empty' }],
      axes: {
        x: { '@type': 'categorical', categories: ['Jan', 'Feb'] },
        y: [{ text: 'Euro', opposite: true, seriesName: 'Net' }],
      },
      annotations: {
        xasis: [{ x: 'Feb', borderColor: 'primary', label: { text: 'Now', borderColor: 'secondary' } }, { x: 'Jan' }],
      },
    };
    const barOptions = component.createChartOptions(bar);
    expect(barOptions.chart).toEqual(expect.objectContaining({ type: 'bar', stacked: true }));
    expect(barOptions.series).toEqual([
      { name: 'Net', data: [1, 2], group: 'money' },
      { name: 'Empty', data: [], group: undefined },
    ]);
    expect(barOptions.xaxis.categories).toEqual(['Jan', 'Feb']);
    expect(barOptions.yaxis[0]).toEqual(
      expect.objectContaining({ opposite: true, seriesName: 'Net', title: { text: 'Euro' } }),
    );
    expect(barOptions.plotOptions.bar.dataLabels.total.enabled).toBe(true);
    expect(barOptions.annotations.xaxis).toHaveLength(2);

    const lineOptions = component.createChartOptions({
      '@type': 'line',
      series: [{ name: 'Visits', data: [3] }],
      axes: { x: { '@type': 'numeric', categories: [1] } },
    });
    expect(lineOptions.chart.type).toBe('line');
    expect(lineOptions.xaxis.categories).toEqual([]);
    expect(lineOptions.yaxis).toHaveLength(1);

    const pieOptions = component.createChartOptions({ '@type': 'pie', series: [30, 70] });
    expect(pieOptions.chart.type).toBe('pie');
    expect(pieOptions.series).toEqual([30, 70]);
    expect(pieOptions.yaxis).toHaveLength(1);

    expect(component.createSeries({ '@type': 'unknown' })).toBeUndefined();
    expect(component.createChartTypeSettings({ '@type': 'unknown' })).toEqual({});
    expect(component.createXAxisCategories({ '@type': 'unknown' })).toEqual([]);
    expect(component.createAnnotations(undefined)).toBeUndefined();
    expect(component.createCartesianSeries(undefined)).toEqual([]);
    component.chartRoot = () => ({ nativeElement: document.createElement('div') });
    component.renderChart({ '@type': 'pie', series: [1] });
    expect(component.chartData()).toEqual({ '@type': 'pie', series: [1] });
    fixture.destroy();
  });

  it('reloads with filters, cancels prior requests and reports rendering and HTTP failures', async () => {
    const pending = new Subject<any>();
    const http = { get: vi.fn(() => pending) };
    const toast = { add: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ChartComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: ToastService, useValue: toast },
      ],
    })
      .overrideComponent(ChartComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ChartComponent);
    fixture.componentRef.setInput('chartUrl', '/charts/revenue');
    fixture.componentRef.setInput('filterRegistry', {});
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(component.loading()).toBe(true);

    component.onFilterValueChanged({ queryParamName: 'year' } as any, 2027);
    expect(http.get).toHaveBeenLastCalledWith('/charts/revenue', { params: { year: 2027 } });
    component.onFilterValueChanged({} as any, 'ignored');
    expect(http.get).toHaveBeenCalledTimes(2);
    pending.next({ '@type': 'pie', series: [1] });
    expect(toast.add).toHaveBeenCalledWith('Chart konnte nicht initialisiert werden', 'error');
    expect(component.chartData()).toBeNull();
    pending.complete();
    expect(component.loading()).toBe(false);

    (component as any).httpClient = {
      get: vi.fn(() => throwError(() => new Error('offline'))),
    };
    (component as any).loadChart();
    expect(toast.add).toHaveBeenCalledWith(expect.stringContaining('Fehler beim Laden'), 'error');
    expect(component.loading()).toBe(false);

    const destroy = vi.fn();
    (component as any).currentChartInstance = { destroy };
    fixture.destroy();
    expect(destroy).toHaveBeenCalled();
  });
});
