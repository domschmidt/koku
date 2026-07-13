import { describe, expect, it, vi } from 'vitest';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { executeInlineFormularSaveEvents } from './inline-formular-save-events';

describe('executeInlineFormularSaveEvents', () => {
  it('propagates payloads and opens routes with resolved payload parameters', () => {
    const listener = vi.fn();
    const dispose = GLOBAL_EVENT_BUS.addGlobalEventListener('save-events-test', 'customer-saved', listener);
    const context = { openRoutedContent: vi.fn() };
    const payload = { customer: { id: 42 }, ignored: null };

    executeInlineFormularSaveEvents(
      [
        { '@type': 'propagate-global-event', eventName: 'customer-saved' },
        {
          '@type': 'open-routed-inline-formular',
          route: 'customers/:customerId/details/:missing',
          params: [
            { '@type': 'event-payload', param: ':customerId', valuePath: 'customer.id' },
            { '@type': 'event-payload', param: ':missing', valuePath: 'ignored' },
            { '@type': 'constant', param: ':unused', valuePath: 'customer.id' },
          ],
        },
      ],
      payload,
      context,
    );

    expect(listener).toHaveBeenCalledWith(payload);
    expect(context.openRoutedContent).toHaveBeenCalledWith(['customers', '42', 'details', ':missing']);
    dispose();
  });

  it('accepts an empty job list and rejects malformed jobs', () => {
    const context = { openRoutedContent: vi.fn() };
    expect(() => executeInlineFormularSaveEvents(undefined, {}, context)).not.toThrow();
    expect(() => executeInlineFormularSaveEvents([{ '@type': 'propagate-global-event' }], {}, context)).toThrow(
      'Missing eventName',
    );
    expect(() => executeInlineFormularSaveEvents([{ '@type': 'unknown' }], {}, context)).toThrow(
      'Unknown saved event type unknown',
    );
  });
});
