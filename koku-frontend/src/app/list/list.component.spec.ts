import { HttpClient } from '@angular/common/http';
import { SimpleChange } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { NavigationEnd, Router } from '@angular/router';
import { firstValueFrom, of, Subject, throwError } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { LIST_CONTENT_SETUP } from '../list-binding/registry';
import { ModalService } from '../modal/modal.service';
import { ListComponent } from './list.component';

describe('ListComponent', () => {
  it('loads, queries and synchronizes list state through actions and global events', async () => {
    vi.useFakeTimers();
    const routerEvents = new Subject<unknown>();
    const router = {
      url: '/customers',
      events: routerEvents,
      navigate: vi.fn(() => Promise.resolve(true)),
      navigateByUrl: vi.fn(() => Promise.resolve(true)),
    };
    const modal = { uid: 1, close: vi.fn(), update: vi.fn() };
    const modalService = { add: vi.fn(() => modal) };
    const listView = {
      fieldFetchPaths: ['name', 'status'],
      itemIdPath: 'id',
      fields: [
        {
          id: 'name',
          valuePath: 'name',
          fieldDefinition: { '@type': 'input', defaultValue: 'Unknown' },
        },
      ],
      filters: [
        {
          id: 'active',
          valuePath: 'active',
          filterDefinition: {
            '@type': 'toggle',
            defaultState: 'ENABLED',
            enabledPredicates: [{ searchOperator: 'EQ', searchExpression: 'TRUE' }],
          },
        },
      ],
      globalEventListeners: [
        { '@type': 'event-payload-search-term', eventName: 'search', valuePath: 'term' },
        {
          '@type': 'item-add',
          eventName: 'created',
          idPath: 'id',
          valueMapping: {
            name: { '@type': 'field-reference', fieldId: 'name' },
            status: { '@type': 'source-path-reference', valuePath: 'status' },
          },
        },
        {
          '@type': 'item-update-via-event-payload',
          eventName: 'updated',
          idPath: 'id',
          valueMapping: {
            name: { '@type': 'field-reference', fieldId: 'name' },
            status: { '@type': 'source-path-reference', valuePath: 'status' },
          },
        },
        {
          '@type': 'open-routed-content',
          eventName: 'open',
          route: 'customers/:id',
          params: [{ '@type': 'event-payload', param: ':id', valuePath: 'id' }],
        },
      ],
      routedContents: [
        {
          '@type': 'routed-inline-content',
          route: 'customers/:id',
          itemId: ':id',
          inlineContent: { '@type': 'formular' },
        },
      ],
      routedItems: [{ '@type': 'routed-item', route: 'new', text: 'New customer' }],
      itemActions: [{ '@type': 'propagate-global-event', eventName: 'action' }],
      globalItemStyling: [],
    } as any;
    const page = {
      results: [{ id: '1', values: { name: 'Ada', status: 'active' } }],
      totalElements: 1,
    };
    const http = {
      get: vi.fn(() => of(listView)),
      post: vi.fn(() => of(page)),
    };
    await TestBed.configureTestingModule({
      imports: [ListComponent],
      providers: [
        { provide: HttpClient, useValue: http },
        { provide: Router, useValue: router },
        { provide: ModalService, useValue: modalService },
      ],
    })
      .overrideComponent(ListComponent, { set: { template: '' } })
      .compileComponents();
    const fixture = TestBed.createComponent(ListComponent);
    fixture.componentRef.setInput('contentSetup', LIST_CONTENT_SETUP);
    fixture.componentRef.setInput('listUrl', '/customers/list');
    fixture.componentRef.setInput('sourceUrl', '/customers/query');
    fixture.componentRef.setInput('parentRoutePath', '/customers');
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(http.get).toHaveBeenCalledWith('/customers/list');
    expect(http.post).toHaveBeenCalledWith(
      '/customers/query',
      expect.objectContaining({
        fieldSelection: ['name', 'status'],
        fieldPredicates: {
          active: { predicates: [{ searchOperator: 'EQ', searchExpression: 'TRUE' }] },
        },
      }),
    );
    expect(component.listRegister()[0].fields['name'].value()).toBe('Ada');
    expect(component.sourceLoading()).toBe(false);

    GLOBAL_EVENT_BUS.propagateGlobalEvent('created', { id: '2', name: 'Grace', status: 'new' });
    expect(component.listRegister()[0].id).toBe('2');
    expect(component.listRegister()[0].fields['name'].value()).toBe('Grace');
    GLOBAL_EVENT_BUS.propagateGlobalEvent('updated', { id: '2', name: 'Grace Hopper', status: 'active' });
    expect(component.listRegister()[0].fields['name'].value()).toBe('Grace Hopper');
    expect(component.listRegister()[0].source()['status']).toBe('active');

    GLOBAL_EVENT_BUS.propagateGlobalEvent('search', { term: 'Ada' });
    vi.advanceTimersByTime(300);
    expect(component.globalSearchTerm()).toBe('Ada');
    GLOBAL_EVENT_BUS.propagateGlobalEvent('open', { id: '2' });
    await Promise.resolve();
    expect(router.navigate).toHaveBeenCalled();

    component.sourceLoading.set(false);
    const item = component.listRegister()[0];
    const clicked = vi.fn();
    const dispose = GLOBAL_EVENT_BUS.addGlobalEventListener('list-component-spec', 'selected', clicked);
    component.itemClickAction(new Event('click'), item, {
      '@type': 'propagate-global-event',
      eventName: 'selected',
    } as any);
    expect(clicked).toHaveBeenCalledWith(item.source());
    component.itemClickAction(new Event('click'), item, {
      '@type': 'open-routed-content',
      route: 'customers/:name',
      params: [
        {
          '@type': 'value',
          param: ':name',
          valueReference: { '@type': 'field-reference', fieldId: 'name' },
        },
      ],
    } as any);
    component.itemClickAction(new Event('click'), item, {
      '@type': 'open-inline-content',
      inlineContent: { '@type': 'formular' },
    } as any);
    expect(component.inlineContent()?.id).toBe(item.id);
    component.itemAction(new Event('click'), item, {
      '@type': 'open-inline-content',
      inlineContent: { '@type': 'list' },
    } as any);
    component.listAction(new Event('click'), {
      '@type': 'open-inline-content',
      inlineContent: { '@type': 'chart' },
    } as any);
    expect(component.inlineContent()?.content?.['@type']).toBe('chart');
    component.listAction(new Event('click'), {
      '@type': 'open-routed-content',
      route: 'customers/new',
    } as any);

    component.onFilterChange('deleted', 'deleted', [{ searchOperator: 'EQ', searchExpression: 'FALSE' }]);
    component.setGlobalSearchTerm('Grace');
    expect(component.currentPage()).toBe(0);
    expect(component.globalSearchTerm()).toBe('Grace');
    expect(component.getItemClasses({}, [])).toEqual([]);

    component.openInlineContent({
      content: { '@type': 'formular' } as any,
      id: '2',
      urlSegments: null,
    });
    expect(await firstValueFrom(component.closeInlineContent())).toBe(true);
    expect(component.inlineContent()).toBeNull();
    router.url = '/customers/new';
    routerEvents.next(new NavigationEnd(1, '/customers', '/customers/new'));
    expect(component.activeTempItem()).toEqual({ text: 'New customer' });

    component.ngOnChanges({
      sourceUrl: new SimpleChange('/old', '/customers/query', false),
    });
    const internal = component as any;
    internal.applySearchTermEvent({}, 'plain search');
    expect(() => internal.updateItemFromGlobalEvent({ idPath: 'id' }, { id: 'missing' })).not.toThrow();
    expect(internal.resolveGlobalEventRoute({ route: '/fixed', params: [{ '@type': 'unknown' }] }, {})).toBe('/fixed');
    expect(() => internal.initializeFilter({})).not.toThrow();
    expect(() =>
      internal.initializeFilter({ id: 'x', valuePath: 'x', filterDefinition: { '@type': 'missing' } }),
    ).not.toThrow();
    expect(() => internal.groupGlobalEventListeners([{}])).toThrow('Missing eventName');
    expect(() => internal.handleListGlobalEvent({ '@type': 'unknown' }, {})).toThrow('Unknown EventListenerType');
    expect(() => internal.addItemFromGlobalEvent({ idPath: 'id' }, {})).toThrow('Missing itemId');
    expect(() => internal.updateItemFromGlobalEvent({}, {})).toThrow('Missing idPath');
    expect(() => internal.applyListValueMapping(item, { '@type': 'unknown' }, 'x')).toThrow('Unknown Reference');
    expect(() => internal.updateFieldReferenceValue(item, {}, 'x')).toThrow('Missing fieldId');
    expect(() => internal.updateFieldReferenceValue(item, { fieldId: 'missing' }, 'x')).toThrow('not resolvable');
    expect(() => internal.updateSourcePathReferenceValue(item, {}, 'x')).toThrow('Missing valuePath');
    expect(() => internal.openRoutedContentFromGlobalEvent({}, {})).toThrow('Missing route');
    expect(() =>
      internal.resolveGlobalEventRoute(
        { route: '/:id', params: [{ '@type': 'event-payload', valuePath: 'id' }] },
        { id: 1 },
      ),
    ).toThrow('Missing param');
    expect(() =>
      internal.resolveGlobalEventRoute(
        { route: '/:id', params: [{ '@type': 'event-payload', param: ':id' }] },
        { id: 1 },
      ),
    ).toThrow('Missing valuePath');
    expect(() => internal.createListRegister({ results: [{}] })).toThrow('Unexpected item id');
    expect(() => internal.requireListItemId({})).toThrow('Unexpected item id');
    expect(() => component.addItem({} as any)).toThrow('Missing item id');
    const sourceData = component.sourceData();
    component.sourceData.set(null);
    expect(() => component.addItem({ id: '3', values: {} } as any)).toThrow('Missing source data');
    component.sourceData.set(sourceData);
    expect(() => internal.propagateItemClickEvent({}, item)).toThrow('Missing eventName');
    expect(() => internal.resolveItemClickRouteParam({ '@type': 'unknown' }, item)).toThrow('Unknown param Type');
    expect(() => internal.resolveItemClickRouteParam({ '@type': 'value', param: ':x' }, item)).toThrow(
      'Missing valuePath',
    );
    expect(() => internal.resolveItemClickValueReference({ '@type': 'field-reference' }, item)).toThrow(
      'Missing fieldId',
    );
    expect(() =>
      internal.resolveItemClickValueReference({ '@type': 'field-reference', fieldId: 'missing' }, item),
    ).toThrow('not resolvable');
    expect(() => internal.resolveItemClickValueReference({ '@type': 'source-path-reference' }, item)).toThrow(
      'Missing valuePath',
    );
    expect(() => internal.resolveItemClickValueReference({ '@type': 'unknown' }, item)).toThrow(
      'Unknown value reference type',
    );
    expect(
      internal.resolveItemClickValueReference({ '@type': 'source-path-reference', valuePath: 'status' }, item),
    ).toBe('active');
    component.sourceLoading.set(true);
    component.itemClickAction(new Event('click'), item, { '@type': 'open-inline-content' } as any);
    component.listAction(new Event('click'), { '@type': 'open-inline-content' } as any);
    component.sourceLoading.set(false);
    component.itemAction(new Event('click'), item, { '@type': 'open-inline-content' } as any);
    component.itemClickAction(new Event('click'), item, { '@type': 'open-inline-content' } as any);
    component.listAction(new Event('click'), { '@type': 'open-inline-content' } as any);
    expect(() => internal.registerListItemField(item, {}, {})).not.toThrow();
    expect(internal.resolveListFieldValue({ values: {} }, { fieldDefinition: { defaultValue: 'fallback' } })).toBe(
      'fallback',
    );
    expect(() => internal.applyRoutedItemState(['missing-text'])).not.toThrow();
    internal.openMatchedRoutedContent(
      {
        '@type': 'routed-inline-content',
        route: 'customers/:id/modal',
        itemId: ':id',
        modalContent: { '@type': 'formular' },
      },
      { ':id': '2' },
    );
    expect(component.modalContent()?.id).toBe('2');
    expect(modalService.add).toHaveBeenCalledWith(expect.objectContaining({ fullscreen: true }));
    expect(
      component.getItemClasses({ state: 'deleted' }, [
        {
          '@type': 'condition',
          compareValuePath: 'state',
          expectedValues: ['deleted'],
          positiveStyling: { opacity: 50, lineThrough: true },
        },
      ] as any),
    ).toEqual(['opacity-50', 'line-through']);
    expect(component.getItemClasses({}, [{ '@type': 'unknown' }] as any)).toEqual([]);
    expect(await firstValueFrom(internal.closeModalContent(undefined))).toBe(true);
    expect(modal.close).toHaveBeenCalled();
    expect(await firstValueFrom(internal.closeModalContent(['/customers']))).toBe(true);

    component.modalContent.set({
      modal,
      content: { '@type': 'formular' },
      id: '2',
      urlSegments: { ':id': '2' },
      parentRoutePath: '/customers/2',
    } as any);
    internal.openMatchedRoutedContent(
      { '@type': 'routed-inline-content', itemId: ':id', modalContent: component.modalContent()!.content },
      { ':id': '2' },
    );
    expect(await firstValueFrom(internal.closeModalContent(['/customers']))).toBe(true);
    internal.createModalCloseHandler(['/customers'])();
    await Promise.resolve();

    internal.lastListSubscription = { closed: false, unsubscribe: vi.fn() };
    internal.cancelLastListRequest();
    expect(internal.lastListSubscription.unsubscribe).toHaveBeenCalled();
    internal.lastSourceQuerySubscription = { closed: false, unsubscribe: vi.fn() };
    internal.routerUrlSubscription = { closed: false, unsubscribe: vi.fn() };
    internal.cancelLastSourceRequest();
    expect(internal.lastSourceQuerySubscription.unsubscribe).toHaveBeenCalled();

    fixture.componentRef.setInput('sourceUrl', undefined);
    expect(() => component.loadListSource()).toThrow('Missing listSource');
    fixture.componentRef.setInput('sourceUrl', '/customers/query');
    const originalPost = http.post;
    (http as any).post = vi.fn(() => throwError(() => new Error('query failed')));
    component.loadListSource();
    expect(component.sourceLoading()).toBe(false);
    (http as any).post = originalPost;

    expect(internal.applyRoutedContentState(['customers', '2'])).toBe(true);
    internal.openMatchedRoutedContent(
      {
        '@type': 'routed-inline-content',
        itemId: ':id',
        inlineContent: { '@type': 'list' },
      },
      { ':id': '3' },
    );
    expect(component.inlineContent()?.id).toBe('3');
    const currentModal = component.modalContent();
    if (currentModal) {
      internal.openMatchedRoutedContent(
        { '@type': 'routed-inline-content', itemId: ':id', modalContent: currentModal.content },
        { ':id': currentModal.id },
      );
    }
    component.listData.set({
      ...component.listData(),
      routedItems: [{ '@type': 'routed-item', route: 'broken' }],
    } as any);
    expect(() => internal.applyRoutedItemState(['broken'])).toThrow('Missing text property');

    fixture.componentRef.setInput('listUrl', undefined);
    await expect(firstValueFrom(internal.loadList(), { defaultValue: undefined })).resolves.toBeUndefined();
    fixture.componentRef.setInput('listUrl', '/broken-list');
    http.get = vi.fn(() => throwError(() => new Error('list failed'))) as any;
    await expect(firstValueFrom(internal.loadList())).rejects.toThrow('list failed');
    expect(component.listData()).toBeNull();
    component.ngOnDestroy();
    dispose();
    fixture.destroy();
    vi.useRealTimers();
  }, 15_000);
});
