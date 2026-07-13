import { signal } from '@angular/core';
import { FormSourceStore } from './form-source.store';

describe('FormSourceStore', () => {
  it('reads, updates and replaces immutable nested source values', () => {
    const source = signal<any>({ customer: { name: 'Ada' }, rows: [{ value: 1 }] });
    const store = new FormSourceStore(source);

    expect(store.value(undefined, 'fallback')).toBe('fallback');
    expect(store.value('missing', 'fallback')).toBe('fallback');
    expect(store.value('customer.name')).toBe('Ada');
    const original = source();
    store.update((writer) => {
      writer.set('customer.name', 'Grace');
      writer.set('rows[0].value', 2);
      writer.set('', 'ignored');
    });
    expect(source()).toEqual({ customer: { name: 'Grace' }, rows: [{ value: 2 }] });
    expect(source()).not.toBe(original);
    store.update((writer) => writer.set('customer.name', 'Grace'));
    expect(source()).toEqual({ customer: { name: 'Grace' }, rows: [{ value: 2 }] });

    store.replaceAndUpdate(undefined, (writer) => writer.set('0.name', 'First'));
    expect(source()).toEqual([{ name: 'First' }]);
    store.replace({ done: true });
    store.resetSelectors();
    expect(store.value('done')).toBe(true);
  });

  it('rejects paths without usable segments', () => {
    const store = new FormSourceStore(signal<any>({}));
    expect(() => (store as any).pathParts('[]')).toThrow('Invalid source path');
  });
});
