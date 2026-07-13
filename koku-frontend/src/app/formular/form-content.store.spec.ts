import { signal } from '@angular/core';
import { vi } from 'vitest';
import { FormContentStore } from './form-content.store';

describe('FormContentStore', () => {
  it('manages handles, values, events and source writers', async () => {
    const store = new FormContentStore();
    store.reconcile(new Set(['name']));
    const writeSource = vi.fn();
    const handle = store.register('name', () => signal('Ada'), writeSource);
    const events = vi.fn();
    handle.events.subscribe(events);
    store.setValue('name', 'Grace');
    store.writeValue('name', { set: vi.fn() }, 'Lin');
    store.writeAll({ set: vi.fn() });
    store.updateLoading('name', 'request', true);
    store.updateLoading('name', 'request', false);
    store.emit('name', 'CHANGE', 42);
    expect(events).toHaveBeenCalledWith({ eventName: 'CHANGE', payload: 42 });
    expect(writeSource).toHaveBeenCalledTimes(2);
    expect(Array.from(store.ids())).toEqual(['name']);
    expect(store.contentHandle('name')).toBe(handle);

    const instance = { validate: vi.fn(() => false) };
    store.attachInstance('name', instance);
    await expect(store.whenInitialized()).resolves.toBeUndefined();
    expect(store.firstInvalidInstance()).toBe(instance);
    expect(store.register('name', () => signal('ignored'), writeSource)).toBe(handle);
    expect(() => store.register('name')).toThrow('recipe cannot change');
  });

  it('rejects invalid registrations and missing value state', async () => {
    const store = new FormContentStore();
    store.reconcile(new Set(['layout']));
    const layout = store.register('layout');
    expect(() => store.register('other')).toThrow('outside the active form definition');
    expect(() => store.setValue('layout', 'x')).toThrow('has no value');
    expect(() => store.setValue('missing', 'x')).toThrow('not initialized yet');
    store.attachInstance('layout', {});
    await store.whenInitialized();
    expect(store.firstInvalidInstance()).toBeUndefined();
    layout.events.subscribe({ complete: vi.fn() });
    store.reconcile(new Set());
    store.reset();
    expect(() => store.register('late')).toThrow('after runtime reset');
  });

  it('rejects a failed initialization', async () => {
    const store = new FormContentStore();
    store.reconcile(new Set(['field']));
    const initialized = store.whenInitialized();
    store.failInitialization(new Error('broken'));
    await expect(initialized).rejects.toThrow('broken');
  });
});
