import { afterEach, describe, expect, it, vi } from 'vitest';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  afterEach(() => vi.useRealTimers());

  it('closes timed and custom-button toasts and ignores unknown values', () => {
    vi.useFakeTimers();
    const service = new ToastService();
    service.add('Saved');
    expect(service.values()[0]).toEqual(expect.objectContaining({ message: 'Saved', type: 'success' }));
    vi.advanceTimersByTime(5000);
    expect(service.values()).toEqual([]);

    const buttons = [{ uid: 7, text: 'Retry', title: 'Retry', onClick: vi.fn() }] as any;
    service.add('Failed', 'error', buttons, 0);
    expect(service.values()[0].buttons).toBe(buttons);
    service.values()[0].close();
    expect(service.values()).toEqual([]);
    service.close({} as any);
    expect(service.values()).toEqual([]);

    service.add('Dismiss me', 'info', undefined, 0);
    service.values()[0].buttons[0].onClick(new Event('click'), service.values()[0], service.values()[0].buttons[0]);
    expect(service.values()).toEqual([]);
  });
});
