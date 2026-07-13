import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { FormularRuntime } from '../formular.component';
import { FormularContentRendererComponent } from './formular-content-renderer.component';

@Component({ template: '' })
class RenderedFieldStub {
  validate = vi.fn(() => true);
}

describe('FormularContentRendererComponent', () => {
  it('creates recipes with runtime handles, overrides and dock state and captures instances', async () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setFormView({
      rootId: 'field',
      contents: {
        field: { id: 'field', '@type': 'stub', alias: 'customer-name', dockable: true },
      },
      placements: [],
    } as any);
    runtime.setContentOverrides([{ alias: 'customer-name', value: 'Ada', disabled: true }]);
    const render = vi.fn(() => ({ component: RenderedFieldStub, inputs: () => ({ value: 'Ada' }) }));
    const factory = vi.fn((context: any) => ({
      control: { createValue: () => signal('initial') },
      render,
      context,
    }));
    await TestBed.configureTestingModule({ imports: [FormularContentRendererComponent] })
      .overrideComponent(FormularContentRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(FormularContentRendererComponent);
    fixture.componentRef.setInput('content', runtime.content('field'));
    fixture.componentRef.setInput('runtime', runtime);
    fixture.componentRef.setInput('contentRegistry', { stub: factory });
    fixture.componentRef.setInput('buttonDockOutlet', {});
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(factory).toHaveBeenCalled();
    const context = factory.mock.calls[0][0];
    expect(context.id).toBe('field');
    expect(context.override()).toEqual({ alias: 'customer-name', value: 'Ada', disabled: true });
    expect(component.enableDockedOutput()).toBe(true);
    expect(component.dynamicRecipe()).toEqual(expect.objectContaining({ component: RenderedFieldStub }));
    expect(runtime.contentHandle('field')?.value?.()).toBe('initial');
    fixture.componentRef.setInput('contentRegistry', { stub: factory });
    fixture.detectChanges();

    const input = document.createElement('input');
    const focus = vi.spyOn(input, 'focus');
    const host = document.createElement('div');
    host.append(input);
    const instance = new RenderedFieldStub();
    component.captureInstance({ instance, location: { nativeElement: host } } as any);
    const attached = runtime.contentHandle('field');
    expect(runtime.firstInvalidInstance()).toBeUndefined();
    instance.validate.mockReturnValue(false);
    expect(runtime.firstInvalidInstance()).toBeDefined();
    (attached as any).instance?.focus?.();
    const invalid = runtime.firstInvalidInstance() as any;
    invalid.focus();
    expect(focus).toHaveBeenCalled();

    fixture.componentRef.setInput('buttonDockOutlet', undefined);
    fixture.detectChanges();
    expect(component.enableDockedOutput()).toBe(false);
  });

  it('rejects unstable ids and missing recipes and ignores anonymous instance capture', async () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setFormView({
      rootId: 'field',
      contents: { field: { id: 'field', '@type': 'stub' } },
      placements: [],
    } as any);
    await TestBed.configureTestingModule({ imports: [FormularContentRendererComponent] })
      .overrideComponent(FormularContentRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(FormularContentRendererComponent);
    fixture.componentRef.setInput('content', runtime.content('field'));
    fixture.componentRef.setInput('runtime', runtime);
    fixture.componentRef.setInput('contentRegistry', {
      stub: () => ({ render: () => ({ component: RenderedFieldStub }) }),
    });
    fixture.detectChanges();
    const component = fixture.componentInstance as any;
    expect(() => component.createDynamicRecipe(undefined, vi.fn())).toThrow('stable id');
    expect(() => component.createDynamicRecipe('field', undefined)).toThrow('No recipe registered');
    const originalContent = component.activeContent;
    component.activeContent = () => ({ '@type': 'stub' });
    expect(() => component.captureInstance({ instance: {}, location: { nativeElement: document.body } })).not.toThrow();
    component.activeContent = originalContent;
    const failInitialization = vi.spyOn(runtime, 'failInitialization');
    void runtime.whenInitialized().catch(() => undefined);
    expect(() => component.refreshDynamicRecipe({ id: 'field', factory: undefined, runtime })).toThrow(
      'No recipe registered',
    );
    expect(failInitialization).toHaveBeenCalled();
  });
});
