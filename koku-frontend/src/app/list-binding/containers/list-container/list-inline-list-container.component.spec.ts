import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { ListInlineListContainerComponent } from './list-inline-list-container.component';

describe('ListInlineListContainerComponent', () => {
  it('resolves endpoint context with route parameters and emits close commands', async () => {
    const http = { request: vi.fn((method: string, url: string) => of({ method, url, name: 'Ada' })) };
    await TestBed.configureTestingModule({
      imports: [ListInlineListContainerComponent],
      providers: [{ provide: HttpClient, useValue: http }],
    })
      .overrideComponent(ListInlineListContainerComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListInlineListContainerComponent);
    fixture.componentRef.setInput('contentSetup', {});
    fixture.componentRef.setInput('urlSegments', { ':id': '42' });
    fixture.componentRef.setInput('contextMapping', {
      customer: { '@type': 'endpoint', endpointMethod: 'GET', endpointUrl: '/customers/:id' },
      stats: { '@type': 'endpoint', endpointMethod: 'POST', endpointUrl: '/customers/:id/stats' },
    });
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(http.request).toHaveBeenCalledWith('GET', '/customers/42');
    expect(http.request).toHaveBeenCalledWith('POST', '/customers/42/stats');
    expect(component.mappedContext()).toEqual(
      expect.objectContaining({ customer: expect.objectContaining({ name: 'Ada' }), stats: expect.any(Object) }),
    );
    const closed = vi.fn();
    component.closeRequested.subscribe(closed);
    component.closeInlineContent();
    expect(closed).toHaveBeenCalled();
    const create = (component as any).createContextRequest.bind(component);
    expect(() => create('x', { '@type': 'unknown' }, {})).toThrow('Unknown context type');
    expect(() => create('x', { '@type': 'endpoint', endpointUrl: '/x' }, {})).toThrow('Missing endpoint method');
    expect(() => create('x', { '@type': 'endpoint', endpointMethod: 'GET' }, {})).toThrow('Missing endpoint url');
    fixture.componentRef.setInput('contextMapping', {});
    fixture.detectChanges();
    expect(component.mappedContext()).toBeNull();
    fixture.componentRef.setInput('contextMapping', undefined);
    fixture.detectChanges();
    expect(component.mappedContext()).toBeNull();
  });
});
