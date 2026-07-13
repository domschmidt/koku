import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListActionRendererComponent } from './list-action-renderer.component';

describe('ListActionRendererComponent', () => {
  it('creates action recipes with the complete parent context', async () => {
    const factory = vi.fn((context) => ({ component: class {}, context }));
    const register = { source: signal({}) };
    const parent = {
      contentSetup: signal({ actionRegistry: { edit: factory } }),
      register: signal(register),
      listRegister: signal([register]),
      urlSegments: signal({ ':id': '42' }),
    } as any;
    await TestBed.configureTestingModule({ imports: [ListActionRendererComponent] })
      .overrideComponent(ListActionRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListActionRendererComponent);
    fixture.componentRef.setInput('action', { '@type': 'edit' });
    fixture.componentRef.setInput('parent', parent);
    fixture.detectChanges();
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    expect(factory.mock.calls[0][0].parent).toBe(parent);
    fixture.componentRef.setInput('action', { '@type': 'edit', title: 'Changed' });
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
  });
});
