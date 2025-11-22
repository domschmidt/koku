import {FORMULAR_CONTENT_SETUP} from '../formular-binding/registry';
import {FormularFieldOverride, FormularSourceOverride} from '../formular/formular.component';
import {CalendarContentSetup, CalendarContext} from '../calendar/calendar.component';
import {ModalComponent} from '../modal/modal.component';
import {CalendarInlineDockContainerComponent} from './dock-container/calendar-inline-dock-container.component';
import {
  CalendarInlineFormularContainerComponent
} from './formular-container/calendar-inline-formular-container.component';
import {CalendarInlineContentComponent} from './calendar-inline-content/calendar-inline-content.component';
import {RenderedModalType} from '../modal/modal.type';
import {CalendarInlineListContainerComponent} from './list-container/calendar-inline-list-container.component';
import {CalendarInlineHeaderContainerComponent} from './header-container/calendar-inline-header-container.component';
import {LIST_CONTENT_SETUP} from '../list-binding/registry';
import {add, format, parse} from 'date-fns';
import {DASHBOARD_CONTENT_SETUP} from '../dashboard-binding/registry';

const MODAL_CONTENT_REGISTRY: Partial<Record<KokuDto.AbstractCalendarInlineContentDto["@type"] | string, {
  componentType: any;
  inputBindings?(instance: ModalComponent, modal: RenderedModalType, content: KokuDto.AbstractCalendarInlineContentDto): {
    [key: string]: any
  }
  outputBindings?(instance: ModalComponent, modal: RenderedModalType, content: KokuDto.AbstractCalendarInlineContentDto): {
    [key: string]: any
  }
}>> = {
  "formular": {
    componentType: CalendarInlineFormularContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, content: KokuDto.CalendarFormularInlineContentDto): {
      [key: string]: any
    } {
      let formularUrl = content.formularUrl || '';
      let sourceUrl = content.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of (content.fieldOverrides || [])) {
        if (currentFieldOverride.fieldId !== undefined) {
          if (currentFieldOverride['@type'] === 'route-based-override') {
            const castedFieldOverride = currentFieldOverride as KokuDto.ListViewRouteBasedFormularFieldOverrideDto;
            const newFieldValue = (modal.urlSegments || {})[castedFieldOverride.routeParam || ''];
            if (newFieldValue !== undefined) {
              fieldOverrides.push({
                fieldId: currentFieldOverride.fieldId,
                disable: currentFieldOverride.disable === true,
                value: newFieldValue
              });
            }
          }
        }
      }

      return {
        'formularUrl': formularUrl,
        'sourceUrl': sourceUrl,
        'submitUrl': content.submitUrl || sourceUrl,
        'maxWidth': content.maxWidthInPx + 'px',
        'submitMethod': content.submitMethod,
        'onSaveEvents': content.onSaveEvents,
        'contentSetup': DASHBOARD_CONTENT_SETUP,
        'fieldOverrides': fieldOverrides,
        // 'buttonDockOutlet': instance.buttonDockOutlet()
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarFormularInlineContentDto) => {
      return {
        // onClose: () => {
        //   instance.modalService.close(instance)
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      }
    }
  },
  "dock": {
    componentType: CalendarInlineDockContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, content: KokuDto.CalendarDockInlineContentDto): {
      [key: string]: any
    } {
      return {
        'content': content.content,
        'contentSetup': CALENDAR_CONTENT_SETUP,
        'urlSegments': modal.urlSegments,
        'parentRoutePath': modal.parentRoutePath,
        // 'buttonDockOutlet': instance.buttonDockOutlet()
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarDockInlineContentDto) => {
      return {
        // onClose: () => {
        //   instance.closeInlineContent()
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      }
    }
  },
  "header": {
    componentType: CalendarInlineHeaderContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarHeaderInlineContentDto): {
      [key: string]: any
    } {
      let segmentMapping: { [key: string]: string } = {...(modal.urlSegments || {})};
      let sourceUrl = undefined;
      if (inlineContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of inlineContent.sourceUrl.split("/")) {
          if (currentRoutePathToMatch.indexOf(":") === 0) {
            mappedSourceParts.push(segmentMapping[currentRoutePathToMatch]);
          } else {
            mappedSourceParts.push(currentRoutePathToMatch);
          }
        }
        sourceUrl = mappedSourceParts.join("/");
      }

      return {
        'content': inlineContent,
        'sourceUrl': sourceUrl,
        'titlePath': inlineContent.titlePath,
        'title': inlineContent.title,
        'contentSetup': CALENDAR_CONTENT_SETUP,
        'urlSegments': modal.urlSegments,
        'parentRoutePath': modal.parentRoutePath,
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarHeaderInlineContentDto) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
      }
    }
  },
  "list": {
    componentType: CalendarInlineListContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarListInlineContentDto): {
      [key: string]: any
    } {
      let listUrl = inlineContent.listUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        listUrl = listUrl.replace(segment, value);
      }
      let sourceUrl = inlineContent.sourceUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        'title': 'Termine',
        'listUrl': listUrl,
        'sourceUrl': sourceUrl,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': modal.urlSegments,
        'parentRoutePath': modal.parentRoutePath,
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarListInlineContentDto) => {
      return {
        // onClose: () => {
        //   instance.closeInlineContent()
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      }
    }
  },
};

const INLINE_CONTENT_REGISTRY: Partial<Record<KokuDto.AbstractCalendarInlineContentDto["@type"] | string, {
  componentType: any;
  inputBindings?(instance: CalendarInlineContentComponent, content: KokuDto.AbstractCalendarInlineContentDto): {
    [key: string]: any
  }
  outputBindings?(instance: CalendarInlineContentComponent, content: KokuDto.AbstractCalendarInlineContentDto): {
    [key: string]: any
  }
}>> = {
  "formular": {
    componentType: CalendarInlineFormularContainerComponent,
    inputBindings(instance: CalendarInlineContentComponent, content: KokuDto.CalendarFormularInlineContentDto): {
      [key: string]: any
    } {
      let formularUrl = content.formularUrl || '';
      let sourceUrl = content.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of (content.fieldOverrides || [])) {
        if (currentFieldOverride.fieldId !== undefined) {
          if (currentFieldOverride['@type'] === 'route-based-override') {
            const castedFieldOverride = currentFieldOverride as KokuDto.ListViewRouteBasedFormularFieldOverrideDto;
            const newFieldValue = (instance.urlSegments() || {})[castedFieldOverride.routeParam || ''];
            if (newFieldValue !== undefined) {
              fieldOverrides.push({
                fieldId: currentFieldOverride.fieldId,
                disable: currentFieldOverride.disable === true,
                value: newFieldValue
              });
            }
          }
        }
      }
      const sourceOverrides: FormularSourceOverride[] = [];
      for (const currentSourceOverride of (content.sourceOverrides || [])) {
        if (currentSourceOverride.sourcePath !== undefined && currentSourceOverride.value !== undefined) {

          const modifyOffset = (date: Date, offsetUnit?: KokuDto.CalendarFormularContextSourceOffsetUnitEnumDto, offsetValue?: number) => {
            if (offsetValue !== undefined && offsetUnit) {
              switch (offsetUnit) {
                case "SECOND": {
                  return add(date, {
                    seconds: offsetValue
                  });
                }
                case "MINUTE": {
                  return add(date, {
                    minutes: offsetValue
                  });
                }
                case "HOUR": {
                  return add(date, {
                    hours: offsetValue
                  });
                }
                case "DAY": {
                  return add(date, {
                    days: offsetValue
                  });
                }
                case "WEEK": {
                  return add(date, {
                    weeks: offsetValue
                  });
                }
                case "MONTH": {
                  return add(date, {
                    months: offsetValue
                  });
                }
                case "YEAR": {
                  return add(date, {
                    years: offsetValue
                  });
                }
                default: {
                  throw new Error(`Unknown offset type ${offsetUnit}`)
                }
              }
            }
            return date;
          };

          let newFieldValue;
          const queryParams = instance.activatedRoute.snapshot.queryParams;
          switch (currentSourceOverride.value) {
            case "SELECTION_START_DATE": {
              const rawValue = (queryParams as CalendarContext).selectionStartDate;
              if (rawValue) {
                const parsedDate = parse(rawValue, 'yyyy-MM-dd', new Date());
                if (parsedDate) {
                  newFieldValue = format(modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue), 'yyyy-MM-dd');
                }
              }
              break;
            }
            case "SELECTION_END_DATE": {
              const rawValue = (queryParams as CalendarContext).selectionEndDate;
              if (rawValue) {
                const parsedDate = parse(rawValue, 'yyyy-MM-dd', new Date());
                if (parsedDate) {
                  newFieldValue = format(modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue), 'yyyy-MM-dd');
                }
              }
              break;
            }
            case "SELECTION_START_TIME": {
              const rawValue = (queryParams as CalendarContext).selectionStartTime;
              if (rawValue) {
                const parsedDate = parse(rawValue, 'HH:mm', new Date());
                if (parsedDate) {
                  newFieldValue = format(modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue), 'HH:mm');
                }
              }
              break;
            }
            case "SELECTION_END_TIME": {
              const rawValue = (queryParams as CalendarContext).selectionEndTime;
              if (rawValue) {
                const parsedDate = parse(rawValue, 'HH:mm', new Date());
                if (parsedDate) {
                  newFieldValue = format(modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue), 'HH:mm');
                }
              }
              break;
            }
            case "SELECTION_START_DATETIME": {
              const rawValue = (queryParams as CalendarContext).selectionStartDateTime;
              if (rawValue) {
                const parsedDate = parse(rawValue, 'yyyy-MM-dd\'T\'HH:mm', new Date());
                if (parsedDate) {
                  newFieldValue = format(modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue), 'yyyy-MM-dd\'T\'HH:mm');
                }
              }
              break;
            }
            case "SELECTION_END_DATETIME": {
              const rawValue = (queryParams as CalendarContext).selectionEndDateTime;
              if (rawValue) {
                const parsedDate = parse(rawValue, 'yyyy-MM-dd\'T\'HH:mm', new Date());
                if (parsedDate) {
                  newFieldValue = format(modifyOffset(parsedDate, currentSourceOverride.offsetUnit, currentSourceOverride.offsetValue), 'yyyy-MM-dd\'T\'HH:mm');
                }
              }
              break;
            }
          }

          sourceOverrides.push({
            path: currentSourceOverride.sourcePath,
            value: newFieldValue
          });
        }
      }
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        formularUrl = formularUrl.replace(segment, value);
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        'formularUrl': formularUrl,
        'sourceUrl': sourceUrl,
        'submitUrl': content.submitUrl || sourceUrl,
        'maxWidth': content.maxWidthInPx + 'px',
        'submitMethod': content.submitMethod,
        'onSaveEvents': content.onSaveEvents,
        'contentSetup': FORMULAR_CONTENT_SETUP,
        'fieldOverrides': fieldOverrides,
        'sourceOverrides': sourceOverrides,
        'buttonDockOutlet': instance.buttonDockOutlet()
      }
    },
    outputBindings: (instance: CalendarInlineContentComponent, inlineContent: KokuDto.CalendarFormularInlineContentDto) => {
      return {
        // onClose: () => {
        //   instance.modalService.close(instance)
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      }
    }
  },
  "dock": {
    componentType: CalendarInlineDockContainerComponent,
    inputBindings(instance: CalendarInlineContentComponent, content: KokuDto.CalendarDockInlineContentDto): {
      [key: string]: any
    } {
      return {
        'content': content.content,
        'parentRoutePath': instance.parentRoutePath(),
        'contentSetup': CALENDAR_CONTENT_SETUP,
        'urlSegments': instance.urlSegments(),
        'buttonDockOutlet': instance.buttonDockOutlet()
      }
    },
    outputBindings: (instance: CalendarInlineContentComponent, inlineContent: KokuDto.CalendarDockInlineContentDto) => {
      return {
        // onClose: () => {
        //   instance.closeInlineContent()
        // },
        // onOpenRoutedContent: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      }
    }
  },
  "header": {
    componentType: CalendarInlineHeaderContainerComponent,
    inputBindings(instance: CalendarInlineContentComponent, inlineContent: KokuDto.CalendarHeaderInlineContentDto): {
      [key: string]: any
    } {
      let segmentMapping: { [key: string]: string } = {...(instance.urlSegments() || {})};
      let sourceUrl = undefined;
      if (inlineContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of inlineContent.sourceUrl.split("/")) {
          if (currentRoutePathToMatch.indexOf(":") === 0) {
            mappedSourceParts.push(segmentMapping[currentRoutePathToMatch]);
          } else {
            mappedSourceParts.push(currentRoutePathToMatch);
          }
        }
        sourceUrl = mappedSourceParts.join("/");
      }

      return {
        'content': inlineContent,
        'sourceUrl': sourceUrl,
        'titlePath': inlineContent.titlePath,
        'title': inlineContent.title,
        'contentSetup': CALENDAR_CONTENT_SETUP,
        'urlSegments': segmentMapping,
        'parentRoutePath': instance.parentRoutePath(),
      }
    },
    outputBindings: (instance: CalendarInlineContentComponent, inlineContent: KokuDto.CalendarHeaderInlineContentDto) => {
      return {
        onClose: () => {
          instance.closeInlineContent()
        },
        onOpenRoutedContent: (routes: string[]) => {
          instance.openRoutedContent(routes)
        },
      }
    }
  },
  "list": {
    componentType: CalendarInlineListContainerComponent,
    inputBindings(instance: CalendarInlineContentComponent, inlineContent: KokuDto.CalendarListInlineContentDto): {
      [key: string]: any
    } {
      let listUrl = inlineContent.listUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        listUrl = listUrl.replace(segment, value);
      }
      let sourceUrl = inlineContent.sourceUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        'title': 'Termine',
        'listUrl': listUrl,
        'sourceUrl': sourceUrl,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': instance.urlSegments(),
        'parentRoutePath': instance.parentRoutePath(),
      }
    },
    outputBindings: (instance: CalendarInlineContentComponent, inlineContent: KokuDto.CalendarListInlineContentDto) => {
      return {
        onClose: () => {
          instance.closeInlineContent()
        },
        onOpenRoutedContent: (routes: string[]) => {
          instance.openRoutedContent(routes)
        },
      }
    }
  },
};

export const CALENDAR_CONTENT_SETUP: CalendarContentSetup = {
  modalContentRegistry: MODAL_CONTENT_REGISTRY,
  inlineContentRegistry: INLINE_CONTENT_REGISTRY
}
