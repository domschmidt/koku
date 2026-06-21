import { GlobalEvents } from './global-events';

describe('GlobalEvents', () => {
  it('supports multiple listeners for the same owner and event', () => {
    const events = new GlobalEvents();
    const values: number[] = [];

    events.addGlobalEventListener('owner', 'changed', (value) => values.push(value));
    events.addGlobalEventListener('owner', 'changed', (value) => values.push(value * 2));
    events.propagateGlobalEvent('changed', 3);

    expect(values).toEqual([3, 6]);
  });

  it('removes individual subscriptions without affecting other listeners', () => {
    const events = new GlobalEvents();
    const values: string[] = [];
    const unsubscribe = events.addGlobalEventListener('owner', 'changed', () => values.push('first'));
    events.addGlobalEventListener('owner', 'changed', () => values.push('second'));

    unsubscribe();
    events.propagateGlobalEvent('changed', undefined);

    expect(values).toEqual(['second']);
  });

  it('invokes all listeners before reporting aggregated failures', () => {
    const events = new GlobalEvents();
    let successfulListenerCalled = false;
    events.addGlobalEventListener('failing', 'changed', () => {
      throw new Error('failed');
    });
    events.addGlobalEventListener('successful', 'changed', () => {
      successfulListenerCalled = true;
    });

    expect(() => events.propagateGlobalEvent('changed', undefined)).toThrowError(AggregateError);
    expect(successfulListenerCalled).toBe(true);
  });
});
