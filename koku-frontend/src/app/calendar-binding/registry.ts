import { computed } from '@angular/core';
import { FormularSourceOverride } from '../formular/formular.component';
import {
  CalendarActionRenderContext,
  CalendarContentSetup,
  CalendarContext,
  CalendarInlineContentRenderContext,
} from '../calendar/calendar.component';
import { LIST_CONTENT_SETUP } from '../list-binding/registry';
import { ModalContentRenderContext, ModalContentSetup } from '../modal/modal.type';
import { DynamicOutputs } from '../dynamic-host/dynamic-host.directive';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import {
  replaceRouteSegments as replaceSegments,
  resolveRouteBasedContentOverrides as routeBasedContentOverrides,
  resolveRoutePath as mapSourceUrl,
} from '../utils/route.utils';
dayjs.extend(customParseFormat);
type InlineContentRegistryItem = NonNullable<CalendarContentSetup['inlineContentRegistry'][string]>;
type ActionRegistryItem = NonNullable<CalendarContentSetup['actionRegistry'][string]>;
type ModalContentRegistryItem = NonNullable<ModalContentSetup[string]>;
const modifyOffset = (
  date: Date,
  offsetUnit?: KokuDto.CalendarFormularContextSourceOffsetUnitEnumDto,
  offsetValue?: number,
) => {
  if (offsetValue === undefined || !offsetUnit) {
    return date;
  }
  switch (offsetUnit) {
    case 'SECOND':
      return dayjs(date).add(offsetValue, 'seconds').toDate();
    case 'MINUTE':
      return dayjs(date).add(offsetValue, 'minutes').toDate();
    case 'HOUR':
      return dayjs(date).add(offsetValue, 'hours').toDate();
    case 'DAY':
      return dayjs(date).add(offsetValue, 'days').toDate();
    case 'WEEK':
      return dayjs(date).add(offsetValue, 'weeks').toDate();
    case 'MONTH':
      return dayjs(date).add(offsetValue, 'months').toDate();
    case 'YEAR':
      return dayjs(date).add(offsetValue, 'years').toDate();
    default:
      throw new Error(`Unknown offset type ${offsetUnit}`);
  }
};
const parseCalendarSourceValue = (
  value: KokuDto.CalendarFormularContextSourceValueEnumDto | undefined,
  queryParams: CalendarContext,
) => {
  switch (value) {
    case 'SELECTION_START_DATE':
      return { rawValue: queryParams.selectionStartDate, inputFormat: 'YYYY-MM-DD', outputFormat: 'YYYY-MM-DD' };
    case 'SELECTION_END_DATE':
      return { rawValue: queryParams.selectionEndDate, inputFormat: 'YYYY-MM-DD', outputFormat: 'YYYY-MM-DD' };
    case 'SELECTION_START_TIME':
      return { rawValue: queryParams.selectionStartTime, inputFormat: 'HH:mm', outputFormat: 'HH:mm' };
    case 'SELECTION_END_TIME':
      return { rawValue: queryParams.selectionEndTime, inputFormat: 'HH:mm', outputFormat: 'HH:mm' };
    case 'SELECTION_START_DATETIME':
      return {
        rawValue: queryParams.selectionStartDateTime,
        inputFormat: 'YYYY-MM-DDTHH:mm',
        outputFormat: 'YYYY-MM-DDTHH:mm',
      };
    case 'SELECTION_END_DATETIME':
      return {
        rawValue: queryParams.selectionEndDateTime,
        inputFormat: 'YYYY-MM-DDTHH:mm',
        outputFormat: 'YYYY-MM-DDTHH:mm',
      };
    default:
      return undefined;
  }
};
const sourceOverridesFromQueryParams = (
  sourceOverrides: KokuDto.CalendarFormularSourceOverrideDto[] | undefined,
  queryParamsRaw: Record<string, any>,
) => {
  const result: FormularSourceOverride[] = [];
  const queryParams = queryParamsRaw as CalendarContext;
  for (const currentSourceOverride of sourceOverrides || []) {
    if (currentSourceOverride.sourcePath === undefined || currentSourceOverride.value === undefined) {
      continue;
    }
    const sourceValue = parseCalendarSourceValue(currentSourceOverride.value, queryParams);
    let newFieldValue;
    if (sourceValue?.rawValue) {
      let parsedDateTmp = dayjs(sourceValue.rawValue, sourceValue.inputFormat);
      if (!parsedDateTmp.isValid()) {
        parsedDateTmp = dayjs(Date.now());
      }
      const parsedDate = parsedDateTmp.toDate();
      if (parsedDate) {
        newFieldValue = dayjs(
          modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
        ).format(sourceValue.outputFormat);
      }
    }
    result.push({
      path: currentSourceOverride.sourcePath,
      value: newFieldValue,
    });
  }
  return result;
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
const MODAL_CONTENT_REGISTRY: Partial<Record<string, ModalContentRegistryItem>> = {
  formular: (context: ModalContentRenderContext<KokuDto.CalendarFormularInlineContentDto>) => {
    const content = computed(() => context.content());
    const sourceUrl = computed(() => replaceSegments(content().sourceUrl, context.modal.urlSegments));
    return {
      loadComponent: () =>
        import('./formular-container/calendar-inline-formular-container.component').then(
          (module) => module.CalendarInlineFormularContainerComponent,
        ),
      inputs: computed(() => ({
        formularUrl: replaceSegments(content().formularUrl, context.modal.urlSegments),
        sourceUrl: sourceUrl(),
        submitUrl: content().submitUrl || sourceUrl(),
        maxWidth: content().maxWidthInPx !== undefined ? content().maxWidthInPx + 'px' : undefined,
        submitMethod: content().submitMethod,
        onSaveEvents: content().onSaveEvents,
        contentOverrides: routeBasedContentOverrides(content().contentOverrides, context.modal.urlSegments),
      })),
      outputs: modalCloseOutputs(context),
    };
  },
  dock: (context: ModalContentRenderContext<KokuDto.CalendarDockInlineContentDto>) => {
    const content = computed(() => context.content());
    return {
      loadComponent: () =>
        import('./dock-container/calendar-inline-dock-container.component').then(
          (module) => module.CalendarInlineDockContainerComponent,
        ),
      inputs: computed(() => ({
        content: content().content,
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: context.modal.urlSegments || null,
        parentRoutePath: context.modal.parentRoutePath || '',
      })),
      outputs: modalCloseOutputs(context),
    };
  },
  header: (context: ModalContentRenderContext<KokuDto.CalendarHeaderInlineContentDto>) => {
    const content = computed(() => context.content());
    return {
      loadComponent: () =>
        import('./header-container/calendar-inline-header-container.component').then(
          (module) => module.CalendarInlineHeaderContainerComponent,
        ),
      inputs: computed(() => ({
        content: content(),
        sourceUrl: mapSourceUrl(content().sourceUrl, context.modal.urlSegments),
        titlePath: content().titlePath,
        title: content().title,
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: context.modal.urlSegments || null,
        parentRoutePath: context.modal.parentRoutePath || '',
      })),
      outputs: modalCloseOutputs(context),
    };
  },
  list: (context: ModalContentRenderContext<KokuDto.CalendarListInlineContentDto>) => {
    const content = computed(() => context.content());
    return {
      loadComponent: () =>
        import('./list-container/calendar-inline-list-container.component').then(
          (module) => module.CalendarInlineListContainerComponent,
        ),
      inputs: computed(() => ({
        title: 'Termine',
        listUrl: replaceSegments(content().listUrl, context.modal.urlSegments),
        sourceUrl: replaceSegments(content().sourceUrl, context.modal.urlSegments),
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: context.modal.urlSegments || null,
        parentRoutePath: context.modal.parentRoutePath || '',
      })),
      outputs: modalCloseOutputs(context),
    };
  },
};
const INLINE_CONTENT_REGISTRY: Partial<Record<string, InlineContentRegistryItem>> = {
  formular: (context: CalendarInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.CalendarFormularInlineContentDto);
    const sourceUrl = computed(() => replaceSegments(content().sourceUrl, context.urlSegments()));
    return {
      loadComponent: () =>
        import('./formular-container/calendar-inline-formular-container.component').then(
          (module) => module.CalendarInlineFormularContainerComponent,
        ),
      inputs: computed(() => ({
        formularUrl: replaceSegments(content().formularUrl, context.urlSegments()),
        sourceUrl: sourceUrl(),
        submitUrl: content().submitUrl || sourceUrl(),
        maxWidth: content().maxWidthInPx !== undefined ? content().maxWidthInPx + 'px' : undefined,
        submitMethod: content().submitMethod,
        onSaveEvents: content().onSaveEvents,
        contentOverrides: routeBasedContentOverrides(content().contentOverrides, context.urlSegments()),
        sourceOverrides: sourceOverridesFromQueryParams(content().sourceOverrides, context.queryParams()),
        buttonDockOutlet: context.buttonDockOutlet(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  dock: (context: CalendarInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.CalendarDockInlineContentDto);
    return {
      loadComponent: () =>
        import('./dock-container/calendar-inline-dock-container.component').then(
          (module) => module.CalendarInlineDockContainerComponent,
        ),
      inputs: computed(() => ({
        content: content().content,
        parentRoutePath: context.parentRoutePath(),
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: context.urlSegments(),
        buttonDockOutlet: context.buttonDockOutlet(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  header: (context: CalendarInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.CalendarHeaderInlineContentDto);
    const segmentMapping = computed(() => ({ ...context.urlSegments() }));
    return {
      loadComponent: () =>
        import('./header-container/calendar-inline-header-container.component').then(
          (module) => module.CalendarInlineHeaderContainerComponent,
        ),
      inputs: computed(() => ({
        content: content(),
        sourceUrl: mapSourceUrl(content().sourceUrl, segmentMapping()),
        titlePath: content().titlePath,
        title: content().title,
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: segmentMapping(),
        parentRoutePath: context.parentRoutePath(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  list: (context: CalendarInlineContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.CalendarListInlineContentDto);
    return {
      loadComponent: () =>
        import('./list-container/calendar-inline-list-container.component').then(
          (module) => module.CalendarInlineListContainerComponent,
        ),
      inputs: computed(() => ({
        title: 'Termine',
        listUrl: replaceSegments(content().listUrl, context.urlSegments()),
        sourceUrl: replaceSegments(content().sourceUrl, context.urlSegments()),
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: context.urlSegments(),
        parentRoutePath: context.parentRoutePath(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
};
const createActionButtonRecipe = (context: CalendarActionRenderContext, outputs: DynamicOutputs) => ({
  loadComponent: () =>
    import('./actions/calendar-action-button.component').then((module) => module.CalendarActionButtonComponent),
  inputs: computed(() => ({
    title: context.action().title,
    loading: context.action().loading === true,
    icon: context.action().icon,
    imgBase64: context.action().imgBase64,
  })),
  outputs,
});
const ACTION_REGISTRY: Partial<Record<string, ActionRegistryItem>> = {
  'open-routed-content': (context: CalendarActionRenderContext) =>
    createActionButtonRecipe(context, {
      clicked: () => {
        const action = context.action() as KokuDto.CalendarOpenRoutedContentActionDto;
        if (action.route) {
          context.openRoutedContent(action.route.split('/'));
        }
      },
    }),
  'select-user': (context: CalendarActionRenderContext) =>
    createActionButtonRecipe(context, {
      clicked: () => {
        context
          .getPluginApi<{
            selectUser(): void;
          }>('CalendarUserSelectActionPlugin')
          ?.selectUser();
      },
    }),
};
export const CALENDAR_CONTENT_SETUP: CalendarContentSetup = {
  modalContentRegistry: MODAL_CONTENT_REGISTRY,
  inlineContentRegistry: INLINE_CONTENT_REGISTRY,
  actionRegistry: ACTION_REGISTRY,
};
