import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListFieldRendererComponent } from './list-field-renderer.component';

describe('ListFieldRendererComponent', () => {
  it('creates field recipes and synchronizes init and value changes', async () => {
    const value = signal('Before');
    const fieldState: any = { config: { '@type': 'text' }, value };
    const register: any = { fields: { name: fieldState } };
    let recipeContext: any;
    const factory = vi.fn((context: any) => {
      recipeContext = context;
      return { component: class {} };
    });
    await TestBed.configureTestingModule({ imports: [ListFieldRendererComponent] })
      .overrideComponent(ListFieldRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListFieldRendererComponent);
    fixture.componentRef.setInput('register', register);
    fixture.componentRef.setInput('fieldId', 'name');
    fixture.componentRef.setInput('contentSetup', { fieldRegistry: { text: factory } });
    fixture.detectChanges();
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    expect(recipeContext.id).toBe('name');
    expect((fixture.componentInstance as any).config()).toBe(fieldState.config);
    fixture.componentRef.setInput('register', { fields: { name: fieldState } });
    expect(fixture.componentInstance.recipe()).toBeTruthy();
    recipeContext.emit('onChange', 'After');
    expect(value()).toBe('After');
    const componentRef = { instance: {} } as any;
    fixture.componentInstance.captureInstance(componentRef);
    expect(fieldState.instance).toBe(componentRef);
  });

  it('rejects missing fields and recipes', async () => {
    await TestBed.configureTestingModule({ imports: [ListFieldRendererComponent] })
      .overrideComponent(ListFieldRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListFieldRendererComponent);
    fixture.componentRef.setInput('register', { fields: {} });
    fixture.componentRef.setInput('fieldId', 'missing');
    fixture.componentRef.setInput('contentSetup', { fieldRegistry: {} });
    expect(() => fixture.componentInstance.recipe()).toThrow('List field state not found');
    fixture.componentRef.setInput('register', { fields: { missing: { config: { '@type': 'unknown' } } } });
    expect(() => fixture.componentInstance.recipe()).toThrow('No list recipe registered');
  });
});
