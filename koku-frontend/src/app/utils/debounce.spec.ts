import { afterEach, describe, expect, it, vi } from 'vitest';
import { debounce } from './debounce';

describe('debounce', () => {
  afterEach(() => vi.useRealTimers());

  it('preserves only the latest call arguments', () => {
    vi.useFakeTimers();
    const listener = vi.fn();
    const debounced = debounce(listener, 25);
    debounced('first');
    debounced('second');
    vi.advanceTimersByTime(24);
    expect(listener).not.toHaveBeenCalled();
    vi.advanceTimersByTime(1);
    expect(listener).toHaveBeenCalledOnce();
    expect(listener).toHaveBeenCalledWith('second');
  });
});
