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
import {
  KokuBusinessRuleContent,
  KokuBusinessRuleDockContent,
  KokuBusinessRuleFormularContent,
  KokuBusinessRuleHeaderContent,
} from '../../types/generated/business-logic';

const MODAL_CONTENT_REGISTRY: Partial<
  Record<
    KokuBusinessRuleContent['type'],
    {
      componentType: any;
      inputBindings?(
        instance: ModalComponent,
        modal: RenderedModalType,
        content: KokuBusinessRuleContent,
      ): Record<string, any>;
      outputBindings?(
        instance: ModalComponent,
        modal: RenderedModalType,
        content: KokuBusinessRuleContent,
      ): Record<string, any>;
    }
  >
> = {
  formular: {
    componentType: BusinessRulesFormularContainerComponent,
    inputBindings(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: KokuBusinessRuleFormularContent,
    ): Record<string, any> {
      const formularUrl = content.formularUrl || '';
      const sourceUrl = content.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of content.fieldOverrides || []) {
        if (currentFieldOverride.fieldId !== undefined) {
          if (currentFieldOverride.type === 'route-based-override') {
            const newFieldValue = (modal.urlSegments || {})[currentFieldOverride.routeParam || ''];
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
      content: KokuBusinessRuleDockContent,
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
        closeRequested: () => {
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
      inlineContent: KokuBusinessRuleHeaderContent,
    ): Record<string, any> {
      const segmentMapping: Record<string, string> = { ...(modal.urlSegments || {}) };
      let sourceUrl = undefined;
      if (inlineContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of inlineContent.sourceUrl.split('/')) {
          if (currentRoutePathToMatch.indexOf(':') === 0) {
            const mappedSegment = segmentMapping[currentRoutePathToMatch];
            if (!mappedSegment) {
              throw new Error(`Missing route segment mapping for '${currentRoutePathToMatch}'`);
            }
            mappedSourceParts.push(mappedSegment);
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
        closeRequested: () => {
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
  //       // closeRequested: () => {
  //       //   instance.closeInlineContent()
  //       // },
  //       // openRoutedContentRequested: (routes: string[]) => {
  //       //   instance.openRoutedContent(routes)
  //       // },
  //     }
  //   }
  // },
};

const BUSINESS_RULES_CONTENT_REGISTRY: Partial<
  Record<
    KokuBusinessRuleContent['type'] | string,
    {
      componentType: any;
      inputBindings?(
        instance: BusinessRulesContentComponent,
        content: KokuBusinessRuleContent,
      ): Record<string, any>;
      outputBindings?(
        instance: BusinessRulesContentComponent,
        content: KokuBusinessRuleContent,
      ): Record<string, any>;
    }
  >
> = {
  formular: {
    componentType: BusinessRulesFormularContainerComponent,
    inputBindings(
      instance: BusinessRulesContentComponent,
      content: KokuBusinessRuleFormularContent,
    ): Record<string, any> {
      let formularUrl = content.formularUrl || '';
      let sourceUrl = content.sourceUrl || '';
      const fieldOverrides: FormularFieldOverride[] = [];
      for (const currentFieldOverride of content.fieldOverrides || []) {
        if (currentFieldOverride.fieldId !== undefined) {
          if (currentFieldOverride.type === 'route-based-override') {
            const newFieldValue = (instance.urlSegments() || {})[currentFieldOverride.routeParam || ''];
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
        // closeRequested: () => {
        //   instance.modalService.close(instance)
        // },
        // openRoutedContentRequested: (routes: string[]) => {
        //   instance.openRoutedContent(routes)
        // },
      };
    },
  },
  header: {
    componentType: CalendarInlineHeaderContainerComponent,
    inputBindings(
      instance: BusinessRulesContentComponent,
      inlineContent: KokuBusinessRuleHeaderContent,
    ): Record<string, any> {
      const segmentMapping: Record<string, string> = { ...(instance.urlSegments() || {}) };
      let sourceUrl = undefined;
      if (inlineContent.sourceUrl) {
        const mappedSourceParts: string[] = [];
        for (const currentRoutePathToMatch of inlineContent.sourceUrl.split('/')) {
          if (currentRoutePathToMatch.indexOf(':') === 0) {
            const mappedSegment = segmentMapping[currentRoutePathToMatch];
            if (!mappedSegment) {
              throw new Error(`Missing route segment mapping for '${currentRoutePathToMatch}'`);
            }
            mappedSourceParts.push(mappedSegment);
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
        closeRequested: () => {
          instance.closeInlineContent();
        },
        openRoutedContentRequested: (routes: string[]) => {
          instance.openRoutedContent(routes);
        },
      };
    },
  },
};

export interface BusinessRulesContentRegistry {
  modalContentRegistry: Partial<ModalContentSetup>;
  contentRegistry: Partial<
    Record<
      KokuBusinessRuleContent['type'] | string,
      {
        componentType: any;
        inputBindings?(
          instance: BusinessRulesContentComponent,
          content: KokuBusinessRuleContent,
        ): Record<string, any>;
        outputBindings?(
          instance: BusinessRulesContentComponent,
          content: KokuBusinessRuleContent,
        ): Record<string, any>;
      }
    >
  >;
}

export const BUSINESS_RULES_CONTENT_SETUP: BusinessRulesContentRegistry = {
  modalContentRegistry: MODAL_CONTENT_REGISTRY,
  contentRegistry: BUSINESS_RULES_CONTENT_REGISTRY,
};
