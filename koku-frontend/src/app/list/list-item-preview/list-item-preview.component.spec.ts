import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ListItemPreviewComponent } from './list-item-preview.component';

describe('ListItemPreviewComponent', () => {
  it('creates preview recipes from current item state and supports missing previews', async () => {
    const factory = vi.fn((context) => ({ component: class {}, context }));
    const register = {
      preview: { '@type': 'text', valuePath: 'customer.name' },
      source: signal({ customer: { name: 'Ada' } }),
    } as any;
    await TestBed.configureTestingModule({ imports: [ListItemPreviewComponent] })
      .overrideComponent(ListItemPreviewComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListItemPreviewComponent);
    fixture.componentRef.setInput('register', register);
    fixture.componentRef.setInput('contentSetup', { previewRegistry: { text: factory } });
    fixture.detectChanges();
    expect(fixture.componentInstance.previewRecipe()).toEqual(
      expect.objectContaining({ component: expect.any(Function) }),
    );
    expect(factory.mock.calls[0][0].value()).toBe('Ada');
    expect(factory.mock.calls[0][0].preview()).toBe(register.preview);
    fixture.componentRef.setInput('register', { ...register, preview: undefined });
    fixture.detectChanges();
    expect(fixture.componentInstance.previewRecipe()).toBeNull();
    const identity = { preview: register.preview, factory };
    expect((fixture.componentInstance as any).samePreviewIdentity(identity, identity)).toBe(true);
  });
});
