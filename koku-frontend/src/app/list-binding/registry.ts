import { computed } from '@angular/core';
import {
  ItemStylingSetup,
  ListContentSetup,
  ListFieldRenderContext,
  ListFilterRenderContext,
  ListInlineContentRenderContext,
  ListItemActionRenderContext,
  ListItemSetup,
  ListPreviewRenderContext,
} from '../list/list.component';
import { FORMULAR_CONTENT_REGISTRY } from '../formular-binding/registry';
import { get } from '../utils/get';
import { ToastTypeUnion } from '../toast/toast.service';
import { set } from '../utils/set';
import { CHART_FILTER_REGISTRY } from '../chart-binding/registry';
import { ModalContentRenderContext, ModalContentSetup } from '../modal/modal.type';
import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { ToggleFilterTriState } from './filters/toggle/toggle-filter.component';
import { DynamicOutputs } from '../dynamic-host/dynamic-host.directive';
import {
  replaceRouteSegments as replaceSegments,
  resolveRouteBasedFieldOverrides as routeBasedFieldOverrides,
} from '../utils/route.utils';
import { colorBackgroundClasses } from '../utils/color.utils';
type FieldRegistryItem = NonNullable<ListContentSetup['fieldRegistry'][string]>;
type PreviewRegistryItem = NonNullable<ListContentSetup['previewRegistry'][string]>;
type InlineContentRegistryItem = NonNullable<ListContentSetup['inlineContentRegistry'][string]>;
type ActionRegistryItem = NonNullable<ListContentSetup['actionRegistry'][string]>;
type FilterRegistryItem = NonNullable<ListContentSetup['filterRegistry'][string]>;
const ROUNDED_MAPPING: Partial<Record<KokuDto.KokuRoundedEnum, string>> = {
  SM: 'rounded-sm',
  MD: 'rounded-md',
  LG: 'rounded-lg',
  XL: 'rounded-xl',
  XL2: 'rounded-2xl',
  XL3: 'rounded-3xl',
  XL4: 'rounded-4xl',
};
const listFieldClasses = (listContent: KokuDto.AbstractListViewFieldDto<any>) => {
  const styledListContent = listContent as KokuDto.AbstractListViewFieldDto<any> & {
    rounded?: KokuDto.KokuRoundedEnum;
    backgroundColor?: KokuDto.KokuColorEnum;
  };
  const rounded = styledListContent.rounded;
  const backgroundColor = styledListContent.backgroundColor;
  return [rounded && 'p-1 my-1', rounded && ROUNDED_MAPPING[rounded], colorBackgroundClasses(backgroundColor)]
    .filter(Boolean)
    .join(' ');
};
const modalCloseOutputs = (context: ModalContentRenderContext): DynamicOutputs => ({
  closeRequested: () => {
    if (context.modal.onCloseRequested) {
      context.modal.onCloseRequested();
    } else {
      context.modal.close();
    }
  },
});
const documentSubmittedOutputs = (content: () => KokuDto.ListViewDocumentFormContentDto): DynamicOutputs => ({
  submitted: (payload: any) => {
    for (const currentOnSubmitEvent of content().onSubmitEvents || []) {
      switch (currentOnSubmitEvent['@type']) {
        case 'propagate-global-event': {
          const castedEventJob =
            currentOnSubmitEvent as KokuDto.ListViewInlineFormularContentAfterSavePropagateGlobalEventDto;
          if (!castedEventJob.eventName) {
            throw new Error(`Missing eventName in saveEvent`);
          }
          GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEventJob.eventName, payload);
          break;
        }
        default: {
          throw new Error(`Unknown onSubmitEvent type ${currentOnSubmitEvent['@type']}`);
        }
      }
    }
  },
});
const FIELD_REGISTRY: Partial<Record<KokuDto.AbstractListViewFieldDto<any>['@type'], FieldRegistryItem>> = {
  'picture-upload': (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/picture-upload/picture-upload.component').then((module) => module.PictureUploadComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
    },
  }),
  input: (context: ListFieldRenderContext) => ({
    loadComponent: () => import('../fields/input/input-field.component').then((module) => module.InputFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      type: (context.config() as KokuDto.ListViewInputFieldDto).type,
      valueOnly: true,
      cls: listFieldClasses(context.config()),
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
  'date-input': (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/input/date-input-field.component').then((module) => module.DateInputFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      valueOnly: true,
      cls: listFieldClasses(context.config()),
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
  'time-input': (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/input/time-input-field.component').then((module) => module.TimeInputFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      valueOnly: true,
      cls: listFieldClasses(context.config()),
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
  'month-input': (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/input/month-input-field.component').then((module) => module.MonthInputFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      valueOnly: true,
      cls: listFieldClasses(context.config()),
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
  'week-input': (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/input/week-input-field.component').then((module) => module.WeekInputFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      valueOnly: true,
      cls: listFieldClasses(context.config()),
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
  textarea: (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/textarea/textarea-field.component').then((module) => module.TextareaFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      valueOnly: true,
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
  checkbox: (context: ListFieldRenderContext) => ({
    loadComponent: () =>
      import('../fields/checkbox/checkbox-field.component').then((module) => module.CheckboxFieldComponent),
    inputs: computed(() => ({
      name: context.id,
      value: context.fieldState.value(),
      valueOnly: true,
    })),
    outputs: {
      changed: (data: any) => context.emit('onChange', data),
      typed: (data: any) => context.emit('onInput', data),
      blurred: (data: any) => context.emit('onBlur', data),
      focused: (data: any) => context.emit('onFocus', data),
    },
  }),
};
const PREVIEW_REGISTRY: Partial<Record<KokuDto.AbstractListViewItemPreviewDto['@type'], PreviewRegistryItem>> = {
  avatar: (context: ListPreviewRenderContext) => ({
    loadComponent: () => import('../avatar/avatar.component').then((module) => module.AvatarComponent),
    inputs: computed(() => ({
      value: context.value(),
    })),
  }),
  text: (context: ListPreviewRenderContext) => ({
    loadComponent: () => import('../text-circle/text-circle.component').then((module) => module.TextCircleComponent),
    inputs: computed(() => ({
      value: context.value(),
    })),
  }),
};
const INLINE_CONTENT_REGISTRY: Partial<
  Record<KokuDto.AbstractListViewContentDto['@type'] | string, InlineContentRegistryItem>
> = {
  formular: (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewFormularContentDto);
    const sourceUrl = computed(() => replaceSegments(content().sourceUrl, context.urlSegments()));
    return {
      loadComponent: () =>
        import('./containers/formular-container/list-formular-container.component').then(
          (module) => module.ListFormularContainerComponent,
        ),
      inputs: computed(() => ({
        formularUrl: replaceSegments(content().formularUrl, context.urlSegments()),
        sourceUrl: sourceUrl(),
        urlSegments: context.urlSegments(),
        submitUrl: content().submitUrl || sourceUrl(),
        maxWidth: content().maxWidthInPx !== undefined ? content().maxWidthInPx + 'px' : undefined,
        submitMethod: content().submitMethod,
        onSaveEvents: content().onSaveEvents,
        contentRegistry: FORMULAR_CONTENT_REGISTRY,
        fieldOverrides: routeBasedFieldOverrides(content().fieldOverrides, context.urlSegments()),
        buttonDockOutlet: context.buttonDockOutlet(),
        context: context.context(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  'document-form': (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewDocumentFormContentDto);
    return {
      loadComponent: () =>
        import('../fields/document/document-form/document-form-field.component').then(
          (module) => module.DocumentFormFieldComponent,
        ),
      inputs: computed(() => ({
        documentUrl: replaceSegments(content().documentUrl, context.urlSegments()),
        submitUrl: replaceSegments(content().submitUrl, context.urlSegments()),
        buttonDockOutlet: context.buttonDockOutlet(),
        context: context.context(),
      })),
      outputs: documentSubmittedOutputs(content),
    };
  },
  'file-viewer': (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewFileViewerContentDto);
    return {
      loadComponent: () =>
        import('../fields/file-viewer/file-viewer.component').then((module) => module.FileViewerComponent),
      inputs: computed(() => ({
        sourceUrl: replaceSegments(content().sourceUrl, context.urlSegments()),
        fileUrl: replaceSegments(content().fileUrl, context.urlSegments()),
        mimeTypeSourcePath: content().mimeTypeSourcePath,
        buttonDockOutlet: context.buttonDockOutlet(),
      })),
    };
  },
  dock: (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewDockContentDto);
    return {
      loadComponent: () =>
        import('./containers/dock-container/list-dock-container.component').then(
          (module) => module.ListDockContainerComponent,
        ),
      inputs: computed(() => ({
        content: content().content,
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: context.urlSegments(),
        buttonDockOutlet: context.buttonDockOutlet(),
        parentRoutePath: context.parentRoutePath(),
        context: context.context(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  header: (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewHeaderContentDto);
    const segmentMapping = computed(() => ({ ...(context.urlSegments() || {}) }));
    return {
      loadComponent: () =>
        import('./containers/header-container/list-header-container.component').then(
          (module) => module.ListHeaderContainerComponent,
        ),
      inputs: computed(() => ({
        content: content(),
        sourceUrl: (() => {
          if (!content().sourceUrl) {
            return undefined;
          }
          return content()
            .sourceUrl!.split('/')
            .map((part) => (part.indexOf(':') === 0 ? segmentMapping()[part] : part))
            .join('/');
        })(),
        titlePath: content().titlePath,
        title: content().title,
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: segmentMapping(),
        parentRoutePath: context.parentRoutePath(),
        context: context.context(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  grid: (context: ListInlineContentRenderContext) => ({
    loadComponent: () =>
      import('./containers/grid-container/list-grid-container.component').then(
        (module) => module.ListGridContainerComponent,
      ),
    inputs: computed(() => ({
      content: context.content(),
      contentSetup: LIST_CONTENT_SETUP,
      urlSegments: context.urlSegments(),
      parentRoutePath: context.parentRoutePath(),
    })),
    outputs: {
      closeRequested: () => context.close(),
      openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
    },
  }),
  chart: (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewChartContentDto);
    return {
      loadComponent: () =>
        import('./containers/chart-container/list-chart-container.component').then(
          (module) => module.ListChartContainerComponent,
        ),
      inputs: computed(() => ({
        chartUrl: replaceSegments(content().chartUrl, context.urlSegments()),
        urlSegments: context.urlSegments(),
        filterRegistry: CHART_FILTER_REGISTRY,
      })),
    };
  },
  barcode: (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewBarcodeContentDto);
    return {
      loadComponent: () =>
        import('../fields/barcode-capture/barcode-capture.component').then((module) => module.BarcodeCaptureComponent),
      outputs: {
        afterCapture: (capturedValue: string) => {
          for (const currentEvent of content().onCaptureEvents || []) {
            switch (currentEvent['@type']) {
              case 'propagate-global-event': {
                const castedEvent =
                  currentEvent as KokuDto.ListViewBarcodeContentDtoAfterCapturePropagateGlobalEventDto;
                if (!castedEvent.eventName) {
                  throw new Error(`Missing eventName`);
                }
                GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, capturedValue);
                break;
              }
            }
          }
          context.close();
        },
      },
    };
  },
  list: (context: ListInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.ListViewListContentDto);
    return {
      loadComponent: () =>
        import('./containers/list-container/list-inline-list-container.component').then(
          (module) => module.ListInlineListContainerComponent,
        ),
      inputs: computed(() => ({
        title: 'Termine',
        listUrl: replaceSegments(content().listUrl, context.urlSegments()),
        sourceUrl: replaceSegments(content().sourceUrl, context.urlSegments()),
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: context.urlSegments(),
        parentRoutePath: context.parentRoutePath(),
        contextMapping: content().context,
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
};
const MODAL_REGISTRY: ModalContentSetup = {
  formular: (context: ModalContentRenderContext<KokuDto.ListViewFormularContentDto>) => {
    const sourceUrl = replaceSegments(context.content().sourceUrl, context.modal.urlSegments);
    return {
      loadComponent: () =>
        import('./containers/formular-container/list-formular-container.component').then(
          (module) => module.ListFormularContainerComponent,
        ),
      inputs: computed(() => ({
        formularUrl: replaceSegments(context.content().formularUrl, context.modal.urlSegments),
        sourceUrl,
        urlSegments: context.modal.urlSegments,
        submitUrl: context.content().submitUrl || sourceUrl,
        maxWidth: context.content().maxWidthInPx !== undefined ? context.content().maxWidthInPx + 'px' : undefined,
        submitMethod: context.content().submitMethod,
        onSaveEvents: context.content().onSaveEvents,
        contentRegistry: FORMULAR_CONTENT_REGISTRY,
        fieldOverrides: routeBasedFieldOverrides(context.content().fieldOverrides, context.modal.urlSegments),
        context: context.modal.urlSegments,
      })),
      outputs: modalCloseOutputs(context),
    };
  },
  dock: (context: ModalContentRenderContext<KokuDto.ListViewDockContentDto>) => ({
    loadComponent: () =>
      import('./containers/dock-container/list-dock-container.component').then(
        (module) => module.ListDockContainerComponent,
      ),
    inputs: computed(() => ({
      content: context.content().content,
      contentSetup: LIST_CONTENT_SETUP,
      urlSegments: context.modal.urlSegments,
      parentRoutePath: context.modal.parentRoutePath,
    })),
    outputs: modalCloseOutputs(context),
  }),
  header: (context: ModalContentRenderContext<KokuDto.ListViewHeaderContentDto>) => {
    const segmentMapping: Record<string, string> = { ...(context.modal.urlSegments || {}) };
    return {
      loadComponent: () =>
        import('./containers/header-container/list-header-container.component').then(
          (module) => module.ListHeaderContainerComponent,
        ),
      inputs: computed(() => ({
        content: context.content(),
        sourceUrl: context.content().sourceUrl
          ? context
              .content()
              .sourceUrl!.split('/')
              .map((part) => (part.indexOf(':') === 0 ? segmentMapping[part] : part))
              .join('/')
          : undefined,
        titlePath: context.content().titlePath,
        title: context.content().title,
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: context.modal.urlSegments,
        parentRoutePath: context.modal.parentRoutePath,
      })),
      outputs: modalCloseOutputs(context),
    };
  },
  grid: (context: ModalContentRenderContext<KokuDto.ListViewGridContentDto>) => ({
    loadComponent: () =>
      import('./containers/grid-container/list-grid-container.component').then(
        (module) => module.ListGridContainerComponent,
      ),
    inputs: computed(() => ({
      content: context.content(),
      contentSetup: LIST_CONTENT_SETUP,
      urlSegments: context.modal.urlSegments,
      parentRoutePath: context.modal.parentRoutePath,
    })),
    outputs: modalCloseOutputs(context),
  }),
  chart: (context: ModalContentRenderContext<KokuDto.ListViewChartContentDto>) => ({
    loadComponent: () =>
      import('./containers/chart-container/list-chart-container.component').then(
        (module) => module.ListChartContainerComponent,
      ),
    inputs: computed(() => ({
      chartUrl: replaceSegments(context.content().chartUrl, context.modal.urlSegments),
      urlSegments: context.modal.urlSegments,
      filterRegistry: CHART_FILTER_REGISTRY,
    })),
  }),
  list: (context: ModalContentRenderContext<KokuDto.ListViewListContentDto>) => ({
    loadComponent: () =>
      import('./containers/list-container/list-inline-list-container.component').then(
        (module) => module.ListInlineListContainerComponent,
      ),
    inputs: computed(() => ({
      title: 'Termine',
      listUrl: replaceSegments(context.content().listUrl, context.modal.urlSegments),
      sourceUrl: replaceSegments(context.content().sourceUrl, context.modal.urlSegments),
      contentSetup: LIST_CONTENT_SETUP,
      urlSegments: context.modal.urlSegments,
      parentRoutePath: context.modal.parentRoutePath,
      contextMapping: context.content().context,
    })),
    outputs: modalCloseOutputs(context),
  }),
};
const executeHttpActionEvents = (
  instance: ListItemActionRenderContext['parent'],
  events: KokuDto.AbstractListViewActionEventDto[],
  eventPayload: Record<string, any> = {},
) => {
  for (const currentEvent of events || []) {
    switch (currentEvent['@type']) {
      case 'reload': {
        instance.reloadRequested.emit();
        break;
      }
      case 'event-payload-update': {
        const castedEvent = currentEvent as KokuDto.ListViewEventPayloadUpdateActionEventDto;
        if (!castedEvent.idPath) {
          throw new Error('Missing id in event payload update');
        }
        const listRegisterIdx: Record<string, ListItemSetup> = {};
        for (const currentEntry of instance.listRegister()) {
          if (currentEntry.id !== undefined && currentEntry.id !== null) {
            listRegisterIdx[currentEntry.id] = currentEntry;
          }
        }
        const item = listRegisterIdx[String(get(eventPayload, castedEvent.idPath, ''))];
        if (item !== undefined) {
          for (const [currentMappingPath, listViewReference] of Object.entries(castedEvent.valueMapping || {})) {
            const mappablePayloadValue = get(eventPayload, currentMappingPath);
            if (mappablePayloadValue !== undefined) {
              switch (listViewReference['@type']) {
                case 'field-reference': {
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
                case 'source-path-reference': {
                  const castedReference = listViewReference as KokuDto.ListViewSourcePathReference;
                  if (!castedReference.valuePath) {
                    throw new Error('Missing valuePath in FieldReference');
                  }
                  const itemSourceSnapshot = { ...item.source() };
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
      case 'notification': {
        const castedEvent = currentEvent as KokuDto.ListViewNotificationEvent;
        if (!castedEvent.text) {
          throw new Error('Missing text in Notification');
        }
        let notificationText = castedEvent.text;
        for (const currentParam of castedEvent.params || []) {
          if (!currentParam.param) {
            throw new Error(`Missing param`);
          }
          const value = resolveListActionParamValue(instance, currentParam);
          notificationText = notificationText.replaceAll(currentParam.param, value);
        }
        let serenity: ToastTypeUnion = 'info';
        if (castedEvent.serenity) {
          switch (castedEvent.serenity) {
            case 'SUCCESS':
              serenity = 'success';
              break;
            case 'ERROR':
              serenity = 'error';
              break;
            default:
              throw new Error(`Unknown Notification serenity ${serenity}`);
          }
        }
        instance.toastService.add(notificationText, serenity);
        break;
      }
      case 'propagate-global-event': {
        const castedEvent = currentEvent as KokuDto.ListViewPropagateGlobalEventActionEventDto;
        if (!castedEvent.eventName) {
          throw new Error('Missing eventName');
        }
        GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, eventPayload);
        break;
      }
      default: {
        throw new Error(`Unknown event type ${currentEvent}`);
      }
    }
  }
};
const resolveListActionParamValue = (
  instance: ListItemActionRenderContext['parent'],
  currentParam:
    | KokuDto.ListViewCallHttpListValueActionParamDto
    | KokuDto.ListViewNotificationEventDateValueParamDto
    | KokuDto.ListViewUserConfirmationDateValueParamDto
    | KokuDto.ListViewUserConfirmationValueParamDto,
) => {
  if (!currentParam.valueReference) {
    throw new Error(`Missing valueReference for param: ${currentParam.param}`);
  }
  switch (currentParam.valueReference['@type']) {
    case 'field-reference': {
      const castedReference = currentParam.valueReference as KokuDto.ListViewFieldReference;
      if (!castedReference.fieldId) {
        throw new Error('Missing fieldId in FieldReference');
      }
      const field = instance.register().fields[castedReference.fieldId];
      if (!field) {
        throw new Error('FieldReference not resolvable');
      }
      return field.value();
    }
    case 'source-path-reference': {
      const castedReference = currentParam.valueReference as KokuDto.ListViewSourcePathReference;
      if (!castedReference.valuePath) {
        throw new Error('Missing valuePath in FieldReference');
      }
      return get(instance.register().source(), castedReference.valuePath);
    }
    default: {
      throw new Error(`Unknown value reference type: ${currentParam.valueReference['@type']}`);
    }
  }
};
const resolvedHttpUrl = (
  instance: ListItemActionRenderContext['parent'],
  action: KokuDto.ListViewCallHttpListItemActionDto,
) => {
  if (!action.method || !action.url) {
    throw new Error('http-call configuration is missing url and/or method');
  }
  let url = action.url;
  for (const currentParam of action.params || []) {
    if (currentParam.param) {
      url = url.replaceAll(currentParam.param, resolveListActionParamValue(instance, currentParam as any));
    }
  }
  return url;
};
const ACTION_REGISTRY: Partial<Record<KokuDto.AbstractListViewItemActionDto['@type'] | string, ActionRegistryItem>> = {
  'http-call': (context: ListItemActionRenderContext) => ({
    loadComponent: () =>
      import('./actions/button-action/button-action.component').then((module) => module.ButtonActionComponent),
    inputs: computed(() => ({
      value: context.action(),
      register: context.register(),
      contentSetup: context.contentSetup(),
      loading: Boolean((context.action() as any).loading),
    })),
    outputs: {
      clicked: (event: MouseEvent) => {
        event.stopPropagation();
        const instance = context.parent;
        const action = context.action() as KokuDto.ListViewCallHttpListItemActionDto;
        const callAction = () =>
          instance.httpClient.request<Record<string, any>>(action.method!, resolvedHttpUrl(instance, action));
        if (action.userConfirmation) {
          let headline = action.userConfirmation.headline || '';
          let content = action.userConfirmation.content || '';
          for (const currentParam of action.userConfirmation.params || []) {
            if (!currentParam.param) {
              throw new Error(`Missing param`);
            }
            const value = resolveListActionParamValue(instance, currentParam as any);
            headline = headline.replaceAll(currentParam.param, value);
            content = content.replaceAll(currentParam.param, value);
          }
          const confirmationModal = instance.modalService.add({
            headline,
            content,
            buttons: [
              {
                text: 'Abbrechen',
                styles: ['OUTLINE'],
                onClick: () => confirmationModal.close(),
              },
              {
                text: 'Bestätigen',
                onClick: (_event, modal, button) => {
                  button.loading = true;
                  button.disabled = true;
                  callAction().subscribe({
                    next: (rawValue) => {
                      executeHttpActionEvents(instance, action.successEvents || [], rawValue);
                      button.loading = false;
                      button.disabled = false;
                      confirmationModal.close();
                    },
                    error: () => {
                      executeHttpActionEvents(instance, action.failEvents || []);
                      button.loading = false;
                      button.disabled = false;
                      confirmationModal.update(modal);
                    },
                  });
                },
              },
            ],
            clickOutside: () => confirmationModal.close(),
          });
        } else {
          callAction().subscribe({
            next: (rawValue) => executeHttpActionEvents(instance, action.successEvents || [], rawValue),
            error: () => executeHttpActionEvents(instance, action.failEvents || []),
          });
        }
      },
    },
  }),
  condition: (context: ListItemActionRenderContext) => ({
    loadComponent: () =>
      import('./actions/condition-action/condition-action.component').then((module) => module.ConditionActionComponent),
    inputs: computed(() => ({
      value: context.action(),
      register: context.register(),
      parent: context.parent,
    })),
  }),
  'open-inline-content': (context: ListItemActionRenderContext) => ({
    loadComponent: () =>
      import('./actions/button-action/button-action.component').then((module) => module.ButtonActionComponent),
    inputs: computed(() => ({
      value: context.action(),
      register: context.register(),
      contentSetup: context.contentSetup(),
    })),
    outputs: {
      clicked: (event: MouseEvent) => {
        event.stopPropagation();
        const action = context.action() as KokuDto.ListViewOpenInlineContentItemActionDto;
        if (action.inlineContent) {
          context.parent.openInlineContentRequested.emit({
            content: action.inlineContent,
            id: context.register().id,
            urlSegments: null,
          });
        }
      },
    },
  }),
  'open-routed-content': (context: ListItemActionRenderContext) => ({
    loadComponent: () =>
      import('./actions/button-action/button-action.component').then((module) => module.ButtonActionComponent),
    inputs: computed(() => ({
      value: context.action(),
      register: context.register(),
      contentSetup: context.contentSetup(),
    })),
    outputs: {
      clicked: (event: MouseEvent) => {
        event.stopPropagation();
        const action = context.action() as KokuDto.ListViewItemActionOpenRoutedContentActionDto;
        if (action.route) {
          let route = action.route;
          for (const currentParam of action.params || []) {
            if (currentParam.param) {
              route = route.replaceAll(
                currentParam.param,
                resolveListActionParamValue(context.parent, currentParam as any),
              );
            }
          }
          context.parent.openRoutedContentRequested.emit(route.split('/'));
        }
      },
    },
  }),
};
const STYLING_REGISTRY: ItemStylingSetup = {
  condition: {
    itemClasses(
      stylingDefinition: KokuDto.ListViewConditionalItemValueStylingDto,
      source: Record<string, any>,
    ): string[] {
      let matchesCondition = false;
      const currentValue = get(source, stylingDefinition.compareValuePath || '', null);
      for (const currentPositiveCompareValue of stylingDefinition.expectedValues || []) {
        if (currentValue == currentPositiveCompareValue) {
          matchesCondition = true;
          break;
        }
      }
      const styling = matchesCondition ? stylingDefinition.positiveStyling : stylingDefinition.negativeStyling;
      const result: string[] = [];
      if (styling?.opacity) {
        result.push(`opacity-${styling.opacity}`);
      }
      if (styling?.lineThrough) {
        result.push(`line-through`);
      }
      return result;
    },
  },
};
const FILTER_REGISTRY: Readonly<Record<string, FilterRegistryItem | undefined>> = {
  toggle: {
    initialPredicates: (filter: KokuDto.ListViewToggleFilterDto) => {
      switch (filter.defaultState) {
        case 'DISABLED':
          return filter.disabledPredicates || [];
        case 'NEUTRAL':
          return filter.neutralPredicates || [];
        case 'ENABLED':
          return filter.enabledPredicates || [];
        default:
          return [];
      }
    },
    createRecipe: (context: ListFilterRenderContext) => ({
      loadComponent: () =>
        import('./filters/toggle/toggle-filter.component').then((module) => module.ToggleFilterComponent),
      inputs: computed(() => ({
        label: (context.filterDefinition() as KokuDto.ListViewToggleFilterDto).label,
      })),
      outputs: {
        filterChanged: (state: ToggleFilterTriState) => {
          const filter = context.filterDefinition() as KokuDto.ListViewToggleFilterDto;
          switch (state) {
            case 'checked':
              context.emit(filter.enabledPredicates || []);
              break;
            case 'unchecked':
              context.emit(filter.disabledPredicates || []);
              break;
            case 'indeterminate':
              context.emit(filter.neutralPredicates || []);
              break;
          }
        },
      },
    }),
  },
};
export const LIST_CONTENT_SETUP: ListContentSetup = {
  fieldRegistry: FIELD_REGISTRY,
  previewRegistry: PREVIEW_REGISTRY,
  inlineContentRegistry: INLINE_CONTENT_REGISTRY,
  actionRegistry: ACTION_REGISTRY,
  modalRegistry: MODAL_REGISTRY,
  itemStylingRegistry: STYLING_REGISTRY,
  filterRegistry: FILTER_REGISTRY,
};
