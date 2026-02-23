import { FORMULAR_CONTENT_SETUP } from '../formular-binding/registry';
import { FormularFieldOverride, FormularSourceOverride } from '../formular/formular.component';
import { CalendarContentSetup, CalendarContext } from '../calendar/calendar.component';
import { ModalComponent } from '../modal/modal.component';
import { CalendarInlineDockContainerComponent } from './dock-container/calendar-inline-dock-container.component';
import { CalendarInlineFormularContainerComponent } from './formular-container/calendar-inline-formular-container.component';
import { CalendarInlineContentComponent } from './calendar-inline-content/calendar-inline-content.component';
import { RenderedModalType } from '../modal/modal.type';
import { CalendarInlineListContainerComponent } from './list-container/calendar-inline-list-container.component';
import { CalendarInlineHeaderContainerComponent } from './header-container/calendar-inline-header-container.component';
import { LIST_CONTENT_SETUP } from '../list-binding/registry';
import { DASHBOARD_CONTENT_SETUP } from '../dashboard-binding/registry';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
dayjs.extend(customParseFormat);

const MODAL_CONTENT_REGISTRY: Partial<
  Record<
    KokuDto.AbstractCalendarInlineContentDto['@type'] | string,
    {
      componentType: any;
      inputBindings?(
        instance: ModalComponent,
        modal: RenderedModalType,
        content: KokuDto.AbstractCalendarInlineContentDto,
      ): Record<string, any>;
      outputBindings?(
        instance: ModalComponent,
        modal: RenderedModalType,
        content: KokuDto.AbstractCalendarInlineContentDto,
      ): Record<string, any>;
    }
  >
> = {
  formular: {
    componentType: CalendarInlineFormularContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: KokuDto.CalendarFormularInlineContentDto,
    ): Record<string, any> {
      const formularUrl = content.formularUrl || '';
      const sourceUrl = content.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of content.fieldOverrides || []) {
        if (currentFieldOverride.fieldId !== undefined) {
          if (currentFieldOverride['@type'] === 'route-based-override') {
            const castedFieldOverride = currentFieldOverride as KokuDto.ListViewRouteBasedFormularFieldOverrideDto;
            const newFieldValue = (modal.urlSegments || {})[castedFieldOverride.routeParam || ''];
            if (newFieldValue !== undefined) {
              fieldOverrides.push({
                fieldId: currentFieldOverride.fieldId,
                disable: currentFieldOverride.disable === true,
                value: newFieldValue,
              });
            }
          }
        }
      }

      return {
        formularUrl: formularUrl,
        sourceUrl: sourceUrl,
        submitUrl: content.submitUrl || sourceUrl,
        maxWidth: content.maxWidthInPx + 'px',
        submitMethod: content.submitMethod,
        onSaveEvents: content.onSaveEvents,
        contentSetup: DASHBOARD_CONTENT_SETUP,
        fieldOverrides: fieldOverrides,
        // 'buttonDockOutlet': instance.buttonDockOutlet()
      };
    },
    outputBindings: () => {
      return {
        // onClose: () => {
        //   instance.modalService.close(instance)
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      };
    },
  },
  dock: {
    componentType: CalendarInlineDockContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: KokuDto.CalendarDockInlineContentDto,
    ): Record<string, any> {
      return {
        content: content.content,
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: modal.urlSegments,
        parentRoutePath: modal.parentRoutePath,
        // 'buttonDockOutlet': instance.buttonDockOutlet()
      };
    },
    outputBindings: () => {
      return {
        // onClose: () => {
        //   instance.closeInlineContent()
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      };
    },
  },
  header: {
    componentType: CalendarInlineHeaderContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      inlineContent: KokuDto.CalendarHeaderInlineContentDto,
    ): Record<string, any> {
      const segmentMapping: Record<string, string> = { ...(modal.urlSegments || {}) };
      let sourceUrl = undefined;
      if (inlineContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of inlineContent.sourceUrl.split('/')) {
          if (currentRoutePathToMatch.indexOf(':') === 0) {
            mappedSourceParts.push(segmentMapping[currentRoutePathToMatch]);
          } else {
            mappedSourceParts.push(currentRoutePathToMatch);
          }
        }
        sourceUrl = mappedSourceParts.join('/');
      }

      return {
        content: inlineContent,
        sourceUrl: sourceUrl,
        titlePath: inlineContent.titlePath,
        title: inlineContent.title,
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: modal.urlSegments,
        parentRoutePath: modal.parentRoutePath,
      };
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
      };
    },
  },
  list: {
    componentType: CalendarInlineListContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      inlineContent: KokuDto.CalendarListInlineContentDto,
    ): Record<string, any> {
      let listUrl = inlineContent.listUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        listUrl = listUrl.replace(segment, value);
      }
      let sourceUrl = inlineContent.sourceUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        title: 'Termine',
        listUrl: listUrl,
        sourceUrl: sourceUrl,
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: modal.urlSegments,
        parentRoutePath: modal.parentRoutePath,
      };
    },
    outputBindings: () => {
      return {
        // onClose: () => {
        //   instance.closeInlineContent()
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      };
    },
  },
};

const INLINE_CONTENT_REGISTRY: Partial<
  Record<
    KokuDto.AbstractCalendarInlineContentDto['@type'] | string,
    {
      componentType: any;
      inputBindings?(
        instance: CalendarInlineContentComponent,
        content: KokuDto.AbstractCalendarInlineContentDto,
      ): Record<string, any>;
      outputBindings?(
        instance: CalendarInlineContentComponent,
        content: KokuDto.AbstractCalendarInlineContentDto,
      ): Record<string, any>;
    }
  >
> = {
  formular: {
    componentType: CalendarInlineFormularContainerComponent,
    inputBindings(
      instance: CalendarInlineContentComponent,
      content: KokuDto.CalendarFormularInlineContentDto,
    ): Record<string, any> {
      let formularUrl = content.formularUrl || '';
      let sourceUrl = content.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of content.fieldOverrides || []) {
        if (currentFieldOverride.fieldId !== undefined) {
          if (currentFieldOverride['@type'] === 'route-based-override') {
            const castedFieldOverride = currentFieldOverride as KokuDto.ListViewRouteBasedFormularFieldOverrideDto;
            const newFieldValue = (instance.urlSegments() || {})[castedFieldOverride.routeParam || ''];
            if (newFieldValue !== undefined) {
              fieldOverrides.push({
                fieldId: currentFieldOverride.fieldId,
                disable: currentFieldOverride.disable === true,
                value: newFieldValue,
              });
            }
          }
        }
      }
      const sourceOverrides: FormularSourceOverride[] = [];
      for (const currentSourceOverride of content.sourceOverrides || []) {
        if (currentSourceOverride.sourcePath !== undefined && currentSourceOverride.value !== undefined) {
          const modifyOffset = (
            date: Date,
            offsetUnit?: KokuDto.CalendarFormularContextSourceOffsetUnitEnumDto,
            offsetValue?: number,
          ) => {
            if (offsetValue !== undefined && offsetUnit) {
              switch (offsetUnit) {
                case 'SECOND': {
                  return dayjs(date).add(offsetValue, 'seconds').toDate();
                }
                case 'MINUTE': {
                  return dayjs(date).add(offsetValue, 'minutes').toDate();
                }
                case 'HOUR': {
                  return dayjs(date).add(offsetValue, 'hours').toDate();
                }
                case 'DAY': {
                  return dayjs(date).add(offsetValue, 'days').toDate();
                }
                case 'WEEK': {
                  return dayjs(date).add(offsetValue, 'weeks').toDate();
                }
                case 'MONTH': {
                  return dayjs(date).add(offsetValue, 'months').toDate();
                }
                case 'YEAR': {
                  return dayjs(date).add(offsetValue, 'years').toDate();
                }
                default: {
                  throw new Error(`Unknown offset type ${offsetUnit}`);
                }
              }
            }
            return date;
          };

          let newFieldValue;
          const queryParams = instance.activatedRoute.snapshot.queryParams;
          switch (currentSourceOverride.value) {
            case 'SELECTION_START_DATE': {
              const rawValue = (queryParams as CalendarContext).selectionStartDate;
              if (rawValue) {
                let parsedDateTmp = dayjs(rawValue, 'YYYY-MM-DD');
                if (!parsedDateTmp.isValid()) {
                  parsedDateTmp = dayjs(Date.now());
                }
                const parsedDate = parsedDateTmp.toDate();
                if (parsedDate) {
                  newFieldValue = dayjs(
                    modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
                  ).format('YYYY-MM-DD');
                }
              }
              break;
            }
            case 'SELECTION_END_DATE': {
              const rawValue = (queryParams as CalendarContext).selectionEndDate;
              if (rawValue) {
                let parsedDateTmp = dayjs(rawValue, 'YYYY-MM-DD');
                if (!parsedDateTmp.isValid()) {
                  parsedDateTmp = dayjs(Date.now());
                }
                const parsedDate = parsedDateTmp.toDate();
                if (parsedDate) {
                  newFieldValue = dayjs(
                    modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
                  ).format('YYYY-MM-DD');
                }
              }
              break;
            }
            case 'SELECTION_START_TIME': {
              const rawValue = (queryParams as CalendarContext).selectionStartTime;
              if (rawValue) {
                let parsedDateTmp = dayjs(rawValue, 'HH:mm');
                if (!parsedDateTmp.isValid()) {
                  parsedDateTmp = dayjs(Date.now());
                }
                const parsedDate = parsedDateTmp.toDate();
                if (parsedDate) {
                  newFieldValue = dayjs(
                    modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
                  ).format('HH:mm');
                }
              }
              break;
            }
            case 'SELECTION_END_TIME': {
              const rawValue = (queryParams as CalendarContext).selectionEndTime;
              if (rawValue) {
                let parsedDateTmp = dayjs(rawValue, 'HH:mm');
                if (!parsedDateTmp.isValid()) {
                  parsedDateTmp = dayjs(Date.now());
                }
                const parsedDate = parsedDateTmp.toDate();
                if (parsedDate) {
                  newFieldValue = dayjs(
                    modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
                  ).format('HH:mm');
                }
              }
              break;
            }
            case 'SELECTION_START_DATETIME': {
              const rawValue = (queryParams as CalendarContext).selectionStartDateTime;
              if (rawValue) {
                let parsedDateTmp = dayjs(rawValue, 'YYYY-MM-DDTHH:mm');
                if (!parsedDateTmp.isValid()) {
                  parsedDateTmp = dayjs(Date.now());
                }
                const parsedDate = parsedDateTmp.toDate();
                if (parsedDate) {
                  newFieldValue = dayjs(
                    modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
                  ).format('YYYY-MM-DDTHH:mm');
                }
              }
              break;
            }
            case 'SELECTION_END_DATETIME': {
              const rawValue = (queryParams as CalendarContext).selectionEndDateTime;
              if (rawValue) {
                let parsedDateTmp = dayjs(rawValue, 'YYYY-MM-DDTHH:mm');
                if (!parsedDateTmp.isValid()) {
                  parsedDateTmp = dayjs(Date.now());
                }
                const parsedDate = parsedDateTmp.toDate();
                if (parsedDate) {
                  newFieldValue = dayjs(
                    modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue),
                  ).format("yyyy-MM-dd'T'HH:mm");
                }
              }
              break;
            }
          }

          sourceOverrides.push({
            path: currentSourceOverride.sourcePath,
            value: newFieldValue,
          });
        }
      }
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        formularUrl = formularUrl.replace(segment, value);
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        formularUrl: formularUrl,
        sourceUrl: sourceUrl,
        submitUrl: content.submitUrl || sourceUrl,
        maxWidth: content.maxWidthInPx + 'px',
        submitMethod: content.submitMethod,
        onSaveEvents: content.onSaveEvents,
        contentSetup: FORMULAR_CONTENT_SETUP,
        fieldOverrides: fieldOverrides,
        sourceOverrides: sourceOverrides,
        buttonDockOutlet: instance.buttonDockOutlet(),
      };
    },
    outputBindings: () => {
      return {
        // onClose: () => {
        //   instance.modalService.close(instance)
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      };
    },
  },
  dock: {
    componentType: CalendarInlineDockContainerComponent,
    inputBindings(
      instance: CalendarInlineContentComponent,
      content: KokuDto.CalendarDockInlineContentDto,
    ): Record<string, any> {
      return {
        content: content.content,
        parentRoutePath: instance.parentRoutePath(),
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: instance.urlSegments(),
        buttonDockOutlet: instance.buttonDockOutlet(),
      };
    },
    outputBindings: () => {
      return {
        // onClose: () => {
        //   instance.closeInlineContent()
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      };
    },
  },
  header: {
    componentType: CalendarInlineHeaderContainerComponent,
    inputBindings(
      instance: CalendarInlineContentComponent,
      inlineContent: KokuDto.CalendarHeaderInlineContentDto,
    ): Record<string, any> {
      const segmentMapping: Record<string, string> = { ...(instance.urlSegments() || {}) };
      let sourceUrl = undefined;
      if (inlineContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of inlineContent.sourceUrl.split('/')) {
          if (currentRoutePathToMatch.indexOf(':') === 0) {
            mappedSourceParts.push(segmentMapping[currentRoutePathToMatch]);
          } else {
            mappedSourceParts.push(currentRoutePathToMatch);
          }
        }
        sourceUrl = mappedSourceParts.join('/');
      }

      return {
        content: inlineContent,
        sourceUrl: sourceUrl,
        titlePath: inlineContent.titlePath,
        title: inlineContent.title,
        contentSetup: CALENDAR_CONTENT_SETUP,
        urlSegments: segmentMapping,
        parentRoutePath: instance.parentRoutePath(),
      };
    },
    outputBindings: (instance: CalendarInlineContentComponent) => {
      return {
        onClose: () => {
          instance.closeInlineContent();
        },
        onOpenRoutedContent: (routes: string[]) => {
          instance.openRoutedContent(routes);
        },
      };
    },
  },
  list: {
    componentType: CalendarInlineListContainerComponent,
    inputBindings(
      instance: CalendarInlineContentComponent,
      inlineContent: KokuDto.CalendarListInlineContentDto,
    ): Record<string, any> {
      let listUrl = inlineContent.listUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        listUrl = listUrl.replace(segment, value);
      }
      let sourceUrl = inlineContent.sourceUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        title: 'Termine',
        listUrl: listUrl,
        sourceUrl: sourceUrl,
        contentSetup: LIST_CONTENT_SETUP,
        urlSegments: instance.urlSegments(),
        parentRoutePath: instance.parentRoutePath(),
      };
    },
    outputBindings: (instance: CalendarInlineContentComponent) => {
      return {
        onClose: () => {
          instance.closeInlineContent();
        },
        onOpenRoutedContent: (routes: string[]) => {
          instance.openRoutedContent(routes);
        },
      };
    },
  },
};

export const CALENDAR_CONTENT_SETUP: CalendarContentSetup = {
  modalContentRegistry: MODAL_CONTENT_REGISTRY,
  inlineContentRegistry: INLINE_CONTENT_REGISTRY,
};
