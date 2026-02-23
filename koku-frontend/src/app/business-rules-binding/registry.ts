import { ModalComponent } from '../modal/modal.component';
import { ModalContentSetup, RenderedModalType } from '../modal/modal.type';
import { BusinessRulesFormularContainerComponent } from './formular-container/business-rules-formular-container.component';
import { FORMULAR_CONTENT_SETUP } from '../formular-binding/registry';
import { FormularFieldOverride } from '../formular/formular.component';
import { BusinessRulesHeaderContainerComponent } from './header-container/business-rules-header-container.component';
import { CalendarInlineHeaderContainerComponent } from '../calendar-binding/header-container/calendar-inline-header-container.component';
import { BusinessRulesContentComponent } from './business-rules-content/business-rules-content.component';
import { BusinessRulesDockContainerComponent } from './dock-container/business-rules-dock-container.component';
import { CALENDAR_CONTENT_SETUP } from '../calendar-binding/registry';

const MODAL_CONTENT_REGISTRY: Partial<
  Record<
    KokuDto.AbstractKokuBusinessRuleContentDto['@type'],
    {
      componentType: any;
      inputBindings?(
        instance: ModalComponent,
        modal: RenderedModalType,
        content: KokuDto.AbstractKokuBusinessRuleContentDto,
      ): Record<string, any>;
      outputBindings?(
        instance: ModalComponent,
        modal: RenderedModalType,
        content: KokuDto.AbstractKokuBusinessRuleContentDto,
      ): Record<string, any>;
    }
  >
> = {
  formular: {
    componentType: BusinessRulesFormularContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: KokuDto.KokuBusinessRuleFormularContentDto,
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
        contentSetup: FORMULAR_CONTENT_SETUP,
        fieldOverrides: fieldOverrides,
      };
    },
    outputBindings: () => {
      return {};
    },
  },
  dock: {
    componentType: BusinessRulesDockContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: KokuDto.CalendarDockInlineContentDto,
    ): Record<string, any> {
      return {
        content: content.content,
        contentSetup: BUSINESS_RULES_CONTENT_SETUP,
        urlSegments: modal.urlSegments,
        parentRoutePath: modal.parentRoutePath,
        // 'buttonDockOutlet': instance.buttonDockOutlet()
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
  header: {
    componentType: BusinessRulesHeaderContainerComponent,
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
        contentSetup: BUSINESS_RULES_CONTENT_SETUP,
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
  // "list": {
  //   componentType: CalendarInlineListContainerComponent,
  //   inputBindings(instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarListInlineContentDto): {
  //     [key: string]: any
  //   } {
  //     let listUrl = inlineContent.listUrl || '';
  //     for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
  //       listUrl = listUrl.replace(segment, value);
  //     }
  //     let sourceUrl = inlineContent.sourceUrl || '';
  //     for (const [segment, value] of Object.entries(modal.urlSegments || {})) {
  //       sourceUrl = sourceUrl.replace(segment, value);
  //     }
  //
  //     return {
  //       'title': 'Termine',
  //       'listUrl': listUrl,
  //       'sourceUrl': sourceUrl,
  //       'contentSetup': CALENDAR_CONTENT_SETUP,
  //       'urlSegments': modal.urlSegments,
  //       'parentRoutePath': modal.parentRoutePath,
  //     }
  //   },
  //   outputBindings: (instance: ModalComponent, modal: RenderedModalType, inlineContent: KokuDto.CalendarListInlineContentDto) => {
  //     return {
  //       // onClose: () => {
  //       //   instance.closeInlineContent()
  //       // },
  //       // onOpenRoutedContent: (routes: string[]) => {
  //       //   instance.openRoutedContent(routes)
  //       // },
  //     }
  //   }
  // },
};

const BUSINESS_RULES_CONTENT_REGISTRY: Partial<
  Record<
    KokuDto.AbstractKokuBusinessRuleContentDto['@type'] | string,
    {
      componentType: any;
      inputBindings?(
        instance: BusinessRulesContentComponent,
        content: KokuDto.AbstractKokuBusinessRuleContentDto,
      ): Record<string, any>;
      outputBindings?(
        instance: BusinessRulesContentComponent,
        content: KokuDto.AbstractKokuBusinessRuleContentDto,
      ): Record<string, any>;
    }
  >
> = {
  formular: {
    componentType: BusinessRulesFormularContainerComponent,
    inputBindings(
      instance: BusinessRulesContentComponent,
      content: KokuDto.KokuBusinessRuleFormularContentDto,
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
  header: {
    componentType: CalendarInlineHeaderContainerComponent,
    inputBindings(
      instance: BusinessRulesContentComponent,
      inlineContent: KokuDto.KokuBusinessRuleHeaderContentDto,
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
    outputBindings: (instance: BusinessRulesContentComponent) => {
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

export interface BusinessRulesContentRegistry {
  modalContentRegistry: ModalContentSetup;
  contentRegistry: Partial<
    Record<
      KokuDto.AbstractKokuBusinessRuleContentDto['@type'] | string,
      {
        componentType: any;
        inputBindings?(
          instance: BusinessRulesContentComponent,
          content: KokuDto.AbstractKokuBusinessRuleContentDto,
        ): Record<string, any>;
        outputBindings?(
          instance: BusinessRulesContentComponent,
          content: KokuDto.AbstractKokuBusinessRuleContentDto,
        ): Record<string, any>;
      }
    >
  >;
}

export const BUSINESS_RULES_CONTENT_SETUP: BusinessRulesContentRegistry = {
  modalContentRegistry: MODAL_CONTENT_REGISTRY,
  contentRegistry: BUSINESS_RULES_CONTENT_REGISTRY,
};
