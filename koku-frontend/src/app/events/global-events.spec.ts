import { describe, expect, it, vi } from 'vitest';
import { GlobalEvents } from './global-events';

describe('GlobalEvents', () => {
  it('dispatches by event name and supports both disposal forms', () => {
    const events = new GlobalEvents();
    const first = vi.fn();
    const second = vi.fn();
    const disposeFirst = events.addGlobalEventListener('one', 'saved', first);
    events.addGlobalEventListener('two', 'saved', second);
    events.addGlobalEventListener('two', 'other', vi.fn());

    events.propagateGlobalEvent('saved', { id: 7 });
    expect(first).toHaveBeenCalledWith({ id: 7 });
    expect(second).toHaveBeenCalledWith({ id: 7 });

    disposeFirst();
    events.removeGlobalEventListener('two');
    events.propagateGlobalEvent('saved', {});
    expect(first).toHaveBeenCalledOnce();
    expect(second).toHaveBeenCalledOnce();
  });

  it('runs every listener and aggregates their failures', () => {
    const events = new GlobalEvents();
    const successful = vi.fn();
    events.addGlobalEventListener('one', 'saved', () => {
      throw new Error('first');
    });
    events.addGlobalEventListener('two', 'saved', successful);
    events.addGlobalEventListener('three', 'saved', () => {
      throw new Error('second');
    });

    expect(() => events.propagateGlobalEvent('saved', 1)).toThrowError(AggregateError);
    expect(successful).toHaveBeenCalledWith(1);
  });
});
