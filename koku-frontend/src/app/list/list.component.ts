import {
  Component,
  ComponentRef,
  DestroyRef,
  inject,
  input,
  OnChanges,
  OnDestroy,
  Signal,
  signal,
  SimpleChanges,
  WritableSignal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { debounceTime, distinctUntilChanged, Observable, of, Subject, Subscription } from 'rxjs';
import { ListFieldEvent, ListItemComponent } from './list-item/list-item.component';
import { InputFieldComponent } from '../fields/input/input-field.component';
import { tap } from 'rxjs/operators';
import { IconComponent } from '../icon/icon.component';
import { ListItemPreviewComponent } from './list-item-preview/list-item-preview.component';
import { ListInlineContentComponent } from './list-inline-content/list-inline-content.component';
import { NavigationEnd, Router } from '@angular/router';
import { get } from '../utils/get';
import { ListItemActionComponent } from './list-item-action/list-item-action.component';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { set } from '../utils/set';
import { deepEqual } from '../utils/deepEqual';
import { UNIQUE_REF_GENERATOR } from '../utils/uniqueRef';
import { ModalService } from '../modal/modal.service';
import { ModalContentSetup, RenderedModalType } from '../modal/modal.type';
import { ListFilterComponent } from './list-filter/list-filter.component';
import { DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';
import { OutletDirective } from '../portal/outlet.directive';
import {
  childRouteSegments,
  matchRouteSegments,
  replaceRouteSegments,
  resolvedRoutePath,
  routePathSegments,
} from '../utils/route.utils';

export interface ListFieldRegistrationType {
  value: WritableSignal<any>;
  config: KokuDto.AbstractListViewFieldDto<any>;
  instance?: ComponentRef<any>;
}

export interface ListItemSetup {
  fieldSelection: string[];
  fields: Record<string, ListFieldRegistrationType>;
  actions: KokuDto.AbstractListViewItemActionDto[];
  clickAction?: KokuDto.AbstractListViewItemClickActionDto;
  preview?: KokuDto.AbstractListViewItemPreviewDto;
  id: string | null;
  source: WritableSignal<Record<string, any>>;
}

type ListViewFieldSelection = NonNullable<KokuDto.ListViewDto['fields']>[number];

export interface ListTempItem {
  text: string;
}

export interface ListFieldRenderContext {
  id: string;
  register: Signal<ListItemSetup>;
  fieldState: ListFieldRegistrationType;
  config: Signal<KokuDto.AbstractListViewFieldDto<any>>;
  submitting: Signal<boolean>;
  emit(eventName: ListFieldEvent, payload?: any): void;
}

export interface ListPreviewRenderContext {
  register: Signal<ListItemSetup>;
  preview: Signal<KokuDto.AbstractListViewItemPreviewDto>;
  value: Signal<any>;
}

export interface ListInlineContentRenderContext {
  content: Signal<KokuDto.AbstractListViewContentDto>;
  loading: Signal<boolean>;
  urlSegments: Signal<Record<string, string> | null>;
  parentRoutePath: Signal<string>;
  buttonDockOutlet: Signal<OutletDirective | undefined>;
  context: Signal<Record<string, any> | undefined>;
  close(): void;
  openRoutedContent(routes: string[]): void;
}

export interface ListItemActionRenderContext {
  action: Signal<KokuDto.AbstractListViewItemActionDto>;
  register: Signal<ListItemSetup>;
  listRegister: Signal<ListItemSetup[]>;
  contentSetup: Signal<ListContentSetup>;
  urlSegments: Signal<Record<string, string> | null>;
  parent: ListItemActionComponent;
}

export interface ListFilterRenderContext {
  filter: Signal<KokuDto.ListViewFilterContentDto>;
  filterDefinition: Signal<KokuDto.AbstractListViewFilterDto>;
  emit(predicates: KokuDto.QueryPredicate[]): void;
}

export type ListFieldRecipeFactory = (context: ListFieldRenderContext) => DynamicRenderRecipe;
export type ListPreviewRecipeFactory = (context: ListPreviewRenderContext) => DynamicRenderRecipe;
export type ListInlineContentRecipeFactory = (context: ListInlineContentRenderContext) => DynamicRenderRecipe;
export type ListItemActionRecipeFactory = (context: ListItemActionRenderContext) => DynamicRenderRecipe;
export type ListFilterRecipeFactory = (context: ListFilterRenderContext) => DynamicRenderRecipe;
export interface ListFilterDefinition {
  createRecipe: ListFilterRecipeFactory;
  initialPredicates(filter: KokuDto.AbstractListViewFilterDto): KokuDto.QueryPredicate[];
}

export type ItemStylingSetup = Partial<
  Record<
    string,
    {
      itemClasses?(
        stylingDefinition: KokuDto.AbstractListViewGlobalItemStylingDto,
        source: Record<string, any>,
      ): string[];
    }
  >
>;

export type ListFilterSetup = Readonly<Record<string, ListFilterDefinition | undefined>>;

export interface ListContentSetup {
  fieldRegistry: Partial<Record<string, ListFieldRecipeFactory>>;
  previewRegistry: Partial<Record<string, ListPreviewRecipeFactory>>;
  inlineContentRegistry: Partial<Record<string, ListInlineContentRecipeFactory>>;
  actionRegistry: Partial<Record<string, ListItemActionRecipeFactory>>;
  filterRegistry: ListFilterSetup;
  modalRegistry: ModalContentSetup;
  itemStylingRegistry: ItemStylingSetup;
}

interface ExtendedAbstractListViewItemActionDto extends KokuDto.AbstractListViewItemActionDto {
  loading: boolean;
}

type ListGlobalEventListener = NonNullable<KokuDto.ListViewDto['globalEventListeners']>[number];
type ListGlobalEventRegistry = Record<string, ((eventPayload: any) => void)[]>;

export interface ListInlineItem {
  content: KokuDto.AbstractListViewContentDto | null;
  id: string | null;
  parentRoutePath?: string;
  urlSegments: Record<string, string> | null;
}
export interface ListModalItem {
  modal: RenderedModalType;
  content: KokuDto.AbstractListViewContentDto | null;
  id: string | null;
  parentRoutePath?: string;
  urlSegments: Record<string, string> | null;
}

@Component({
  selector: 'list',
  imports: [
    ListItemComponent,
    InputFieldComponent,
    IconComponent,
    ListItemPreviewComponent,
    ListInlineContentComponent,
    ListItemActionComponent,
    ListFilterComponent,
  ],
  host: { class: 'overflow-auto' },
  templateUrl: './list.component.html',
})
export class ListComponent implements OnDestroy, OnChanges {
  httpClient = inject(HttpClient);
  destroyRef = inject(DestroyRef);
  router = inject(Router);
  modalService = inject(ModalService);

  contentSetup = input.required<ListContentSetup>();
  listUrl = input<string>();
  sourceUrl = input<string>();
  urlSegments = input<Record<string, string> | null>(null);
  parentRoutePath = input<string>('');
  context = input<Record<string, any>>();

  sourceLoading = signal(true);
  sourceData = signal<KokuDto.ListPage | null>(null);
  listData = signal<KokuDto.ListViewDto | null>(null);

  listRegister = signal<ListItemSetup[]>([]);
  inlineContent = signal<ListInlineItem | null>(null);
  modalContent = signal<ListModalItem | null>(null);
  activeTempItem = signal<ListTempItem | null>(null);

  currentPage = signal<number>(0);
  globalSearchTerm = signal<string>('');
  showFilters = signal<boolean>(false);
  globalSearchTermSubject = new Subject<string>();

  private lastSourceQuery: Partial<KokuDto.ListQuery> | null = null;
  private lastSourceQuerySubscription: Subscription | undefined;
  private lastListSubscription: Subscription | undefined;
  private routerUrlSubscription: Subscription | undefined;
  private globalSearchTermSubscription: Subscription | undefined;
  private filters: Record<
    string,
    {
      valuePath: string;
      predicates: KokuDto.QueryPredicate[];
    }
  > = {};

  componentRef = UNIQUE_REF_GENERATOR.generate();

  ngOnDestroy(): void {
    this.clearGlobalEventListeners();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['listUrl']) {
      this.loadList().subscribe({
        next: () => {
          this.loadListSource();
        },
      });
    } else if (changes['sourceUrl']) {
      this.loadListSource();
    }
  }

  private loadList(): Observable<KokuDto.ListViewDto | void> {
    const listUrlSnapshot = this.listUrl();
    if (!listUrlSnapshot) {
      return of();
    }

    this.cancelLastListRequest();
    return new Observable((subscriber) => {
      this.lastListSubscription = this.httpClient.get<KokuDto.ListViewDto>(listUrlSnapshot).subscribe({
        next: (listData) => {
          this.handleListLoaded(listData);
          subscriber.next(listData);
          subscriber.complete();
        },
        error: (error) => {
          this.listData.set(null);
          subscriber.error(error);
        },
      });
    });
  }

  loadListSource(query?: Partial<KokuDto.ListQuery>) {
    const sourceUrlSnapshot = this.sourceUrl();
    const listDataSnapshot = this.listData();
    if (!sourceUrlSnapshot || listDataSnapshot === undefined) {
      throw new Error('Missing listSource');
    }

    this.sourceLoading.set(true);
    this.cancelLastSourceRequest();

    const newQueryParams = {
      ...this.lastSourceQuery,
      ...query,
    };
    const newQuery = this.createSourceQuery(sourceUrlSnapshot, newQueryParams);

    this.lastSourceQuerySubscription = newQuery.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (listData) => this.handleSourcePageLoaded(listData),
      error: () => {
        this.sourceLoading.set(false);
      },
    });
    return newQuery;
  }

  private cancelLastListRequest(): void {
    if (this.lastListSubscription?.closed === false) {
      this.lastListSubscription.unsubscribe();
    }
  }

  private handleListLoaded(listData: KokuDto.ListViewDto): void {
    this.clearGlobalEventListeners();
    this.registerListGlobalEventListeners(listData);
    this.initializeFilters(listData);
    this.registerGlobalSearchSubscription();
    this.listData.set(listData);
  }

  private registerListGlobalEventListeners(listData: KokuDto.ListViewDto): void {
    const registeredEventListeners = this.groupGlobalEventListeners(listData.globalEventListeners);
    for (const currentEventListenerName of Object.keys(registeredEventListeners)) {
      GLOBAL_EVENT_BUS.addGlobalEventListener(String(this.componentRef), currentEventListenerName, (eventPayload) =>
        this.runRegisteredGlobalEventListeners(registeredEventListeners, currentEventListenerName, eventPayload),
      );
    }
  }

  private groupGlobalEventListeners(listeners: ListGlobalEventListener[] | undefined): ListGlobalEventRegistry {
    const registeredEventListeners: ListGlobalEventRegistry = {};
    for (const currentEventListener of listeners || []) {
      if (!currentEventListener.eventName) {
        throw new Error('Missing eventName in Global Listener Configuration');
      }
      registeredEventListeners[currentEventListener.eventName] ??= [];
      registeredEventListeners[currentEventListener.eventName].push((eventPayload: any) =>
        this.handleListGlobalEvent(currentEventListener, eventPayload),
      );
    }
    return registeredEventListeners;
  }

  private runRegisteredGlobalEventListeners(
    registeredEventListeners: ListGlobalEventRegistry,
    eventName: string,
    eventPayload: any,
  ): void {
    for (const currentEventListener of registeredEventListeners[eventName] || []) {
      currentEventListener(eventPayload);
    }
  }

  private handleListGlobalEvent(currentEventListener: ListGlobalEventListener, eventPayload: any): void {
    if (currentEventListener['@type'] === 'event-payload-search-term') {
      this.applySearchTermEvent(
        currentEventListener as KokuDto.ListViewEventPayloadSearchTermGlobalEventListenerDto,
        eventPayload,
      );
      return;
    }
    if (currentEventListener['@type'] === 'item-add') {
      this.addItemFromGlobalEvent(
        currentEventListener as KokuDto.ListViewEventPayloadAddItemGlobalEventListenerDto,
        eventPayload,
      );
      return;
    }
    if (currentEventListener['@type'] === 'item-update-via-event-payload') {
      this.updateItemFromGlobalEvent(
        currentEventListener as KokuDto.ListViewEventPayloadItemUpdateGlobalEventListenerDto,
        eventPayload,
      );
      return;
    }
    if (currentEventListener['@type'] === 'open-routed-content') {
      this.openRoutedContentFromGlobalEvent(
        currentEventListener as KokuDto.ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto,
        eventPayload,
      );
      return;
    }
    throw new Error(`Unknown EventListenerType ${currentEventListener['@type']}`);
  }

  private applySearchTermEvent(
    eventListener: KokuDto.ListViewEventPayloadSearchTermGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    if (eventListener.valuePath !== undefined) {
      this.globalSearchTermSubject.next(get(eventPayload, eventListener.valuePath));
      return;
    }
    if (typeof eventPayload === 'string') {
      this.globalSearchTermSubject.next(eventPayload);
    }
  }

  private addItemFromGlobalEvent(
    eventListener: KokuDto.ListViewEventPayloadAddItemGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    const itemId = get(eventPayload, eventListener.idPath || '', null);
    if (!itemId) {
      throw new Error('Missing itemId');
    }
    const newItemInstance = this.addItem({
      id: String(itemId),
      values: {},
    });
    this.applyListValueMappings(newItemInstance, eventListener.valueMapping, eventPayload);
  }

  private updateItemFromGlobalEvent(
    eventListener: KokuDto.ListViewEventPayloadItemUpdateGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    if (!eventListener.idPath) {
      throw new Error('Missing idPath configuration in EventListener');
    }
    const item = this.indexListRegister()[String(get(eventPayload, eventListener.idPath, ''))];
    if (item === undefined) {
      return;
    }
    this.applyListValueMappings(item, eventListener.valueMapping, eventPayload);
  }

  private indexListRegister(): Record<string, ListItemSetup> {
    const listRegisterIdx: Record<string, ListItemSetup> = {};
    for (const currentEntry of this.listRegister()) {
      if (currentEntry.id != null) {
        listRegisterIdx[currentEntry.id] = currentEntry;
      }
    }
    return listRegisterIdx;
  }

  private applyListValueMappings(
    item: ListItemSetup,
    valueMapping: Record<string, KokuDto.ListViewReference> | undefined,
    eventPayload: any,
  ): void {
    for (const [currentMappingPath, listViewReference] of Object.entries(valueMapping || {})) {
      const mappablePayloadValue = get(eventPayload, currentMappingPath);
      if (mappablePayloadValue !== undefined) {
        this.applyListValueMapping(item, listViewReference, mappablePayloadValue);
      }
    }
  }

  private applyListValueMapping(
    item: ListItemSetup,
    listViewReference: KokuDto.ListViewReference,
    mappablePayloadValue: any,
  ): void {
    if (listViewReference['@type'] === 'field-reference') {
      this.updateFieldReferenceValue(item, listViewReference as KokuDto.ListViewFieldReference, mappablePayloadValue);
      return;
    }
    if (listViewReference['@type'] === 'source-path-reference') {
      this.updateSourcePathReferenceValue(
        item,
        listViewReference as KokuDto.ListViewSourcePathReference,
        mappablePayloadValue,
      );
      return;
    }
    throw new Error(`Unknown Reference ${listViewReference['@type']}`);
  }

  private updateFieldReferenceValue(
    item: ListItemSetup,
    reference: KokuDto.ListViewFieldReference,
    mappablePayloadValue: any,
  ): void {
    if (!reference.fieldId) {
      throw new Error('Missing fieldId in FieldReference');
    }
    const field = item.fields[reference.fieldId];
    if (!field) {
      throw new Error('FieldReference not resolvable');
    }
    field.value.set(mappablePayloadValue);
  }

  private updateSourcePathReferenceValue(
    item: ListItemSetup,
    reference: KokuDto.ListViewSourcePathReference,
    mappablePayloadValue: any,
  ): void {
    if (!reference.valuePath) {
      throw new Error('Missing valuePath in FieldReference');
    }
    const itemSourceSnapshot = { ...item.source() };
    set(itemSourceSnapshot, reference.valuePath, mappablePayloadValue);
    item.source.set(itemSourceSnapshot);
  }

  private openRoutedContentFromGlobalEvent(
    eventListener: KokuDto.ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto,
    eventPayload: any,
  ): void {
    if (!eventListener.route) {
      throw new Error('Missing route configuration in EventListener');
    }
    this.openRoutedContent(this.resolveGlobalEventRoute(eventListener, eventPayload).split('/'));
  }

  private resolveGlobalEventRoute(
    eventListener: KokuDto.ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto,
    eventPayload: any,
  ): string {
    let route = eventListener.route || '';
    for (const currentParam of eventListener.params || []) {
      if (currentParam['@type'] !== 'event-payload') {
        continue;
      }
      const castedParam = currentParam as KokuDto.ListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto;
      if (!castedParam.param) {
        throw new Error('Missing param configuration in EventListener');
      }
      if (!castedParam.valuePath) {
        throw new Error('Missing valuePath configuration in EventListener');
      }
      route = route.replace(castedParam.param, get(eventPayload, castedParam.valuePath));
    }
    return route;
  }

  private initializeFilters(listData: KokuDto.ListViewDto): void {
    this.filters = {};
    for (const currentFilter of listData.filters || []) {
      this.initializeFilter(currentFilter);
    }
  }

  private initializeFilter(currentFilter: KokuDto.ListViewFilterContentDto): void {
    const filterDefinitionConfig = currentFilter.filterDefinition;
    if (!filterDefinitionConfig || !currentFilter.id || !currentFilter.valuePath) {
      return;
    }
    const filterDefinition = this.contentSetup().filterRegistry[filterDefinitionConfig['@type']];
    const filterQueryPredicates = filterDefinition?.initialPredicates(filterDefinitionConfig);
    if (!filterQueryPredicates) {
      return;
    }
    this.filters[currentFilter.id] = {
      valuePath: currentFilter.valuePath,
      predicates: filterQueryPredicates,
    };
  }

  private registerGlobalSearchSubscription(): void {
    this.globalSearchTermSubscription?.unsubscribe();
    this.globalSearchTermSubscription = this.globalSearchTermSubject
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe({
        next: (term) => {
          this.currentPage.set(0);
          this.globalSearchTerm.set(term);
          this.loadListSource({
            globalSearchTerm: term,
            page: this.currentPage(),
          });
        },
      });
  }

  private cancelLastSourceRequest(): void {
    if (this.lastSourceQuerySubscription?.closed === false) {
      this.lastSourceQuerySubscription.unsubscribe();
    }
    if (this.routerUrlSubscription?.closed === false) {
      this.routerUrlSubscription.unsubscribe();
    }
  }

  private createSourceQuery(sourceUrlSnapshot: string, newQueryParams: Partial<KokuDto.ListQuery>) {
    return this.httpClient
      .post<KokuDto.ListPage>(sourceUrlSnapshot, {
        ...newQueryParams,
        fieldSelection: this.listData()?.fieldFetchPaths || [],
        fieldPredicates: this.buildFieldPredicates(),
      } as KokuDto.ListQuery)
      .pipe(
        tap(() => {
          this.lastSourceQuery = newQueryParams;
        }),
      );
  }

  private buildFieldPredicates(): Record<string, KokuDto.ListFieldQuery> {
    const fieldPredicates: Record<string, KokuDto.ListFieldQuery> = {};
    for (const currentFilter of Object.values(this.filters)) {
      this.addFilterPredicates(fieldPredicates, currentFilter);
    }
    return fieldPredicates;
  }

  private addFilterPredicates(
    fieldPredicates: Record<string, KokuDto.ListFieldQuery>,
    currentFilter: { valuePath: string; predicates: KokuDto.QueryPredicate[] },
  ): void {
    fieldPredicates[currentFilter.valuePath] = {
      predicates: [...(fieldPredicates[currentFilter.valuePath]?.predicates || []), ...currentFilter.predicates],
    };
  }

  private handleSourcePageLoaded(listData: KokuDto.ListPage): void {
    this.listRegister.set(this.createListRegister(listData));
    this.sourceData.set(listData);
    this.subscribeToRouteChanges();
    this.applyNavigationState();
    this.sourceLoading.set(false);
  }

  private createListRegister(listData: KokuDto.ListPage): ListItemSetup[] {
    const newFieldRegister: ListItemSetup[] = [];
    for (const currentResult of listData.results || []) {
      if (currentResult.id === undefined) {
        throw new Error('Unexpected item id');
      }
      newFieldRegister.push(this.prepareListItem(currentResult));
    }
    return newFieldRegister;
  }

  private subscribeToRouteChanges(): void {
    this.routerUrlSubscription = this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (evnt) => {
        if (evnt instanceof NavigationEnd) {
          this.applyNavigationState();
        }
      },
    });
  }

  private applyNavigationState(): void {
    const segments = childRouteSegments(this.router.url, this.parentRoutePath());
    if (!this.applyRoutedContentState(segments)) {
      this.closeInlineContentWithoutRouteChange().subscribe();
    }
    if (!this.applyRoutedItemState(segments)) {
      this.hideTempItem();
    }
  }

  private applyRoutedContentState(segments: string[]): boolean {
    const routedContentMatch = this.findRouteMatch(this.listData()?.routedContents, segments);
    if (!routedContentMatch) {
      return false;
    }

    if (routedContentMatch.item['@type'] === 'routed-inline-content') {
      this.openMatchedRoutedContent(routedContentMatch.item, routedContentMatch.segmentMapping);
    }
    return true;
  }

  private findRouteMatch<TItem extends { route?: string }>(
    items: TItem[] | undefined,
    segments: string[],
  ): { item: TItem; segmentMapping: Record<string, string> } | null {
    for (const item of items || []) {
      const segmentMapping = matchRouteSegments(item.route, segments);
      if (segmentMapping) {
        return { item, segmentMapping };
      }
    }
    return null;
  }

  private openMatchedRoutedContent(
    currentRoutedContent: KokuDto.AbstractListViewRoutedContentDto,
    segmentMapping: Record<string, string>,
  ): void {
    const castedRouteContent = currentRoutedContent as KokuDto.ListViewRoutedContentDto;
    const urlSegmentsSnapshot = this.urlSegments();
    const newUrlSegments = urlSegmentsSnapshot ? { ...urlSegmentsSnapshot, ...segmentMapping } : segmentMapping;
    if (castedRouteContent.inlineContent) {
      this.openMatchedInlineContent(castedRouteContent, newUrlSegments);
    } else if (castedRouteContent.modalContent) {
      this.openMatchedModalContent(castedRouteContent, newUrlSegments);
    }
  }

  private openMatchedInlineContent(
    castedRouteContent: KokuDto.ListViewRoutedContentDto,
    newUrlSegments: Record<string, string>,
  ): void {
    const itemId = this.resolveRouteItemId(castedRouteContent.itemId, newUrlSegments);
    if (
      this.inlineContent()?.id !== itemId ||
      this.inlineContent()?.content !== castedRouteContent.inlineContent ||
      !deepEqual(this.inlineContent()?.urlSegments, newUrlSegments)
    ) {
      this.openInlineContent({
        content: castedRouteContent.inlineContent!,
        id: itemId || null,
        parentRoutePath: resolvedRoutePath(this.parentRoutePath(), castedRouteContent.route, newUrlSegments),
        urlSegments: newUrlSegments,
      });
    }
  }

  private openMatchedModalContent(
    castedRouteContent: KokuDto.ListViewRoutedContentDto,
    newUrlSegments: Record<string, string>,
  ): void {
    const itemId = this.resolveRouteItemId(castedRouteContent.itemId, newUrlSegments);
    if (
      this.modalContent()?.id === itemId &&
      this.modalContent()?.content === castedRouteContent.modalContent &&
      deepEqual(this.modalContent()?.urlSegments, newUrlSegments)
    ) {
      return;
    }

    const oldRoutePath = this.createParentRouteParts(newUrlSegments);
    const newParentRoutePath = resolvedRoutePath(this.parentRoutePath(), castedRouteContent.route, newUrlSegments);
    const closeModal = this.createModalCloseHandler(oldRoutePath);
    const newModal = this.modalService.add({
      dynamicContent: castedRouteContent.modalContent!,
      urlSegments: newUrlSegments,
      dynamicContentSetup: this.contentSetup().modalRegistry,
      fullscreen: true,
      parentRoutePath: newParentRoutePath,
      clickOutside: closeModal,
      onCloseRequested: closeModal,
    });

    this.modalContent.set({
      modal: newModal,
      content: castedRouteContent.modalContent!,
      id: itemId || '',
      urlSegments: newUrlSegments,
      parentRoutePath: newParentRoutePath,
    });
  }

  private createParentRouteParts(newUrlSegments: Record<string, string>): string[] {
    return ['/', ...routePathSegments(resolvedRoutePath(this.parentRoutePath(), undefined, newUrlSegments))];
  }

  private createModalCloseHandler(oldRoutePath: string[]): () => void {
    return () => {
      this.closeModalContent(oldRoutePath).subscribe();
    };
  }

  private resolveRouteItemId(itemId: string | undefined, routeSegments: Record<string, string>): string | undefined {
    return itemId ? replaceRouteSegments(itemId, routeSegments) : undefined;
  }

  private applyRoutedItemState(segments: string[]): boolean {
    const routedItemMatch = this.findRouteMatch(this.listData()?.routedItems, segments);
    if (!routedItemMatch) {
      return false;
    }

    if (routedItemMatch.item['@type'] === 'routed-item') {
      const castedRouteContent = routedItemMatch.item as KokuDto.ListViewRoutedDummyItemDto;
      if (!castedRouteContent.text) {
        throw new Error('Missing text property in RoutedItem Configuration');
      }
      this.showTempItem(castedRouteContent.text);
    }
    return true;
  }

  itemAction(event: Event, result: ListItemSetup, action: ExtendedAbstractListViewItemActionDto) {
    if (action['@type'] === 'open-inline-content') {
      event.stopPropagation();
      const castedActionType = action as KokuDto.ListViewOpenInlineContentItemActionDto;
      if (castedActionType.inlineContent) {
        this.openInlineContent({
          content: castedActionType.inlineContent,
          id: result.id,
          urlSegments: null,
        });
      }
    }
  }

  itemClickAction(event: Event, item: ListItemSetup, action: KokuDto.AbstractListViewItemClickActionDto | undefined) {
    if (this.sourceLoading() || !action) {
      return;
    }

    if (action['@type'] === 'propagate-global-event') {
      this.propagateItemClickEvent(action as KokuDto.ListViewItemClickPropagateGlobalEventActionDto, item);
      return;
    }
    if (action['@type'] === 'open-routed-content') {
      this.openItemClickRoutedContent(action as KokuDto.ListViewItemClickOpenRoutedContentActionDto, item);
      return;
    }
    if (action['@type'] === 'open-inline-content') {
      this.openItemClickInlineContent(action as KokuDto.ListViewItemOpenInlineContentClickActionDto, item);
    }
  }

  listAction($event: Event, action: KokuDto.AbstractListViewActionDto) {
    if (this.sourceLoading() || !action) {
      return;
    }

    if (action['@type'] === 'open-inline-content') {
      this.openListInlineContent(action as KokuDto.ListViewOpenInlineContentActionDto);
    } else if (action['@type'] === 'open-routed-content') {
      this.openListRoutedContent(action as KokuDto.ListViewOpenRoutedContentActionDto);
    }
  }

  private propagateItemClickEvent(
    action: KokuDto.ListViewItemClickPropagateGlobalEventActionDto,
    item: ListItemSetup,
  ): void {
    if (!action.eventName) {
      throw new Error('Missing eventName');
    }
    GLOBAL_EVENT_BUS.propagateGlobalEvent(action.eventName, item.source());
  }

  private openItemClickRoutedContent(
    action: KokuDto.ListViewItemClickOpenRoutedContentActionDto,
    item: ListItemSetup,
  ): void {
    this.openRoutedContent(this.resolveItemClickRoute(action, item).split('/'));
  }

  private resolveItemClickRoute(
    action: KokuDto.ListViewItemClickOpenRoutedContentActionDto,
    item: ListItemSetup,
  ): string {
    let replacedRoute = action.route || '';
    for (const currentParam of action.params || []) {
      if (currentParam.param) {
        replacedRoute = replacedRoute.replaceAll(
          currentParam.param,
          this.resolveItemClickRouteParam(currentParam, item),
        );
      }
    }
    return replacedRoute;
  }

  private resolveItemClickRouteParam(
    currentParam: KokuDto.AbstractListViewItemClickOpenRoutedContentActionParamDto,
    item: ListItemSetup,
  ): string {
    if (currentParam['@type'] !== 'value') {
      throw new Error(`Unknown param Type: ${currentParam['@type']}`);
    }

    const castedParamType = currentParam as KokuDto.ListViewItemClickOpenRoutedContentActionItemValueParamDto;
    if (!castedParamType.valueReference) {
      throw new Error(`Missing valuePath for param: ${currentParam.param}`);
    }

    return this.resolveItemClickValueReference(castedParamType.valueReference, item);
  }

  private resolveItemClickValueReference(valueReference: KokuDto.ListViewReference, item: ListItemSetup): string {
    if (valueReference['@type'] === 'field-reference') {
      const castedReference = valueReference as KokuDto.ListViewFieldReference;
      if (!castedReference.fieldId) {
        throw new Error('Missing fieldId in FieldReference');
      }
      const field = item.fields[castedReference.fieldId];
      if (!field) {
        throw new Error('FieldReference not resolvable');
      }
      return field.value();
    }
    if (valueReference['@type'] === 'source-path-reference') {
      const castedReference = valueReference as KokuDto.ListViewSourcePathReference;
      if (!castedReference.valuePath) {
        throw new Error('Missing valuePath in FieldReference');
      }
      return get(item.source(), castedReference.valuePath);
    }
    throw new Error(`Unknown value reference type: ${valueReference['@type']}`);
  }

  private openItemClickInlineContent(
    action: KokuDto.ListViewItemOpenInlineContentClickActionDto,
    item: ListItemSetup,
  ): void {
    if (action.inlineContent) {
      this.openInlineContent({
        content: action.inlineContent,
        id: item.id,
        urlSegments: null,
      });
    }
  }

  private openListInlineContent(action: KokuDto.ListViewOpenInlineContentActionDto): void {
    if (action.inlineContent) {
      this.openInlineContent({
        content: action.inlineContent,
        id: null,
        urlSegments: null,
      });
    }
  }

  private openListRoutedContent(action: KokuDto.ListViewOpenRoutedContentActionDto): void {
    if (action.route) {
      this.openRoutedContent(routePathSegments(replaceRouteSegments(action.route, this.urlSegments())));
    }
  }

  closeInlineContent() {
    return new Observable<boolean>((observer) => {
      this.router
        .navigateByUrl(`/${resolvedRoutePath(this.parentRoutePath(), undefined, this.urlSegments())}`)
        .then((success) => {
          if (success) {
            this.inlineContent.set(null);
          }
          observer.next(success);
          observer.complete();
        });
    });
  }

  private closeInlineContentWithoutRouteChange() {
    return new Observable<boolean>((observer) => {
      this.inlineContent.set(null);
      observer.next(true);
      observer.complete();
    });
  }

  openRoutedContent(routes: string[]) {
    this.closeModalContent([...this.parentRoutePath().split('/'), ...routes]).subscribe();
  }

  openInlineContent(item: ListInlineItem) {
    this.inlineContent.set(item);
  }

  addItem(item: KokuDto.ListItem) {
    const listRegisterSnapshot = this.listRegister();
    if (!item.id) {
      throw new Error('Missing item id');
    }
    const sourceDataSnapshot = this.sourceData();
    if (!sourceDataSnapshot) {
      throw new Error('Missing source data');
    }
    const newItemInstance = this.prepareListItem(item);
    listRegisterSnapshot.unshift(newItemInstance);
    this.listRegister.set(listRegisterSnapshot);
    return newItemInstance;
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }

  private showTempItem(text: string) {
    this.activeTempItem.set({
      text,
    });
  }

  private hideTempItem() {
    this.activeTempItem.set(null);
  }

  private prepareListItem(listItem: KokuDto.ListItem) {
    const currentItemId = this.requireListItemId(listItem);
    const listDataSnapshot = this.listData();
    const currentResultListContentStates = this.createListItemSetup(listItem, currentItemId, listDataSnapshot);
    this.registerListItemFields(currentResultListContentStates, listItem, listDataSnapshot);
    return currentResultListContentStates;
  }

  private requireListItemId(listItem: KokuDto.ListItem): string {
    const currentItemId = listItem.id;
    if (currentItemId === undefined) {
      throw new Error('Unexpected item id');
    }
    return currentItemId;
  }

  private createListItemSetup(
    listItem: KokuDto.ListItem,
    currentItemId: string,
    listDataSnapshot: KokuDto.ListViewDto | null,
  ): ListItemSetup {
    const itemIdPath = listDataSnapshot?.itemIdPath;
    return {
      fieldSelection: [],
      fields: {},
      actions: (listDataSnapshot?.itemActions || []).map((value) => {
        return {
          ...value,
          loading: false,
        };
      }),
      clickAction: listDataSnapshot?.itemClickAction,
      preview: listDataSnapshot?.itemPreview,
      id: currentItemId,
      source: signal({
        ...listItem.values,
        ...(itemIdPath ? { [itemIdPath]: currentItemId } : {}),
      }),
    };
  }

  private registerListItemFields(
    currentResultListContentStates: ListItemSetup,
    listItem: KokuDto.ListItem,
    listDataSnapshot: KokuDto.ListViewDto | null,
  ): void {
    for (const currentFieldSelection of listDataSnapshot?.fields || []) {
      this.registerListItemField(currentResultListContentStates, listItem, currentFieldSelection);
    }
  }

  private registerListItemField(
    currentResultListContentStates: ListItemSetup,
    listItem: KokuDto.ListItem,
    currentFieldSelection: ListViewFieldSelection,
  ): void {
    const fieldDefinition = currentFieldSelection.fieldDefinition;
    if (!fieldDefinition || !currentFieldSelection.id || !this.hasFieldTypeSetup(fieldDefinition)) {
      return;
    }

    currentResultListContentStates.fields[currentFieldSelection.id] = this.createFieldState(
      fieldDefinition,
      this.resolveListFieldValue(listItem, currentFieldSelection),
    );
    currentResultListContentStates.fieldSelection.push(currentFieldSelection.id);
  }

  private hasFieldTypeSetup(fieldDefinition: KokuDto.AbstractListViewFieldDto<any>): boolean {
    return this.contentSetup().fieldRegistry[fieldDefinition['@type']] !== undefined;
  }

  private resolveListFieldValue(listItem: KokuDto.ListItem, currentFieldSelection: ListViewFieldSelection): any {
    const defaultValue = currentFieldSelection.fieldDefinition?.defaultValue ?? null;
    if (!currentFieldSelection.valuePath) {
      return defaultValue;
    }

    return get(
      {
        ...listItem.values,
      },
      currentFieldSelection.valuePath,
      defaultValue,
    );
  }

  private createFieldState(listContent: KokuDto.AbstractListViewFieldDto<any>, value: any): ListFieldRegistrationType {
    return {
      value: signal(value === undefined ? listContent.defaultValue : value),
      config: listContent,
    };
  }

  private closeModalContent(newRoute: string[]) {
    return new Observable<boolean>((observer) => {
      const modalContentSnapshot = this.modalContent();
      if (modalContentSnapshot) {
        const afterParentRouteOpened = (success: boolean) => {
          if (success) {
            modalContentSnapshot.modal.close();
            this.modalContent.set(null);
          }
          observer.next(success);
          observer.complete();
        };
        if (newRoute) {
          this.router.navigate(newRoute).then((success) => {
            afterParentRouteOpened(success);
          });
        } else {
          afterParentRouteOpened(true);
        }
      } else {
        this.router.navigate(newRoute).then(() => {
          observer.next(true);
          observer.complete();
        });
      }
    });
  }

  public setGlobalSearchTerm(newTerm: string) {
    this.currentPage.set(0);
    this.globalSearchTerm.set(newTerm);
    this.loadListSource({
      globalSearchTerm: this.globalSearchTerm(),
      page: this.currentPage(),
    });
  }

  getItemClasses(
    source: Record<string, any>,
    globalItemStyling: KokuDto.AbstractListViewGlobalItemStylingDto[] | undefined,
  ) {
    let result: string[] = [];
    for (const currentItemStyling of globalItemStyling || []) {
      const iconStylingRegister = this.contentSetup().itemStylingRegistry[currentItemStyling['@type']];
      if (iconStylingRegister?.itemClasses) {
        result = [...result, ...iconStylingRegister.itemClasses(currentItemStyling, source)];
      }
    }
    return result;
  }

  onFilterChange(id: string, valuePath: string, predicates: KokuDto.QueryPredicate[]) {
    this.filters[id] = {
      valuePath: valuePath,
      predicates: predicates,
    };
    this.currentPage.set(0);
    this.loadListSource({
      page: this.currentPage(),
    });
  }
}
