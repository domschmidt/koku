import { ApplicationRef } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { ThemingService } from './theming.service';

describe('ThemingService', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('tracks system theme changes and triggers application rendering', () => {
    let listener: ((event: { matches: boolean }) => void) | undefined;
    vi.stubGlobal(
      'matchMedia',
      vi.fn(() => ({ matches: true, addEventListener: (_: string, callback: any) => (listener = callback) })),
    );
    const appRef = { tick: vi.fn() };
    TestBed.configureTestingModule({ providers: [{ provide: ApplicationRef, useValue: appRef }] });
    const service = TestBed.inject(ThemingService);
    expect(service.theme.value).toBe('koku-dark');
    listener?.({ matches: false });
    expect(service.theme.value).toBe('koku-light');
    expect(appRef.tick).toHaveBeenCalledOnce();
  });
});
