import {
  Component,
  ComponentRef,
  DestroyRef,
  inject,
  input,
  OnChanges,
  OnDestroy,
  signal,
  SimpleChanges,
  WritableSignal
} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {HttpClient} from '@angular/common/http';
import {Observable, of, Subscription} from 'rxjs';
import {ListItemComponent} from './list-item/list-item.component';
import {InputFieldComponent} from '../fields/input/input-field.component';
import {tap} from 'rxjs/operators';
import {IconComponent} from '../icon/icon.component';
import {ListItemPreviewComponent} from './list-item-preview/list-item-preview.component';
import {ListInlineContentComponent} from './list-inline-content/list-inline-content.component';
import {NavigationEnd, Router} from '@angular/router';
import {get} from '../utils/get';
import {ListItemActionComponent} from './list-item-action/list-item-action.component';
import {GLOBAL_EVENT_BUS} from '../events/global-events';
import {set} from '../utils/set';
import {deepEqual} from '../utils/deepEqual';
import {UNIQUE_REF_GENERATOR} from '../utils/uniqueRef';
import {ModalService} from '../modal/modal.service';
import {ModalContentSetup, RenderedModalType} from '../modal/modal.type';

export type ListFieldRegistrationType = {
  value: WritableSignal<any>,
  config: KokuDto.AbstractListViewFieldDto<any>,
  instance?: ComponentRef<any>
};

export interface ListItemSetup {

  fieldSelection: string[];
  fields: { [key: string]: ListFieldRegistrationType };
  actions: KokuDto.AbstractListViewItemActionDto[];
  clickAction?: KokuDto.AbstractListViewItemClickActionDto;
  preview?: KokuDto.AbstractListViewItemPreviewDto;
  id: string | null;
  source: WritableSignal<{ [index: string]: any }>;

}

export interface ListTempItem {

  text: string;

}

export type ItemStylingSetup = Partial<Record<KokuDto.AbstractListViewGlobalItemStylingDto["@type"] | string, {
  itemClasses?(stylingDefinition: KokuDto.AbstractListViewGlobalItemStylingDto, source: {
    [p: string]: any
  }): string[]
}>>;

export interface ListContentSetup {
  fieldRegistry: Partial<Record<KokuDto.AbstractListViewFieldDto<any>["@type"] | string, {
    componentType: any;
    stateInitializer: (listContent: KokuDto.AbstractListViewFieldDto<any>, value: any) => ListFieldRegistrationType,
    inputBindings?(instance: ListItemComponent, key: string, listContent: KokuDto.AbstractListViewFieldDto<any>): {
      [key: string]: any
    }
    outputBindings?(instance: ListItemComponent, key: string, listContent: KokuDto.AbstractListViewFieldDto<any>): {
      [key: string]: any
    }
  }>>,
  previewRegistry: Partial<Record<KokuDto.AbstractListViewItemPreviewDto["@type"] | string, {
    componentType: any;
    inputBindings?(instance: ListItemPreviewComponent, listPreviewContent: KokuDto.AbstractListViewItemPreviewDto): {
      [key: string]: any
    }
    outputBindings?(instance: ListItemPreviewComponent, listPreviewContent: KokuDto.AbstractListViewItemPreviewDto): {
      [key: string]: any
    }
  }>>,
  inlineContentRegistry: Partial<Record<KokuDto.AbstractListViewContentDto["@type"] | string, {
    componentType: any;
    inputBindings?(instance: ListInlineContentComponent, inlineContent: KokuDto.AbstractListViewContentDto): {
      [key: string]: any
    }
    outputBindings?(instance: ListInlineContentComponent, inlineContent: KokuDto.AbstractListViewContentDto): {
      [key: string]: any
    }
  }>>,
  actionRegistry: Partial<Record<KokuDto.AbstractListViewItemActionDto["@type"] | string, {
    componentType: any;
    inputBindings?(instance: ListItemActionComponent, inlineContent: KokuDto.AbstractListViewItemActionDto): {
      [key: string]: any
    }
    outputBindings?(instance: ListItemActionComponent, inlineContent: KokuDto.AbstractListViewItemActionDto): {
      [key: string]: any
    }
  }>>,
  modalRegistry: ModalContentSetup,
  itemStylingRegistry: ItemStylingSetup,
}

interface ExtendedAbstractListViewItemActionDto extends KokuDto.AbstractListViewItemActionDto {

  loading: boolean;

}


export type ListInlineItem = {
  content: KokuDto.AbstractListViewContentDto | null;
  id: string | null;
  parentRoutePath?: string;
  urlSegments: { [key: string]: string } | null;
};
export type ListModalItem = {
  modal: RenderedModalType;
  content: KokuDto.AbstractListViewContentDto | null;
  id: string | null;
  parentRoutePath?: string;
  urlSegments: { [key: string]: string } | null;
};

@Component({
  selector: 'list',
  imports: [
    ListItemComponent,
    InputFieldComponent,
    IconComponent,
    ListItemPreviewComponent,
    ListInlineContentComponent,
    ListItemActionComponent,
  ],
  templateUrl: './list.component.html',
  styleUrl: './list.component.css'
})
export class ListComponent implements OnDestroy, OnChanges {

  httpClient = inject(HttpClient);
  destroyRef = inject(DestroyRef);
  router = inject(Router);
  modalService = inject(ModalService);

  contentSetup = input.required<ListContentSetup>();
  listUrl = input<string>();
  sourceUrl = input<string>();
  urlSegments = input<{ [key: string]: string } | null>(null);
  parentRoutePath = input<string>('');
  context = input<{ [key: string]: any }>();

  sourceLoading = signal(true);
  sourceData = signal<KokuDto.ListPage | null>(null);
  listData = signal<KokuDto.ListViewDto | null>(null);

  listRegister = signal<ListItemSetup[]>([]);
  inlineContent = signal<ListInlineItem | null>(null);
  modalContent = signal<ListModalItem | null>(null);
  activeTempItem = signal<ListTempItem | null>(null);

  currentPage = signal<number>(0);
  globalSearchTerm = signal<string>('');

  private lastSourceQuery: Partial<KokuDto.ListQuery> | null = null;
  private lastSourceQuerySubscription: Subscription | undefined;
  private lastListSubscription: Subscription | undefined;
  private routerUrlSubscription: Subscription | undefined;

  componentRef = UNIQUE_REF_GENERATOR.generate();

  ngOnDestroy(): void {
    this.clearGlobalEventListeners();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['listUrl']) {
      this.loadList().subscribe(() => {
        this.loadListSource();
      });
    } else if (changes['sourceUrl']) {
      this.loadListSource();
    }
  }

  private loadList() {
    const listUrlSnapshot = this.listUrl();
    if (listUrlSnapshot) {
      if (this.lastListSubscription && !this.lastListSubscription.closed) {
        this.lastListSubscription.unsubscribe();
      }
      return new Observable(subscriber => {
        this.lastListSubscription = this.httpClient.get<KokuDto.ListViewDto>(listUrlSnapshot).subscribe({
          next: (listData) => {
            this.clearGlobalEventListeners();

            const registeredEventListeners: { [key: string]: ((eventPayload: any) => void)[] } = {};
            for (const currentEventListener of listData.globalEventListeners || []) {
              if (!currentEventListener.eventName) {
                throw new Error('Missing eventName in Global Listener Configuration');
              }
              if (!registeredEventListeners[currentEventListener.eventName]) {
                registeredEventListeners[currentEventListener.eventName] = [];
              }
              registeredEventListeners[currentEventListener.eventName].push((eventPayload: any) => {
                switch (currentEventListener['@type']) {
                  case "event-payload-search-term": {
                    const castedEventListener = currentEventListener as KokuDto.ListViewEventPayloadSearchTermGlobalEventListenerDto;

                    let newSearchTerm;
                    if (castedEventListener.valuePath !== undefined) {
                      newSearchTerm = get(eventPayload, castedEventListener.valuePath);
                    } else if (typeof eventPayload === 'string') {
                      newSearchTerm = eventPayload;
                    }


                    break;
                  }
                  case "item-add": {
                    const castedEventListener = currentEventListener as KokuDto.ListViewEventPayloadAddItemGlobalEventListenerDto;

                    const itemId = get(eventPayload, castedEventListener.idPath || '', null);
                    if (!itemId) {
                      throw new Error('Missing itemId');
                    }

                    const newItemInstance = this.addItem({
                      id: String(itemId),
                      values: {}
                    });

                    for (const [currentMappingPath, listViewReference] of Object.entries(castedEventListener.valueMapping || {})) {
                      const mappablePayloadValue = get(eventPayload, currentMappingPath);
                      if (mappablePayloadValue !== undefined) {
                        switch (listViewReference['@type']) {
                          case "field-reference": {
                            const castedReference = listViewReference as KokuDto.ListViewFieldReference;
                            if (!castedReference.fieldId) {
                              throw new Error('Missing fieldId in FieldReference');
                            }
                            const field = newItemInstance.fields[castedReference.fieldId];
                            if (!field) {
                              throw new Error('FieldReference not resolvable');
                            }
                            field.value.set(mappablePayloadValue);
                            break;
                          }
                          case "source-path-reference": {
                            const castedReference = listViewReference as KokuDto.ListViewSourcePathReference;
                            if (!castedReference.valuePath) {
                              throw new Error('Missing valuePath in FieldReference');
                            }
                            const itemSourceSnapshot = {
                              ...newItemInstance.source()
                            };
                            set(itemSourceSnapshot, castedReference.valuePath, mappablePayloadValue);
                            newItemInstance.source.set(itemSourceSnapshot);
                            break;
                          }
                          default: {
                            throw new Error(`Unknown Reference ${listViewReference['@type']}`);
                          }
                        }
                      }
                    }
                    break;
                  }
                  case "item-update-via-event-payload": {
                    const castedEventListener = currentEventListener as KokuDto.ListViewEventPayloadItemUpdateGlobalEventListenerDto;

                    if (!castedEventListener.idPath) {
                      throw new Error('Missing idPath configuration in EventListener');
                    }

                    const listRegisterIdx: { [key: string]: ListItemSetup } = {};
                    for (const currentEntry of this.listRegister()) {
                      if (currentEntry.id !== undefined && currentEntry.id !== null) {
                        listRegisterIdx[currentEntry.id] = currentEntry;
                      }
                    }
                    const item = listRegisterIdx[String(get(eventPayload, castedEventListener.idPath, ''))];
                    // it might be possible that the item is (currently) not shown. in this case, we cannot update the list.
                    if (item !== undefined) {
                      for (const [currentMappingPath, listViewReference] of Object.entries(castedEventListener.valueMapping || {})) {
                        const mappablePayloadValue = get(eventPayload, currentMappingPath);
                        if (mappablePayloadValue !== undefined) {
                          switch (listViewReference['@type']) {
                            case "field-reference": {
                              const castedReference = listViewReference as KokuDto.ListViewFieldReference;
                              if (!castedReference.fieldId) {
                                throw new Error('Missing fieldId in FieldReference');
                              }
                              const field = item.fields[castedReference.fieldId];
                              if (!field) {
                                throw new Error('FieldReference not resolvable');
                              }
                              field.value.set(mappablePayloadValue);
                              break;
                            }
                            case "source-path-reference": {
                              const castedReference = listViewReference as KokuDto.ListViewSourcePathReference;
                              if (!castedReference.valuePath) {
                                throw new Error('Missing valuePath in FieldReference');
                              }
                              const itemSourceSnapshot = {
                                ...item.source()
                              };
                              set(itemSourceSnapshot, castedReference.valuePath, mappablePayloadValue);
                              item.source.set(itemSourceSnapshot);
                              break;
                            }
                            default: {
                              throw new Error(`Unknown Reference ${listViewReference['@type']}`);
                            }
                          }
                        }
                      }
                    }

                    break;
                  }
                  case "open-routed-content": {
                    const castedEventListener = currentEventListener as KokuDto.ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto;

                    if (!castedEventListener.route) {
                      throw new Error('Missing route configuration in EventListener');
                    }

                    let route = castedEventListener.route;
                    for (const currentParam of castedEventListener.params || []) {
                      switch (currentParam['@type']) {
                        case "event-payload": {
                          const castedParam = currentParam as KokuDto.ListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto;
                          if (!castedParam.param) {
                            throw new Error('Missing param configuration in EventListener');
                          }
                          if (!castedParam.valuePath) {
                            throw new Error('Missing valuePath configuration in EventListener');
                          }
                          route = route.replace(castedParam.param, get(eventPayload, castedParam.valuePath));
                        }
                      }
                    }
                    this.openRoutedContent(route.split('/'))
                    break;
                  }
                  default: {
                    throw new Error(`Unknown EventListenerType ${currentEventListener['@type']}`);
                  }
                }
              })
            }

            for (const currentEventListenerName of new Set(Object.keys(registeredEventListeners))) {
              GLOBAL_EVENT_BUS.addGlobalEventListener(String(this.componentRef), currentEventListenerName, (eventPayload) => {
                for (const currentEventListener of registeredEventListeners[currentEventListenerName] || []) {
                  currentEventListener(eventPayload);
                }
              });
            }

            this.listData.set(listData);
            subscriber.next(listData);
            subscriber.complete();
          },
          error: (error) => {
            this.listData.set(null);
            subscriber.error(error);
          }
        });
      });
    } else {
      return of();
    }
  }

  loadListSource(query?: Partial<KokuDto.ListQuery>) {
    const sourceUrlSnapshot = this.sourceUrl();
    const listDataSnapshot = this.listData();
    if (sourceUrlSnapshot && listDataSnapshot !== undefined) {
      this.sourceLoading.set(true);
      if (this.lastSourceQuerySubscription && !this.lastSourceQuerySubscription.closed) {
        this.lastSourceQuerySubscription.unsubscribe();
      }
      if (this.routerUrlSubscription && !this.routerUrlSubscription.closed) {
        this.routerUrlSubscription.unsubscribe();
      }
      const newQuery = this.httpClient.post<KokuDto.ListPage>(sourceUrlSnapshot, {
        ...(this.lastSourceQuery || {}),
        ...(query || {}),
        fieldSelection: (this.listData() || {}).fieldFetchPaths || []
      } as KokuDto.ListQuery).pipe(
        tap(() => {
          this.lastSourceQuery = query || {};
        })
      );

      this.lastSourceQuerySubscription = newQuery.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: (listData) => {
          const newFieldRegister: ListItemSetup[] = [];
          for (const currentResult of listData.results || []) {
            const currentItemId = currentResult.id;
            if (currentItemId === undefined) {
              throw new Error('Unexpected item id');
            }
            newFieldRegister.push(this.prepareListItem(listData, currentResult));
          }
          this.listRegister.set(newFieldRegister);
          this.sourceData.set(listData);

          const afterNavigationUrlChange = () => {
            const segments = this.router.url.split('/').filter(value => value !== '').slice(this.parentRoutePath().split('/').filter(value => value !== '').length);
            let routedContentFound = false;
            for (const currentRoutedContent of ((this.listData() || {}).routedContents || [])) {
              if (currentRoutedContent.route) {
                let failedLookup = false;
                let segmentIdx = 0;
                let segmentMapping: { [key: string]: string } = {};
                for (const currentRoutePathToMatch of currentRoutedContent.route.split("/")) {
                  const currentSegment = segments[segmentIdx++];
                  if (!currentSegment) {
                    failedLookup = true;
                    break;
                  }
                  const currentSegmentPath = currentSegment;
                  if (currentRoutePathToMatch.indexOf(":") === 0) {
                    segmentMapping[currentRoutePathToMatch] = currentSegmentPath;
                  } else if (currentRoutePathToMatch !== currentSegmentPath) {
                    failedLookup = true;
                    break;
                  }
                }

                const newUrlSegments = {
                  ...this.urlSegments(),
                  ...segmentMapping,
                };
                if (!failedLookup) {
                  routedContentFound = true;
                  if (currentRoutedContent['@type'] === "routed-inline-content") {
                    const castedRouteContent = currentRoutedContent as KokuDto.ListViewRoutedContentDto;
                    if (castedRouteContent.inlineContent) {
                      let itemId = castedRouteContent.itemId;
                      if (itemId) {
                        for (const [segment, value] of Object.entries(segmentMapping || {})) {
                          itemId = itemId.replace(segment, value);
                        }
                      }

                      if (
                        this.inlineContent()?.id !== itemId
                        || this.inlineContent()?.content !== castedRouteContent.inlineContent
                        || !deepEqual(this.inlineContent()?.urlSegments, segmentMapping)
                      ) {
                        this.openInlineContent({
                          content: castedRouteContent.inlineContent,
                          id: itemId || null,
                          parentRoutePath: [
                            ...(this.parentRoutePath() + '/' + castedRouteContent.route).split('/').map(value => newUrlSegments[value] || value),
                          ].filter(value => value !== '').join('/'),
                          urlSegments: newUrlSegments
                        });
                      }
                    } else if (castedRouteContent.modalContent) {
                      let itemId = castedRouteContent.itemId;
                      if (itemId) {
                        for (const [segment, value] of Object.entries(newUrlSegments || {})) {
                          itemId = itemId.replace(segment, value);
                        }
                      }

                      if (
                        this.modalContent()?.id !== itemId
                        || this.modalContent()?.content !== castedRouteContent.modalContent
                        || !deepEqual(this.modalContent()?.urlSegments, newUrlSegments)
                      ) {
                        const oldRoutePath = [
                          ...(this.parentRoutePath()).split('/').map(value => newUrlSegments[value] || value),
                        ].filter(value => value !== '');
                        const newParentRoutePath = [
                          ...(this.parentRoutePath() + '/' + castedRouteContent.route).split('/').map(value => newUrlSegments[value] || value),
                        ].filter(value => value !== '').join('/');
                        const newModal = this.modalService.add({
                          dynamicContent: castedRouteContent.modalContent,
                          urlSegments: newUrlSegments,
                          dynamicContentSetup: this.contentSetup().modalRegistry,
                          fullscreen: true,
                          parentRoutePath: newParentRoutePath,
                          clickOutside: (event) => {
                            this.closeModalContent(oldRoutePath).subscribe();
                          },
                          onCloseRequested: () => {
                            this.closeModalContent(oldRoutePath).subscribe();
                          }
                        });

                        this.modalContent.set({
                          modal: newModal,
                          content: castedRouteContent.modalContent,
                          id: itemId || '',
                          urlSegments: newUrlSegments,
                          parentRoutePath: newParentRoutePath
                        });
                      }
                    }
                    break;
                  }
                }
              }
            }
            if (!routedContentFound) {
              this.closeInlineContent(true).subscribe();
            }

            let routedItemFound = false;
            for (const currentRoutedItem of ((this.listData() || {}).routedItems || [])) {
              if (currentRoutedItem.route) {
                let failedLookup = false;
                let segmentIdx = 0;
                let segmentMapping: { [key: string]: string } = {};
                for (const currentRoutePathToMatch of currentRoutedItem.route.split("/")) {
                  const currentSegment = segments[segmentIdx++];
                  if (!currentSegment) {
                    failedLookup = true;
                    break;
                  }
                  const currentSegmentPath = currentSegment;
                  if (currentRoutePathToMatch.indexOf(":") === 0) {
                    segmentMapping[currentRoutePathToMatch] = currentSegmentPath;
                  } else if (currentRoutePathToMatch !== currentSegmentPath) {
                    failedLookup = true;
                    break;
                  }
                }
                if (!failedLookup) {
                  routedItemFound = true;
                  if (currentRoutedItem['@type'] === "routed-item") {
                    const castedRouteContent = currentRoutedItem as KokuDto.ListViewRoutedDummyItemDto;
                    if (!castedRouteContent.text) {
                      throw new Error('Missing text property in RoutedItem Configuration');
                    }

                    this.showTempItem(castedRouteContent.text);
                    break;
                  }
                }
              }
            }
            if (!routedItemFound) {
              this.hideTempItem();
            }
          }
          this.routerUrlSubscription = this.router.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((evnt) => {
            if (evnt instanceof NavigationEnd) {
              afterNavigationUrlChange();
            }
          });
          afterNavigationUrlChange();

          this.sourceLoading.set(false);
        },
        error: (error) => {
          this.sourceLoading.set(false);
        }
      });
      return newQuery;
    } else {
      throw new Error('Missing listSource');
    }
  }

  itemAction(event: Event, result: ListItemSetup, action: ExtendedAbstractListViewItemActionDto) {
    switch (action['@type']) {
      case 'open-inline-content': {
        event.stopPropagation();
        const castedActionType = action as KokuDto.ListViewOpenInlineContentItemActionDto;
        if (castedActionType.inlineContent) {
          this.openInlineContent({
            content: castedActionType.inlineContent,
            id: result.id,
            urlSegments: null
          });
        }
        break;
      }
      case "http-call": {

        break;
      }
    }
  }

  itemClickAction(event: Event, item: ListItemSetup, action: KokuDto.AbstractListViewItemClickActionDto | undefined) {
    if (!this.sourceLoading()) {
      if (action) {
        switch (action['@type']) {
          case "propagate-global-event": {
            const castedActionType = action as KokuDto.ListViewItemClickPropagateGlobalEventActionDto;
            if (!castedActionType.eventName) {
              throw new Error('Missing eventName');
            }
            GLOBAL_EVENT_BUS.propagateGlobalEvent(castedActionType.eventName, item.source());
            break;
          }
          case "open-routed-content": {
            const castedActionType = action as KokuDto.ListViewItemClickOpenRoutedContentActionDto;
            let replacedRoute = castedActionType.route || '';
            for (const currentParam of castedActionType.params || []) {
              if (currentParam.param) {
                switch (currentParam['@type']) {
                  case "value": {
                    const castedParamType = currentParam as KokuDto.ListViewItemClickOpenRoutedContentActionItemValueParamDto;
                    if (castedParamType.valueReference) {
                      switch (castedParamType.valueReference['@type']) {
                        case "field-reference": {
                          const castedReference = castedParamType.valueReference as KokuDto.ListViewFieldReference;
                          if (!castedReference.fieldId) {
                            throw new Error('Missing fieldId in FieldReference');
                          }
                          const field = item.fields[castedReference.fieldId];
                          if (!field) {
                            throw new Error('FieldReference not resolvable');
                          }
                          replacedRoute = replacedRoute.replaceAll(currentParam.param, field.value());
                          break;
                        }
                        case "source-path-reference": {
                          const castedReference = castedParamType.valueReference as KokuDto.ListViewSourcePathReference;
                          if (!castedReference.valuePath) {
                            throw new Error('Missing valuePath in FieldReference');
                          }
                          replacedRoute = replacedRoute.replaceAll(currentParam.param, get(item.source(), castedReference.valuePath));
                          break;
                        }
                        default: {
                          throw new Error(`Unknown value reference type: ${castedParamType.valueReference['@type']}`);
                        }
                      }
                    } else {
                      throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                    }
                    break;
                  }
                  default:
                    throw new Error(`Unknown param Type: ${currentParam['@type']}`);
                }
              }
            }
            this.openRoutedContent(replacedRoute.split('/'));
            break;
          }
          case "open-inline-content": {
            const castedActionType = action as KokuDto.ListViewItemOpenInlineContentClickActionDto;
            if (castedActionType.inlineContent) {
              this.openInlineContent({
                content: castedActionType.inlineContent,
                id: item.id,
                urlSegments: null
              });
            }
            break;
          }
        }
        if (action['@type'] === 'open-inline-content') {

        } else if (action['@type'] === 'open-routed-content') {

        }
      }
    }
  }

  listAction($event: Event, action: KokuDto.AbstractListViewActionDto) {
    if (!this.sourceLoading()) {
      if (action) {
        if (action['@type'] === 'open-inline-content') {
          const castedActionType = action as KokuDto.ListViewOpenInlineContentActionDto;
          if (castedActionType.inlineContent) {
            this.openInlineContent({
              content: castedActionType.inlineContent,
              id: null,
              urlSegments: null
            });
          }
        } else if (action['@type'] === 'open-routed-content') {
          const castedActionType = action as KokuDto.ListViewOpenRoutedContentActionDto;
          if (castedActionType.route) {
            let replacedRoute = castedActionType.route;
            for (const [segment, value] of Object.entries(this.urlSegments() || {})) {
              replacedRoute = replacedRoute.replace(segment, value);
            }
            this.openRoutedContent(replacedRoute.split('/'));
          }
        }
      }
    }
  }

  closeInlineContent(skipRouteChange = false) {
    return new Observable<boolean>((observer) => {
      if (!skipRouteChange) {
        this.router.navigate(
          [
            ...this.parentRoutePath().split('/').map(value => (this.urlSegments() || {})[value] || value),
          ]
        ).then((success) => {
          if (success) {
            this.inlineContent.set(null);
          }
          observer.next(success);
          observer.complete();
        })
      } else {
        this.inlineContent.set(null);
        observer.next(true);
        observer.complete();
      }
    })
  }

  openRoutedContent(routes: string[]) {
    this.closeModalContent([
      ...this.parentRoutePath().split('/'),
      ...routes,
    ]).subscribe();
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
    const newItemInstance = this.prepareListItem(sourceDataSnapshot, item);
    listRegisterSnapshot.unshift(newItemInstance);
    this.listRegister.set(listRegisterSnapshot);
    return newItemInstance;
  }

  clearGlobalEventListeners() {
    GLOBAL_EVENT_BUS.removeGlobalEventListener(this.componentRef);
  }

  private showTempItem(text: string) {
    this.activeTempItem.set({
      text
    });
  }

  private hideTempItem() {
    this.activeTempItem.set(null);
  }

  private prepareListItem(
    listData: KokuDto.ListPage,
    listItem: KokuDto.ListItem
  ) {
    const currentItemId = listItem.id;
    if (currentItemId === undefined) {
      throw new Error('Unexpected item id');
    }
    const contentSetupSnapshot = this.contentSetup();
    const listDataSnapshot = this.listData();
    const itemIdPath = (listDataSnapshot || {}).itemIdPath;
    const currentResultListContentStates: ListItemSetup = {
      fieldSelection: listData.fieldSelection || [],
      fields: {},
      actions: ((listDataSnapshot || {}).itemActions || []).map(value => {
        return {
          ...value,
          loading: false
        }
      }),
      clickAction: (listDataSnapshot || {}).itemClickAction,
      preview: (listDataSnapshot || {}).itemPreview,
      id: currentItemId,
      source: signal({
        ...(listItem.values || {}),
        ...(itemIdPath ? {[itemIdPath]: currentItemId} : {})
      })
    };
    for (const currentFieldSelection of (listDataSnapshot || {}).fields || []) {
      if (currentFieldSelection.fieldDefinition) {
        const currentFieldTypeSetup = contentSetupSnapshot.fieldRegistry[currentFieldSelection.fieldDefinition['@type']];
        if (currentFieldTypeSetup && currentFieldSelection.id) {
          const defaultValue = currentFieldSelection.fieldDefinition?.defaultValue || null;
          let currentFieldValue = defaultValue;
          if (currentFieldSelection.valuePath) {
            currentFieldValue = get({
              ...(listItem.values || {})
            }, currentFieldSelection.valuePath, defaultValue);
          }
          currentResultListContentStates.fields[currentFieldSelection.id] = currentFieldTypeSetup.stateInitializer(currentFieldSelection.fieldDefinition, currentFieldValue)
        }
      }
    }
    return currentResultListContentStates;
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
        }
        if (newRoute) {
          this.router.navigate(newRoute).then((success) => {
            afterParentRouteOpened(success);
          });
        } else {
          afterParentRouteOpened(true);
        }
      } else {
        this.router.navigate(newRoute).then((success) => {
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
      page: this.currentPage()
    });
  }

  getItemClasses(source: {
    [p: string]: any
  }, globalItemStyling: KokuDto.AbstractListViewGlobalItemStylingDto[] | undefined) {
    let result: string[] = [];
    for (const currentItemStyling of globalItemStyling || []) {
      const iconStylingRegister = this.contentSetup().itemStylingRegistry[currentItemStyling['@type']];
      if (iconStylingRegister && iconStylingRegister.itemClasses) {
        result = [...result, ...iconStylingRegister.itemClasses(currentItemStyling, source)];
      }
    }
    return result;
  }

}
