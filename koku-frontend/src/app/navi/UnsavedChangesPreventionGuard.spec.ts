import { firstValueFrom, of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { UnsavedChangesPreventionGuard } from './UnsavedChangesPreventionGuard';

describe('UnsavedChangesPreventionGuard', () => {
  it('registers, delegates and removes unsaved-change decisions', async () => {
    const guard = new UnsavedChangesPreventionGuard();
    expect(await firstValueFrom(guard.canDeactivate() as any)).toBe(true);
    const instance = {};
    const decision = vi.fn(() => of(false));
    guard.registerUnsavedChangesPrevention(instance, decision);
    expect(await firstValueFrom(guard.canDeactivate() as any)).toBe(false);
    expect(decision).toHaveBeenCalledOnce();
    guard.unregisterUnsavedChangesPrevention(instance);
    expect(await firstValueFrom(guard.canDeactivate() as any)).toBe(true);
  });
});
