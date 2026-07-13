import { signal, TemplateRef, ViewContainerRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';
import { PortalDirective } from './portal.directive';

describe('PortalDirective', () => {
  it('clears, creates and destroys its embedded view', () => {
    const view = { destroy: vi.fn() };
    const outlet = {
      viewContainerRef: {
        clear: vi.fn(),
        createEmbeddedView: vi.fn(() => view),
      } as unknown as ViewContainerRef,
    };
    const template = {} as TemplateRef<unknown>;
    TestBed.configureTestingModule({ providers: [{ provide: TemplateRef, useValue: template }] });
    const directive = TestBed.runInInjectionContext(() => new PortalDirective());
    (directive.portalOutlet as any) = signal(outlet);
    (directive.append as any) = signal(false);
    directive.ngAfterViewInit();
    expect(outlet.viewContainerRef.clear).toHaveBeenCalledOnce();
    expect(outlet.viewContainerRef.createEmbeddedView).toHaveBeenCalledWith(template);
    directive.ngOnDestroy();
    expect(view.destroy).toHaveBeenCalledOnce();

    const detached = TestBed.runInInjectionContext(() => new PortalDirective());
    (detached.portalOutlet as any) = signal(undefined);
    detached.ngAfterViewInit();
  });
});
