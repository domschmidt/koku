import { FormDefinitionStore } from './form-definition.store';

const content = (id: string, type = 'grid') => ({ id, '@type': type }) as any;

describe('FormDefinitionStore', () => {
  it('maintains content and outlet signals across compatible definitions', () => {
    const store = new FormDefinitionStore();
    const childIds = store.childIds('root', 'content');
    store.setFormView({
      rootId: 'root',
      contents: { root: content('root'), child: content('child', 'input') },
      placements: [{ parentId: 'root', outlet: 'content', childId: 'child' }],
    } as any);
    expect(childIds()).toEqual(['child']);
    expect(store.content(undefined)).toBeUndefined();
    expect(store.childIds(undefined, 'content')()).toEqual([]);
    expect(store.contentIds()).toEqual(new Set(['root', 'child']));
    store.updateContent('child', (current) => ({ ...current, label: 'Name' }) as any);
    expect((store.content('child') as any).label).toBe('Name');
    expect(() => store.updateContent('missing', (current) => current)).toThrow('Content not found');
    expect(() => store.updateContent('child', (current) => ({ ...current, id: 'other' }) as any)).toThrow(
      'Content id cannot change',
    );
    expect(() => store.updateContent('child', (current) => ({ ...current, '@type': 'select' }) as any)).toThrow(
      'Content type cannot change',
    );

    store.setFormView({ rootId: 'root', contents: { root: content('root') }, placements: [] } as any);
    expect(childIds()).toEqual([]);
    expect(store.content('child')).toBeUndefined();
    store.reset();
  });

  it('rejects malformed and unstable definition graphs', () => {
    const store = new FormDefinitionStore();
    expect(() => store.setFormView({ contents: {} } as any)).toThrow('root content is missing');
    expect(() =>
      store.setFormView({ rootId: 'root', contents: { root: { '@type': 'grid' } }, placements: [] } as any),
    ).toThrow('requires an id');
    expect(() =>
      store.setFormView({ rootId: 'root', contents: { root: content('other') }, placements: [] } as any),
    ).toThrow('does not match');
    const base = { rootId: 'root', contents: { root: content('root'), child: content('child') } };
    expect(() => store.setFormView({ ...base, placements: [{}] } as any)).toThrow('requires parentId');
    expect(() =>
      store.setFormView({ ...base, placements: [{ parentId: 'missing', outlet: 'x', childId: 'child' }] } as any),
    ).toThrow('parent content not found');
    expect(() =>
      store.setFormView({ ...base, placements: [{ parentId: 'root', outlet: 'x', childId: 'missing' }] } as any),
    ).toThrow('child content not found');
    expect(() =>
      store.setFormView({ ...base, placements: [{ parentId: 'child', outlet: 'x', childId: 'root' }] } as any),
    ).toThrow('root content cannot be placed');
    expect(() => store.setFormView({ ...base, placements: [] } as any)).toThrow('has no placement');

    store.setFormView({ rootId: 'root', contents: { root: content('root') }, placements: [] } as any);
    expect(() =>
      store.setFormView({ rootId: 'root', contents: { root: content('root', 'input') }, placements: [] } as any),
    ).toThrow('type cannot change');
  });

  it('detects duplicate, cyclic and unreachable placements', () => {
    const contents = { root: content('root'), a: content('a'), b: content('b') };
    const duplicate = [
      { parentId: 'root', outlet: 'x', childId: 'a' },
      { parentId: 'b', outlet: 'x', childId: 'a' },
      { parentId: 'root', outlet: 'x', childId: 'b' },
    ];
    expect(() =>
      new FormDefinitionStore().setFormView({ rootId: 'root', contents, placements: duplicate } as any),
    ).toThrow('more than once');
    const unreachableCycle = [
      { parentId: 'b', outlet: 'x', childId: 'a' },
      { parentId: 'a', outlet: 'x', childId: 'b' },
    ];
    expect(() =>
      new FormDefinitionStore().setFormView({ rootId: 'root', contents, placements: unreachableCycle } as any),
    ).toThrow('unreachable');
    const store = new FormDefinitionStore() as any;
    expect(() =>
      store.validateReachability(
        'root',
        contents,
        new Map([
          ['root', ['a']],
          ['a', ['root']],
        ]),
      ),
    ).toThrow('cycle detected');
    expect(() => store.validateReachability('root', contents, new Map([['root', ['a', 'a', 'b']]]))).not.toThrow();
  });
});
