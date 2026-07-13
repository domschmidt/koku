import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { ModalContentRendererComponent } from './modal-content-renderer.component';

describe('ModalContentRendererComponent', () => {
  it('creates dynamic modal content with the modal host context', async () => {
    const factory = vi.fn((context) => ({ component: class {}, context }));
    const instance = {} as any;
    await TestBed.configureTestingModule({ imports: [ModalContentRendererComponent] })
      .overrideComponent(ModalContentRendererComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ModalContentRendererComponent);
    fixture.componentRef.setInput('instance', instance);
    fixture.componentRef.setInput('modal', { title: 'Info', dynamicContentSetup: { info: factory } });
    fixture.componentRef.setInput('content', { '@type': 'info', text: 'Hello' });
    fixture.detectChanges();
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
    expect(factory.mock.calls[0][0].instance).toBe(instance);
    fixture.componentRef.setInput('content', { '@type': 'info', text: 'Changed' });
    expect(fixture.componentInstance.recipe()).toEqual(expect.objectContaining({ component: expect.any(Function) }));
  });
});
