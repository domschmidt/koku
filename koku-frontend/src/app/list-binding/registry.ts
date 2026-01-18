import {ItemStylingSetup, ListContentSetup, ListFieldRegistrationType, ListItemSetup} from '../list/list.component';
import {InputFieldComponent} from '../fields/input/input-field.component';
import {PictureUploadComponent} from '../fields/picture-upload/picture-upload.component';
import {ListItemComponent} from '../list/list-item/list-item.component';
import {ListItemPreviewComponent} from '../list/list-item-preview/list-item-preview.component';
import {AvatarComponent} from '../avatar/avatar.component';
import {ListInlineContentComponent} from '../list/list-inline-content/list-inline-content.component';
import {FORMULAR_CONTENT_SETUP} from '../formular-binding/registry';
import {TextareaFieldComponent} from '../fields/textarea/textarea-field.component';
import {CheckboxFieldComponent} from '../fields/checkbox/checkbox-field.component';
import {ListDockContainerComponent} from './containers/dock-container/list-dock-container.component';
import {ListFormularContainerComponent} from './containers/formular-container/list-formular-container.component';
import {ListInlineListContainerComponent} from './containers/list-container/list-inline-list-container.component';
import {FormularFieldOverride} from '../formular/formular.component';
import {ListHeaderContainerComponent} from './containers/header-container/list-header-container.component';
import {TextCircleComponent} from '../text-circle/text-circle.component';
import {ListItemActionComponent} from '../list/list-item-action/list-item-action.component';
import {ButtonActionComponent} from './actions/button-action/button-action.component';
import {signal} from '@angular/core';
import {ConditionActionComponent} from './actions/condition-action/condition-action.component';
import {get} from '../utils/get';
import {ToastTypeUnion} from '../toast/toast.service';
import {set} from '../utils/set';
import {ListGridContainerComponent} from './containers/grid-container/list-grid-container.component';
import {ListChartContainerComponent} from './containers/chart-container/list-chart-container.component';
import {CHART_CONTENT_SETUP} from '../chart-binding/registry';
import {ModalContentSetup, RenderedModalType} from '../modal/modal.type';
import {ModalComponent} from '../modal/modal.component';
import {DocumentFormFieldComponent} from '../fields/document/document-form/document-form-field.component';
import {FileViewerComponent} from '../fields/file-viewer/file-viewer.component';
import {GLOBAL_EVENT_BUS} from '../events/global-events';
import {BarcodeCaptureComponent} from '../fields/barcode-capture/barcode-capture.component';

const ROUNDED_MAPPING: Partial<Record<KokuDto.KokuRoundedEnum, string>> = {
  'SM': 'rounded-sm',
  'MD': 'rounded-md',
  'LG': 'rounded-lg',
  'XL': 'rounded-xl',
  'XL2': 'rounded-2xl',
  'XL3': 'rounded-3xl',
  'XL4': 'rounded-4xl'
}
const COLOR_MAPPING: Partial<Record<KokuDto.KokuColorEnum, string>> = {
  "PRIMARY": "bg-primary text-primary-content",
  "SECONDARY": "bg-secondary text-secondary-content",
  "ACCENT": "bg-accent text-accent-content",
  "INFO": "bg-info text-info-content",
  "SUCCESS": "bg-success text-success-content",
  "WARNING": "bg-warning text-warning-content",
  "ERROR": "bg-error text-error-content",
  "RED": "bg-red",
  "ORANGE": "bg-orange",
  "AMBER": "bg-amber",
  "YELLOW": "bg-yellow",
  "LIME": "bg-lime",
  "GREEN": "bg-green",
  "EMERALD": "bg-emerald",
  "TEAL": "bg-teal",
  "CYAN": "bg-cyan",
  "SKY": "bg-sky",
  "BLUE": "bg-blue",
  "INDIGO": "bg-indigo",
  "VIOLET": "bg-violet",
  "PURPLE": "bg-purple",
  "FUCHSIA": "bg-fuchsia",
  "PINK": "bg-pink",
  "ROSE": "bg-rose",
  "SLATE": "bg-slate",
  "GRAY": "bg-gray",
  "ZINC": "bg-zinc",
  "NEUTRAL": "bg-neutral text-neutral-content",
  "STONE": "bg-stone",
};

const FIELD_INITIALIZER = (listContent: KokuDto.AbstractListViewFieldDto<any>, value: any) => {
  const result: ListFieldRegistrationType = {
    value: signal(value !== undefined ? value : listContent.defaultValue),
    config: listContent
  }
  return result;
}

const FIELD_REGISTRY: Partial<Record<KokuDto.AbstractListViewFieldDto<any>["@type"], {
  componentType: any;
  stateInitializer: (listContent: KokuDto.AbstractListViewFieldDto<any>, value: any) => ListFieldRegistrationType,
  inputBindings?(instance: ListItemComponent, key: string, listContent: KokuDto.AbstractListViewFieldDto<any>): {
    [key: string]: any
  }
  outputBindings?(instance: ListItemComponent, key: string, listContent: KokuDto.AbstractListViewFieldDto<any>): {
    [key: string]: any
  }
}
>> = {
  "picture-upload": {
    componentType: PictureUploadComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings(instance: ListItemComponent, key: string, listContent: KokuDto.ListViewPictureUploadFieldDto): {
      [p: string]: any
    } {
      return {}
    },
    outputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewPictureUploadFieldDto) => {
      return {
        onChange: (data: any) => instance.fieldEventBus(key, 'onChange', data),
      }
    }
  },
  "input": {
    componentType: InputFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewInputFieldDto) => {

      const clss = [
        listContent.rounded && 'p-1 my-1',
        listContent.rounded && ROUNDED_MAPPING[listContent.rounded],
        listContent.backgroundColor && COLOR_MAPPING[listContent.backgroundColor]
      ];

      return {
        ...(listContent.type && {type: listContent.type}),
        valueOnly: true,
        cls: clss.join(' ')
      }
    },
    outputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewInputFieldDto) => {
      return {
        onChange: (data: any) => instance.fieldEventBus(key, 'onChange', data),
        onInput: (data: any) => instance.fieldEventBus(key, 'onInput', data),
        onBlur: (data: any) => instance.fieldEventBus(key, 'onBlur', data),
        onFocus: (data: any) => instance.fieldEventBus(key, 'onFocus', data),
      }
    }
  },
  "textarea": {
    componentType: TextareaFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewTextareaFieldDto) => {
      return {
        valueOnly: true
      }
    },
    outputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewTextareaFieldDto) => {
      return {
        onChange: (data: any) => instance.fieldEventBus(key, 'onChange', data),
        onInput: (data: any) => instance.fieldEventBus(key, 'onInput', data),
        onBlur: (data: any) => instance.fieldEventBus(key, 'onBlur', data),
        onFocus: (data: any) => instance.fieldEventBus(key, 'onFocus', data),
      }
    }
  },
  "checkbox": {
    componentType: CheckboxFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewCheckboxFieldDto) => {
      return {
        valueOnly: true
      }
    },
    outputBindings: (instance: ListItemComponent, key: string, listContent: KokuDto.ListViewCheckboxFieldDto) => {
      return {
        onChange: (data: any) => instance.fieldEventBus(key, 'onChange', data),
        onInput: (data: any) => instance.fieldEventBus(key, 'onInput', data),
        onBlur: (data: any) => instance.fieldEventBus(key, 'onBlur', data),
        onFocus: (data: any) => instance.fieldEventBus(key, 'onFocus', data),
      }
    }
  },
};

const PREVIEW_REGISTRY: Partial<Record<KokuDto.AbstractListViewItemPreviewDto["@type"], {
  componentType: any;
  inputBindings?(instance: ListItemPreviewComponent, listPreviewContent: KokuDto.AbstractListViewItemPreviewDto): {
    [key: string]: any
  }
  outputBindings?(instance: ListItemPreviewComponent, listPreviewContent: KokuDto.AbstractListViewItemPreviewDto): {
    [key: string]: any
  }
}
>> = {
  "avatar": {
    componentType: AvatarComponent,
    inputBindings(instance: ListItemPreviewComponent, listPreviewContent: KokuDto.ListViewItemPreviewAvatarDto): {
      [p: string]: any
    } {
      return {}
    }
  },
  "text": {
    componentType: TextCircleComponent,
    inputBindings(instance: ListItemPreviewComponent, listPreviewContent: KokuDto.ListViewItemPreviewTextDto): {
      [p: string]: any
    } {
      return {}
    }
  },
};

const INLINE_CONTENT_REGISTRY: Partial<Record<KokuDto.AbstractListViewContentDto["@type"] | string, {
  componentType: any;
  inputBindings?(instance: ListInlineContentComponent, inlineContent: KokuDto.AbstractListViewContentDto): {
    [key: string]: any
  }
  outputBindings?(instance: ListInlineContentComponent, inlineContent: KokuDto.AbstractListViewContentDto): {
    [key: string]: any
  }
}>> = {
  "formular": {
    componentType: ListFormularContainerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewFormularContentDto): {
      [key: string]: any
    } {
      let formularUrl = inlineContent.formularUrl || '';
      let sourceUrl = inlineContent.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of (inlineContent.fieldOverrides || [])) {
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
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        formularUrl = formularUrl.replace(segment, value);
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        'formularUrl': formularUrl,
        'sourceUrl': sourceUrl,
        'urlSegments': instance.urlSegments(),
        'submitUrl': inlineContent.submitUrl || sourceUrl,
        'maxWidth': inlineContent.maxWidthInPx + 'px',
        'submitMethod': inlineContent.submitMethod,
        'onSaveEvents': inlineContent.onSaveEvents,
        'contentSetup': FORMULAR_CONTENT_SETUP,
        'fieldOverrides': fieldOverrides,
        'buttonDockOutlet': instance.buttonDockOutlet(),
        'context': instance.context(),
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewFormularContentDto) => {
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
  "document-form": {
    componentType: DocumentFormFieldComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewDocumentFormContentDto): {
      [key: string]: any
    } {
      let documentUrl = inlineContent.documentUrl || '';
      let submitUrl = inlineContent.submitUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        documentUrl = documentUrl.replace(segment, value);
        submitUrl = submitUrl.replace(segment, value);
      }

      return {
        'documentUrl': documentUrl,
        'submitUrl': submitUrl,
        'buttonDockOutlet': instance.buttonDockOutlet(),
        'context': instance.context(),
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewDocumentFormContentDto) => {
      return {
        onSubmit: (payload: any) => {
          for (const currentOnSubmitEvent of inlineContent.onSubmitEvents || []) {
            switch (currentOnSubmitEvent['@type']) {
              case "propagate-global-event": {
                const castedEventJob = currentOnSubmitEvent as KokuDto.ListViewInlineFormularContentAfterSavePropagateGlobalEventDto;
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
      }
    }
  },
  "file-viewer": {
    componentType: FileViewerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewFileViewerContentDto): {
      [key: string]: any
    } {
      let sourceUrl = inlineContent.sourceUrl || '';
      let fileUrl = inlineContent.fileUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        sourceUrl = sourceUrl.replace(segment, value);
        fileUrl = fileUrl.replace(segment, value);
      }

      return {
        'sourceUrl': sourceUrl,
        'fileUrl': fileUrl,
        'mimeTypeSourcePath': inlineContent.mimeTypeSourcePath,
        'buttonDockOutlet': instance.buttonDockOutlet(),
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewFileViewerContentDto) => {
      return {}
    }
  },
  "dock": {
    componentType: ListDockContainerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewDockContentDto): {
      [key: string]: any
    } {
      return {
        'content': inlineContent.content,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': instance.urlSegments(),
        'buttonDockOutlet': instance.buttonDockOutlet(),
        'parentRoutePath': instance.parentRoutePath(),
        'context': instance.context(),
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewDockContentDto) => {
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
  "header": {
    componentType: ListHeaderContainerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewHeaderContentDto): {
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
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': segmentMapping,
        'parentRoutePath': instance.parentRoutePath(),
        'context': instance.context(),
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewHeaderContentDto) => {
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
  "grid": {
    componentType: ListGridContainerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewGridContentDto): {
      [key: string]: any
    } {
      return {
        'content': inlineContent,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': instance.urlSegments(),
        'parentRoutePath': instance.parentRoutePath()
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewGridContentDto) => {
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
  "chart": {
    componentType: ListChartContainerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewChartContentDto): {
      [key: string]: any
    } {
      let chartUrl = inlineContent.chartUrl || '';
      for (const [segment, value] of Object.entries(instance.urlSegments() || {})) {
        chartUrl = chartUrl.replace(segment, value);
      }

      return {
        'chartUrl': chartUrl,
        'urlSegments': instance.urlSegments(),
        'chartContentSetup': CHART_CONTENT_SETUP,
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewChartContentDto) => {
      return {}
    }
  },
  "barcode": {
    componentType: BarcodeCaptureComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewBarcodeContentDto): {
      [key: string]: any
    } {
      return {
        'title': 'Termine',
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': instance.urlSegments(),
        'parentRoutePath': instance.parentRoutePath(),
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewBarcodeContentDto) => {
      return {
        afterCapture: (capturedValue: string) => {
          for (const currentEvent of inlineContent.onCaptureEvents || []) {
            switch (currentEvent['@type']) {
              case "propagate-global-event": {
                const castedEvent = currentEvent as KokuDto.ListViewBarcodeContentDtoAfterCapturePropagateGlobalEventDto;
                if (!castedEvent.eventName) {
                  throw new Error(`Missing eventName`);
                }
                GLOBAL_EVENT_BUS.propagateGlobalEvent(castedEvent.eventName, capturedValue);
                break;
              }
            }
          }
          instance.onClose.emit();
        }
      }
    }
  },
  "list": {
    componentType: ListInlineListContainerComponent,
    inputBindings(instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewListContentDto): {
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
        'contextMapping': inlineContent.context,
      }
    },
    outputBindings: (instance: ListInlineContentComponent, inlineContent: KokuDto.ListViewListContentDto) => {
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

const MODAL_REGISTRY: ModalContentSetup = {

  "formular": {
    componentType: ListFormularContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewFormularContentDto): {
      [key: string]: any
    } {
      let formularUrl = modalContent.formularUrl || '';
      let sourceUrl = modalContent.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of (modalContent.fieldOverrides || [])) {
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
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        formularUrl = formularUrl.replace(segment, value);
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        'formularUrl': formularUrl,
        'sourceUrl': sourceUrl,
        'urlSegments': modal.urlSegments,
        'submitUrl': modalContent.submitUrl || sourceUrl,
        'maxWidth': modalContent.maxWidthInPx + 'px',
        'submitMethod': modalContent.submitMethod,
        'onSaveEvents': modalContent.onSaveEvents,
        'contentSetup': FORMULAR_CONTENT_SETUP,
        'fieldOverrides': fieldOverrides,
        // 'buttonDockOutlet': instance.buttonDockOutlet()
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewFormularContentDto) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
        onOpenRoutedContent: (routes: string[]) => {
          // instance.openRoutedContent(routes)
        },
      }
    }
  },
  "dock": {
    componentType: ListDockContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewDockContentDto): {
      [key: string]: any
    } {
      return {
        'content': modalContent.content,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': modal.urlSegments,
        // 'buttonDockOutlet': instance.buttonDockOutlet(),
        'parentRoutePath': modal.parentRoutePath
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewDockContentDto) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
        onOpenRoutedContent: (routes: string[]) => {
          // instance.openRoutedContent(routes)
        },
      }
    }
  },
  "header": {
    componentType: ListHeaderContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewHeaderContentDto): {
      [key: string]: any
    } {
      let segmentMapping: { [key: string]: string } = {...(modal.urlSegments || {})};
      let sourceUrl = undefined;
      if (modalContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of modalContent.sourceUrl.split("/")) {
          if (currentRoutePathToMatch.indexOf(":") === 0) {
            mappedSourceParts.push(segmentMapping[currentRoutePathToMatch]);
          } else {
            mappedSourceParts.push(currentRoutePathToMatch);
          }
        }
        sourceUrl = mappedSourceParts.join("/");
      }

      return {
        'content': modalContent,
        'sourceUrl': sourceUrl,
        'titlePath': modalContent.titlePath,
        'title': modalContent.title,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': segmentMapping,
        'parentRoutePath': modal.parentRoutePath
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewHeaderContentDto) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
        onOpenRoutedContent: (routes: string[]) => {
          // instance.openRoutedContent(routes)
        },
      }
    }
  },
  "grid": {
    componentType: ListGridContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewGridContentDto): {
      [key: string]: any
    } {
      return {
        'content': modalContent,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': modal.urlSegments,
        'parentRoutePath': modal.parentRoutePath
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewGridContentDto) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
        onOpenRoutedContent: (routes: string[]) => {
          // instance.openRoutedContent(routes)
        },
      }
    }
  },
  "chart": {
    componentType: ListChartContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewChartContentDto): {
      [key: string]: any
    } {
      let chartUrl = modalContent.chartUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        chartUrl = chartUrl.replace(segment, value);
      }

      return {
        'chartUrl': chartUrl,
        'urlSegments': modal.urlSegments,
        'chartContentSetup': CHART_CONTENT_SETUP,
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewChartContentDto) => {
      return {}
    }
  },
  "list": {
    componentType: ListInlineListContainerComponent,
    inputBindings(instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewListContentDto): {
      [key: string]: any
    } {
      let listUrl = modalContent.listUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        listUrl = listUrl.replace(segment, value);
      }
      let sourceUrl = modalContent.sourceUrl || '';
      for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
        sourceUrl = sourceUrl.replace(segment, value);
      }

      return {
        'title': 'Termine',
        'listUrl': listUrl,
        'sourceUrl': sourceUrl,
        'contentSetup': LIST_CONTENT_SETUP,
        'urlSegments': modal.urlSegments,
        'parentRoutePath': modal.parentRoutePath
      }
    },
    outputBindings: (instance: ModalComponent, modal: RenderedModalType, modalContent: KokuDto.ListViewListContentDto) => {
      return {
        onClose: () => {
          if (modal.onCloseRequested) {
            modal.onCloseRequested();
          } else {
            modal.close();
          }
        },
        onOpenRoutedContent: (routes: string[]) => {
          // instance.openRoutedContent(routes)
        },
      }
    }
  },
};

const ACTION_REGISTRY: Partial<Record<KokuDto.AbstractListViewItemActionDto["@type"] | string, {
  componentType: any;
  inputBindings?(instance: ListItemActionComponent, inlineContent: KokuDto.AbstractListViewItemActionDto): {
    [key: string]: any
  }
  outputBindings?(instance: ListItemActionComponent, inlineContent: KokuDto.AbstractListViewItemActionDto): {
    [key: string]: any
  }
}>> = {
  "http-call": {
    componentType: ButtonActionComponent,
    inputBindings(instance: ListItemActionComponent, action: KokuDto.ListViewCallHttpListItemActionDto): {
      [key: string]: any
    } {
      return {}
    },
    outputBindings: (instance: ListItemActionComponent, action: KokuDto.ListViewCallHttpListItemActionDto) => {
      return {
        onClick: (event: MouseEvent) => {
          event.stopPropagation();

          const callAction = () => {
            if (action.method && action.url) {
              let url = action.url;
              for (const currentParam of action.params || []) {
                if (currentParam.param) {
                  switch (currentParam['@type']) {
                    case "value": {
                      const castedCurrentParam = currentParam as KokuDto.ListViewCallHttpListValueActionParamDto;
                      if (castedCurrentParam.valueReference) {
                        switch (castedCurrentParam.valueReference['@type']) {
                          case "field-reference": {
                            const castedReference = castedCurrentParam.valueReference as KokuDto.ListViewFieldReference;
                            if (!castedReference.fieldId) {
                              throw new Error('Missing fieldId in FieldReference');
                            }
                            const field = instance.register().fields[castedReference.fieldId];
                            if (!field) {
                              throw new Error('FieldReference not resolvable');
                            }
                            url = url.replaceAll(currentParam.param, field.value())
                            break;
                          }
                          case "source-path-reference": {
                            const castedReference = castedCurrentParam.valueReference as KokuDto.ListViewSourcePathReference;
                            if (!castedReference.valuePath) {
                              throw new Error('Missing valuePath in FieldReference');
                            }
                            url = url.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                            break;
                          }
                          default: {
                            throw new Error(`Unknown value reference type: ${castedCurrentParam.valueReference['@type']}`);
                          }
                        }
                      } else {
                        throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                      }
                      break;
                    }
                    default: {
                      throw new Error(`Unknown param type ${currentParam['@type']}`);
                    }
                  }
                }
              }
              return instance.httpClient.request<{ [key: string]: any }>(action.method, url);
            } else {
              throw new Error('http-call configuration is missing url and/or method');
            }
          }
          const executeEvents = (events: KokuDto.AbstractListViewActionEventDto[], eventPayload: {
            [key: string]: any
          } = {}) => {
            for (const currentEvent of events || []) {
              switch (currentEvent['@type']) {
                case 'reload': {
                  instance.onReload.emit();
                  break;
                }
                case 'event-payload-update': {
                  const castedEvent = currentEvent as KokuDto.ListViewEventPayloadUpdateActionEventDto;
                  if (!castedEvent.idPath) {
                    throw new Error('Missing id in event payload update');
                  }
                  const listRegisterIdx: { [key: string]: ListItemSetup } = {};
                  for (const currentEntry of instance.listRegister()) {
                    if (currentEntry.id !== undefined && currentEntry.id !== null) {
                      listRegisterIdx[currentEntry.id] = currentEntry;
                    }
                  }
                  const item = listRegisterIdx[String(get(eventPayload, castedEvent.idPath, ''))];
                  // it might be possible that the item is (currently) not shown. in this case, we cannot update the list.
                  if (item !== undefined) {
                    for (const [currentMappingPath, listViewReference] of Object.entries(castedEvent.valueMapping || {})) {
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
                    switch (currentParam['@type']) {
                      case "value": {
                        const castedParam = currentParam as KokuDto.ListViewCallHttpListValueActionParamDto;
                        if (castedParam.valueReference) {
                          switch (castedParam.valueReference['@type']) {
                            case "field-reference": {
                              const castedReference = castedParam.valueReference as KokuDto.ListViewFieldReference;
                              if (!castedReference.fieldId) {
                                throw new Error('Missing fieldId in FieldReference');
                              }
                              const field = instance.register().fields[castedReference.fieldId];
                              if (!field) {
                                throw new Error('FieldReference not resolvable');
                              }
                              notificationText = notificationText.replaceAll(currentParam.param, field.value())
                              break;
                            }
                            case "source-path-reference": {
                              const castedReference = castedParam.valueReference as KokuDto.ListViewSourcePathReference;
                              if (!castedReference.valuePath) {
                                throw new Error('Missing valuePath in FieldReference');
                              }
                              notificationText = notificationText.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                              break;
                            }
                            default: {
                              throw new Error(`Unknown value reference type: ${castedParam.valueReference['@type']}`);
                            }
                          }
                        } else {
                          throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                        }
                        break;
                      }
                      case "date-value": {
                        const castedParam = currentParam as KokuDto.ListViewCallHttpListValueActionParamDto;
                        if (castedParam.valueReference) {
                          switch (castedParam.valueReference['@type']) {
                            case "field-reference": {
                              const castedReference = castedParam.valueReference as KokuDto.ListViewFieldReference;
                              if (!castedReference.fieldId) {
                                throw new Error('Missing fieldId in FieldReference');
                              }
                              const field = instance.register().fields[castedReference.fieldId];
                              if (!field) {
                                throw new Error('FieldReference not resolvable');
                              }
                              notificationText = notificationText.replaceAll(currentParam.param, field.value())
                              break;
                            }
                            case "source-path-reference": {
                              const castedReference = castedParam.valueReference as KokuDto.ListViewSourcePathReference;
                              if (!castedReference.valuePath) {
                                throw new Error('Missing valuePath in FieldReference');
                              }
                              notificationText = notificationText.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                              break;
                            }
                            default: {
                              throw new Error(`Unknown value reference type: ${castedParam.valueReference['@type']}`);
                            }
                          }
                        } else {
                          throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                        }
                        break;
                      }
                      default: {
                        throw new Error(`Unknown param type ${currentParam['@type']}`);
                      }
                    }
                  }
                  let serenity: ToastTypeUnion = 'info';
                  if (castedEvent.serenity) {
                    switch (castedEvent.serenity) {
                      case "SUCCESS": {
                        serenity = 'success';
                        break;
                      }
                      case "ERROR": {
                        serenity = 'error';
                        break;
                      }
                      default:
                        throw new Error(`Unknown Notification serenity ${serenity}`);
                    }
                  }
                  instance.toastService.add(notificationText, serenity);
                  break;
                }
                case "propagate-global-event": {
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
          }

          if (action.userConfirmation) {
            let headline = action.userConfirmation.headline || '';
            let content = action.userConfirmation.content || '';

            for (const currentParam of action.userConfirmation.params || []) {
              if (!currentParam.param) {
                throw new Error(`Missing param`);
              }
              switch (currentParam['@type']) {
                case "value": {
                  const castedParam = currentParam as KokuDto.ListViewCallHttpListValueActionParamDto;
                  if (castedParam.valueReference) {
                    switch (castedParam.valueReference['@type']) {
                      case "field-reference": {
                        const castedReference = castedParam.valueReference as KokuDto.ListViewFieldReference;
                        if (!castedReference.fieldId) {
                          throw new Error('Missing fieldId in FieldReference');
                        }
                        const field = instance.register().fields[castedReference.fieldId];
                        if (!field) {
                          throw new Error('FieldReference not resolvable');
                        }
                        headline = headline.replaceAll(currentParam.param, field.value())
                        content = content.replaceAll(currentParam.param, field.value())
                        break;
                      }
                      case "source-path-reference": {
                        const castedReference = castedParam.valueReference as KokuDto.ListViewSourcePathReference;
                        if (!castedReference.valuePath) {
                          throw new Error('Missing valuePath in FieldReference');
                        }
                        headline = headline.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                        content = content.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                        break;
                      }
                      default: {
                        throw new Error(`Unknown value reference type: ${castedParam.valueReference['@type']}`);
                      }
                    }
                  } else {
                    throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                  }

                  break;
                }
                case "date-value": {
                  const castedParam = currentParam as KokuDto.ListViewCallHttpListValueActionParamDto;
                  if (castedParam.valueReference) {
                    switch (castedParam.valueReference['@type']) {
                      case "field-reference": {
                        const castedReference = castedParam.valueReference as KokuDto.ListViewFieldReference;
                        if (!castedReference.fieldId) {
                          throw new Error('Missing fieldId in FieldReference');
                        }
                        const field = instance.register().fields[castedReference.fieldId];
                        if (!field) {
                          throw new Error('FieldReference not resolvable');
                        }
                        headline = headline.replaceAll(currentParam.param, field.value())
                        content = content.replaceAll(currentParam.param, field.value())
                        break;
                      }
                      case "source-path-reference": {
                        const castedReference = castedParam.valueReference as KokuDto.ListViewSourcePathReference;
                        if (!castedReference.valuePath) {
                          throw new Error('Missing valuePath in FieldReference');
                        }
                        headline = headline.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                        content = content.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                        break;
                      }
                      default: {
                        throw new Error(`Unknown value reference type: ${castedParam.valueReference['@type']}`);
                      }
                    }
                  } else {
                    throw new Error(`Missing valuePath for param: ${currentParam.param}`);
                  }
                  break;
                }
                default: {
                  throw new Error(`Unknown param type ${currentParam['@type']}`);
                }
              }

            }

            const confirmationModal = instance.modalService.add({
              headline: headline,
              content: content,
              buttons: [{
                text: 'Abbrechen',
                styles: ['OUTLINE'],
                onClick: () => {
                  confirmationModal.close();
                }
              }, {
                text: 'Bestätigen',
                onClick: (event, modal, button) => {
                  button.loading = true;
                  button.disabled = true;

                  callAction().subscribe({
                    next: (rawValue) => {
                      executeEvents(action.successEvents || [], rawValue);
                      button.loading = false;
                      button.disabled = false;
                      confirmationModal.close();
                    }, error: () => {
                      executeEvents(action.failEvents || []);
                      button.loading = false;
                      button.disabled = false;
                      confirmationModal.update(modal);
                    }
                  })
                }
              }],
              clickOutside: () => {
                confirmationModal.close();
              }
            });
          } else {
            callAction().subscribe({
              next: (rawValue) => {
                executeEvents(action.successEvents || [], rawValue);
              },
              error: () => {
                executeEvents(action.failEvents || []);
              }
            });
          }
        },
      }
    }
  },
  "condition": {
    componentType: ConditionActionComponent,
    inputBindings(instance: ListItemActionComponent, action: KokuDto.ListViewConditionalItemValueActionDto): {
      [key: string]: any
    } {
      return {
        'parent': instance,
      }
    },
    outputBindings: (instance: ListItemActionComponent, action: KokuDto.ListViewConditionalItemValueActionDto) => {
      return {}
    }
  },
  "open-inline-content": {
    componentType: ButtonActionComponent,
    inputBindings(instance: ListItemActionComponent, action: KokuDto.ListViewOpenInlineContentItemActionDto): {
      [key: string]: any
    } {
      return {}
    },
    outputBindings: (instance: ListItemActionComponent, action: KokuDto.ListViewOpenInlineContentItemActionDto) => {
      return {
        onClick: (event: MouseEvent) => {
          event.stopPropagation();
          if (action.inlineContent) {
            instance.onOpenInlineContent.emit({
              content: action.inlineContent,
              id: instance.register().id,
              urlSegments: null
            });
          }
        },
      }
    }
  },
  "open-routed-content": {
    componentType: ButtonActionComponent,
    inputBindings(instance: ListItemActionComponent, action: KokuDto.ListViewOpenInlineContentItemActionDto): {
      [key: string]: any
    } {
      return {}
    },
    outputBindings: (instance: ListItemActionComponent, action: KokuDto.ListViewItemActionOpenRoutedContentActionDto) => {
      return {
        onClick: (event: MouseEvent) => {
          event.stopPropagation();
          if (action.route) {

            let route = action.route;
            for (const currentParam of action.params || []) {
              if (currentParam.param) {
                switch (currentParam['@type']) {
                  case "value": {
                    const castedCurrentParam = currentParam as KokuDto.ListViewItemActionOpenRoutedContentActionItemValueParamDto;

                    if (!castedCurrentParam.valueReference) {
                      throw new Error('Missing valueReference in FieldReference');
                    }
                    switch (castedCurrentParam.valueReference['@type']) {
                      case "field-reference": {
                        const castedReference = castedCurrentParam.valueReference as KokuDto.ListViewFieldReference;
                        if (!castedReference.fieldId) {
                          throw new Error('Missing fieldId in FieldReference');
                        }
                        const field = instance.register().fields[castedReference.fieldId];
                        if (!field) {
                          throw new Error('FieldReference not resolvable');
                        }
                        route = route.replaceAll(currentParam.param, field.value())
                        break;
                      }
                      case "source-path-reference": {
                        const castedReference = castedCurrentParam.valueReference as KokuDto.ListViewSourcePathReference;
                        if (!castedReference.valuePath) {
                          throw new Error('Missing valuePath in FieldReference');
                        }
                        route = route.replaceAll(currentParam.param, get(instance.register().source(), castedReference.valuePath))
                        break;
                      }
                    }

                    break;
                  }
                }
              }
            }

            instance.onOpenRoutedContent.emit(
              route.split('/')
            );
          }
        },
      }
    }
  },
};
const STYLING_REGISTRY: ItemStylingSetup = {
  "condition": {
    itemClasses(stylingDefinition: KokuDto.ListViewConditionalItemValueStylingDto, source: {
      [p: string]: any
    }): string[] {
      let matchesCondition = false;
      let currentValue = get(source, stylingDefinition.compareValuePath || '', null)
      for (const currentPositiveCompareValue of stylingDefinition.expectedValues || []) {
        if (currentValue == currentPositiveCompareValue) {
          matchesCondition = true;
          break;
        }
      }
      let styling = stylingDefinition.negativeStyling;
      if (matchesCondition) {
        styling = stylingDefinition.positiveStyling;
      }
      const result: string[] = [];
      if (styling?.opacity) {
        result.push(`opacity-${styling.opacity}`);
      }
      if (styling?.lineThrough) {
        result.push(`line-through`);
      }
      return result;
    }
  }
};

export const LIST_CONTENT_SETUP: ListContentSetup = {
  fieldRegistry: FIELD_REGISTRY,
  previewRegistry: PREVIEW_REGISTRY,
  inlineContentRegistry: INLINE_CONTENT_REGISTRY,
  actionRegistry: ACTION_REGISTRY,
  modalRegistry: MODAL_REGISTRY,
  itemStylingRegistry: STYLING_REGISTRY
}
