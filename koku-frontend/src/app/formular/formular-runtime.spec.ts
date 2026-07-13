import { computed, linkedSignal, signal } from '@angular/core';
import { FormularRuntime } from './formular.component';
import { FORM_OUTLET } from './form-outlet';

describe('FormularRuntime', () => {
  it('creates value bindings once and reuses the handle', () => {
    const runtime = new FormularRuntime(() => undefined);
    let valueFactoryCalls = 0;

    const first = runtime.resolveContent('field', {
      createValue: () => {
        valueFactoryCalls++;
        return signal('initial');
      },
    });
    const second = runtime.resolveContent('field', {
      createValue: () => {
        valueFactoryCalls++;
        return signal('replacement');
      },
    });

    expect(second).toBe(first);
    expect(second.value?.()).toBe('initial');
    expect(valueFactoryCalls).toBe(1);
  });

  it('exposes stable content signals and tracks loading causes through the runtime facade', () => {
    const runtime = new FormularRuntime(() => undefined);
    const content = runtime.contentSignal('field');
    expect(content()).toBeUndefined();
    const handle = runtime.resolveContent('field');
    runtime.updateContentLoading('field', 'request', true);
    expect(handle.loadingCauses().has('request')).toBe(true);
    runtime.updateContentLoading('field', 'request', false);
    expect(handle.loadingCauses().has('request')).toBe(false);
  });

  it('rejects late control hydration for a stable content id', () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.resolveContent('field');

    expect(() =>
      runtime.resolveContent('field', {
        createValue: () => signal('late value'),
        writeSource: (sourceWriter, value) => sourceWriter.set('field', value),
      }),
    ).toThrowError(/control recipe cannot change/);
  });

  it('synchronizes handle values and result updates into the source', () => {
    const markDirty = vi.fn();
    const runtime = new FormularRuntime(markDirty);
    runtime.resolveContent('field', {
      createValue: () => signal('default value'),
      writeSource: (sourceWriter, value) => sourceWriter.set('nested.value', value),
    });

    runtime.initializeSource({}, []);
    expect(runtime.source()['nested']['value']).toBe('default value');

    runtime.updateContentValue('field', 'rule result');
    expect(runtime.contentHandle('field')?.value?.()).toBe('rule result');
    expect(runtime.source()['nested']['value']).toBe('rule result');
    expect(markDirty).toHaveBeenCalledTimes(1);
    expect(markDirty).toHaveBeenCalledWith();
  });

  it('indexes content overrides by alias while preserving first-match semantics', () => {
    const runtime = new FormularRuntime(() => undefined);

    runtime.setContentOverrides([
      { alias: 'customer', value: 'first', disabled: true },
      { alias: 'customer', value: 'second' },
      { alias: 'appointment', value: 'appointment-value' },
    ]);

    expect(runtime.contentOverridesByAlias().get('customer')).toEqual({
      alias: 'customer',
      value: 'first',
      disabled: true,
    });
    expect(runtime.contentOverridesByAlias().get('appointment')?.value).toBe('appointment-value');
    expect(runtime.contentOverridesByAlias().size).toBe(2);
  });

  it('treats missing content overrides as an empty index', () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setContentOverrides([{ alias: 'customer', value: '41' }]);

    runtime.setContentOverrides(undefined);

    expect(runtime.contentOverridesByAlias().size).toBe(0);
  });

  it('initializes recipe values after applying source overrides', () => {
    const runtime = new FormularRuntime(() => undefined);
    const handle = runtime.resolveContent('field', {
      createValue: () => linkedSignal(() => runtime.sourceValue('nested.value', 'default value')),
      writeSource: (sourceWriter, value) => sourceWriter.set('nested.value', value),
    });

    runtime.initializeSource({ nested: { value: 'source value' } }, [
      { path: 'nested.value', value: 'source override' },
    ]);

    expect(handle.value?.()).toBe('source override');
    expect(runtime.source()['nested']['value']).toBe('source override');
  });

  it('invalidates only selectors affected by a source update', () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.initializeSource({ first: 'a', second: 'b' }, []);
    let firstReads = 0;
    let secondReads = 0;
    const first = computed(() => {
      firstReads++;
      return runtime.sourceValue('first');
    });
    const second = computed(() => {
      secondReads++;
      return runtime.sourceValue('second');
    });
    runtime.resolveContent('first', {
      createValue: () => signal('a'),
      writeSource: (writer, value) => writer.set('first', value),
    });

    expect(first()).toBe('a');
    expect(second()).toBe('b');
    runtime.updateContentValue('first', 'updated');

    expect(first()).toBe('updated');
    expect(second()).toBe('b');
    expect(firstReads).toBe(2);
    expect(secondReads).toBe(1);
  });

  it('refreshes related parent and child source selectors', () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.initializeSource({ customer: { name: 'Before' } }, []);
    const previousSource = runtime.source();
    let customerReads = 0;
    const customer = computed(() => {
      customerReads++;
      return runtime.sourceValue('customer');
    });
    const customerName = computed(() => runtime.sourceValue('customer.name'));
    runtime.resolveContent('name', {
      createValue: () => signal('Before'),
      writeSource: (writer, value) => writer.set('customer.name', value),
    });

    expect(customer()).toEqual({ name: 'Before' });
    expect(customerName()).toBe('Before');
    runtime.updateContentValue('name', 'After');

    expect(customer()).toEqual({ name: 'After' });
    expect(customerName()).toBe('After');
    expect(customerReads).toBe(2);
    expect(previousSource).toEqual({ customer: { name: 'Before' } });
    expect(runtime.source()).not.toBe(previousSource);
    expect(runtime.source().customer).not.toBe(previousSource.customer);
  });

  it('emits lifecycle events through the normal targeted event path and completes them on reset', () => {
    const runtime = new FormularRuntime(() => undefined);
    const handle = runtime.resolveContent('field');
    const events: string[] = [];
    let completed = false;
    handle.events.subscribe({
      next: (event) => events.push(event.eventName),
      complete: () => (completed = true),
    });

    runtime.emit('field', 'INIT');
    runtime.emit('field', 'REINIT');
    runtime.reset();

    expect(events).toEqual(['INIT', 'REINIT']);
    expect(completed).toBe(true);
  });

  it('reconciles stable content ids without replacing their handles', () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setFormView({
      rootId: 'field',
      contents: { field: { id: 'field', '@type': 'input', label: 'Before' } },
      placements: [],
    } as KokuDto.FormViewDto);
    const handle = runtime.resolveContent('field', { createValue: () => signal('value') });
    let completed = false;
    handle.events.subscribe({ complete: () => (completed = true) });

    runtime.setFormView({
      rootId: 'field',
      contents: { field: { id: 'field', '@type': 'input', label: 'After' } },
      placements: [],
    } as KokuDto.FormViewDto);

    expect(runtime.contentHandle('field')).toBe(handle);
    expect((runtime.content('field') as KokuDto.InputFormularField).label).toBe('After');
    expect(completed).toBe(false);
    expect(() =>
      runtime.setFormView({
        rootId: 'field',
        contents: { field: { id: 'field', '@type': 'date-input' } },
        placements: [],
      } as KokuDto.FormViewDto),
    ).toThrowError(/Content type cannot change/);
  });

  it('is initialized only after every registered content has rendered', async () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setFormView({
      rootId: 'root',
      contents: {
        root: { id: 'root', '@type': 'grid' },
        field: { id: 'field', '@type': 'input' },
      },
      placements: [{ parentId: 'root', outlet: FORM_OUTLET.CONTENT, childId: 'field' }],
    } as KokuDto.FormViewDto);
    let initialized = false;
    const initialization = runtime.whenInitialized().then(() => (initialized = true));
    runtime.resolveContent('root');
    runtime.attachInstance('root', {});
    await Promise.resolve();
    expect(initialized).toBe(false);
    runtime.resolveContent('field');
    runtime.attachInstance('field', {});
    await initialization;
    expect(initialized).toBe(true);
  });

  it('surfaces content initialization failures', async () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setFormView({
      rootId: 'root',
      contents: { root: { id: 'root', '@type': 'grid' } },
      placements: [],
    } as KokuDto.FormViewDto);
    const error = new Error('recipe failed');
    const initialization = runtime.whenInitialized();

    runtime.failInitialization(error);

    await expect(initialization).rejects.toEqual(error);
  });

  it('rejects incomplete and ambiguous placement trees', () => {
    const runtime = new FormularRuntime(() => undefined);
    expect(() =>
      runtime.setFormView({
        rootId: 'root',
        contents: {
          root: { id: 'root', '@type': 'grid' },
          field: { id: 'field', '@type': 'input' },
        },
        placements: [],
      } as KokuDto.FormViewDto),
    ).toThrowError(/Content has no placement: field/);

    expect(() =>
      runtime.setFormView({
        rootId: 'root',
        contents: {
          root: { id: 'root', '@type': 'grid' },
          field: { id: 'different-id', '@type': 'input' },
        },
        placements: [{ parentId: 'root', outlet: FORM_OUTLET.CONTENT, childId: 'field' }],
      } as KokuDto.FormViewDto),
    ).toThrowError(/does not match its registry key/);
  });

  it('does not let removed renderers register stale handles', () => {
    const runtime = new FormularRuntime(() => undefined);
    runtime.setFormView({
      rootId: 'root',
      contents: {
        root: { id: 'root', '@type': 'grid' },
        field: { id: 'field', '@type': 'input' },
      },
      placements: [{ parentId: 'root', outlet: FORM_OUTLET.CONTENT, childId: 'field' }],
    } as KokuDto.FormViewDto);
    runtime.resolveContent('root');
    runtime.resolveContent('field');

    runtime.setFormView({
      rootId: 'root',
      contents: { root: { id: 'root', '@type': 'grid' } },
      placements: [],
    } as KokuDto.FormViewDto);

    expect(() => runtime.resolveContent('field')).toThrowError(/outside the active form definition/);
  });

  it('rejects every malformed form-tree invariant and protects stable content identity', () => {
    const runtime = new FormularRuntime(() => undefined);
    const view = (contents: Record<string, any>, placements: any[], rootId: string | undefined = 'root') =>
      ({ rootId, contents, placements }) as KokuDto.FormViewDto;
    const root = { id: 'root', '@type': 'grid' };
    const child = { id: 'child', '@type': 'input' };

    expect(() => runtime.setFormView(view({}, [], undefined))).toThrow(/root content is missing/);
    expect(() => runtime.setFormView(view({ root: { '@type': 'grid' } }, []))).toThrow(/requires an id/);
    expect(() => runtime.setFormView(view({ root, child }, [{}]))).toThrow(/requires parentId, outlet and childId/);
    expect(() =>
      runtime.setFormView(view({ root, child }, [{ parentId: 'missing', outlet: 'content', childId: 'child' }])),
    ).toThrow(/parent content not found/);
    expect(() =>
      runtime.setFormView(view({ root, child }, [{ parentId: 'root', outlet: 'content', childId: 'missing' }])),
    ).toThrow(/child content not found/);
    expect(() =>
      runtime.setFormView(view({ root, child }, [{ parentId: 'child', outlet: 'content', childId: 'root' }])),
    ).toThrow(/root content cannot be placed/);
    expect(() =>
      runtime.setFormView(
        view({ root, child }, [
          { parentId: 'root', outlet: 'a', childId: 'child' },
          { parentId: 'root', outlet: 'b', childId: 'child' },
        ]),
      ),
    ).toThrow(/placed more than once/);

    runtime.setFormView(view({ root }, []));
    expect(runtime.childIds(undefined, 'content')()).toEqual([]);
    expect(() => runtime.updateContentConfig('missing', (content) => content)).toThrow(/Content not found/);
    expect(() => runtime.updateContentConfig('root', (content) => ({ ...content, id: 'changed' }))).toThrow(
      /id cannot change/,
    );
    expect(() => runtime.updateContentConfig('root', (content) => ({ ...content, '@type': 'fieldset' }))).toThrow(
      /type cannot change/,
    );
    runtime.reset();
    expect(runtime.content('root')).toBeUndefined();
  });
});
