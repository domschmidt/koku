import { Signal, computed } from '@angular/core';
import { ModalContentRenderContext, ModalContentSetup } from '../modal/modal.type';
import { FORMULAR_CONTENT_REGISTRY } from '../formular-binding/registry';
import { DynamicOutputs, DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';
import { OutletDirective } from '../portal/outlet.directive';
import { executeInlineFormularSaveEvents } from '../formular/inline-formular-save-events';
import {
  replaceRouteSegments as replaceSegments,
  resolveRouteBasedContentOverrides as routeBasedContentOverrides,
  resolveRoutePath as mapSourceUrl,
} from '../utils/route.utils';
export interface BusinessRulesContentRenderContext {
  content: Signal<KokuDto.AbstractKokuBusinessRuleContentDto>;
  loading: Signal<boolean>;
  contentSetup: Signal<BusinessRulesContentRegistry>;
  urlSegments: Signal<Record<string, string> | null>;
  parentRoutePath: Signal<string>;
  buttonDockOutlet: Signal<OutletDirective | undefined>;
  close(): void;
  openRoutedContent(routes: string[]): void;
}
export type BusinessRulesContentRecipeFactory = (context: BusinessRulesContentRenderContext) => DynamicRenderRecipe;
export interface BusinessRulesContentRegistry {
  modalContentRegistry: Partial<ModalContentSetup>;
  contentRegistry: Partial<
    Record<KokuDto.AbstractKokuBusinessRuleContentDto['@type'] | string, BusinessRulesContentRecipeFactory>
  >;
}
type ContentRegistryItem = NonNullable<BusinessRulesContentRegistry['contentRegistry'][string]>;
type ModalContentRegistryItem = NonNullable<ModalContentSetup[string]>;
const modalCloseOutputs = (context: ModalContentRenderContext): DynamicOutputs => ({
  closeRequested: () => {
    if (context.modal.onCloseRequested) {
      context.modal.onCloseRequested();
    } else {
      context.modal.close();
    }
  },
});
const MODAL_CONTENT_REGISTRY: Partial<
  Record<KokuDto.AbstractKokuBusinessRuleContentDto['@type'] | string, ModalContentRegistryItem>
> = {
  formular: (context: ModalContentRenderContext<KokuDto.KokuBusinessRuleFormularContentDto>) => {
    const content = computed(() => context.content());
    const sourceUrl = computed(() => replaceSegments(content().sourceUrl, context.modal.urlSegments));
    return {
      loadComponent: () => import('../formular/formular.component').then((module) => module.FormularComponent),
      inputs: computed(() => ({
        formularUrl: replaceSegments(content().formularUrl, context.modal.urlSegments),
        sourceUrl: sourceUrl(),
        submitUrl: content().submitUrl || sourceUrl(),
        maxWidth: content().maxWidthInPx !== undefined ? content().maxWidthInPx + 'px' : undefined,
        submitMethod: content().submitMethod,
        contentRegistry: FORMULAR_CONTENT_REGISTRY,
        contentOverrides: routeBasedContentOverrides(content().contentOverrides, context.modal.urlSegments),
      })),
      outputs: {
        saved: (payload: any) =>
          executeInlineFormularSaveEvents(content().onSaveEvents, payload, {
            openRoutedContent: () => undefined,
          }),
      },
    };
  },
  dock: (context: ModalContentRenderContext<KokuDto.KokuBusinessRuleDockContentDto>) => {
    const content = computed(() => context.content());
    return {
      loadComponent: () =>
        import('./dock-container/business-rules-dock-container.component').then(
          (module) => module.BusinessRulesDockContainerComponent,
        ),
      inputs: computed(() => ({
        content: content().content,
        contentSetup: BUSINESS_RULES_CONTENT_SETUP,
        urlSegments: context.modal.urlSegments || null,
        parentRoutePath: context.modal.parentRoutePath || '',
      })),
      outputs: modalCloseOutputs(context),
    };
  },
  header: (context: ModalContentRenderContext<KokuDto.KokuBusinessRuleHeaderContentDto>) => {
    const content = computed(() => context.content());
    return {
      loadComponent: () =>
        import('./header-container/business-rules-header-container.component').then(
          (module) => module.BusinessRulesHeaderContainerComponent,
        ),
      inputs: computed(() => ({
        content: content(),
        sourceUrl: mapSourceUrl(content().sourceUrl, context.modal.urlSegments),
        titlePath: content().titlePath,
        title: content().title,
        contentSetup: BUSINESS_RULES_CONTENT_SETUP,
        urlSegments: context.modal.urlSegments || null,
        parentRoutePath: context.modal.parentRoutePath || '',
      })),
      outputs: modalCloseOutputs(context),
    };
  },
};
const BUSINESS_RULES_CONTENT_REGISTRY: Partial<
  Record<KokuDto.AbstractKokuBusinessRuleContentDto['@type'] | string, ContentRegistryItem>
> = {
  formular: (context: BusinessRulesContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.KokuBusinessRuleFormularContentDto);
    const sourceUrl = computed(() => replaceSegments(content().sourceUrl, context.urlSegments()));
    return {
      loadComponent: () => import('../formular/formular.component').then((module) => module.FormularComponent),
      inputs: computed(() => ({
        formularUrl: replaceSegments(content().formularUrl, context.urlSegments()),
        sourceUrl: sourceUrl(),
        submitUrl: content().submitUrl || sourceUrl(),
        maxWidth: content().maxWidthInPx !== undefined ? content().maxWidthInPx + 'px' : undefined,
        submitMethod: content().submitMethod,
        contentRegistry: FORMULAR_CONTENT_REGISTRY,
        contentOverrides: routeBasedContentOverrides(content().contentOverrides, context.urlSegments()),
        buttonDockOutlet: context.buttonDockOutlet(),
      })),
      outputs: {
        saved: (payload: any) =>
          executeInlineFormularSaveEvents(content().onSaveEvents, payload, {
            openRoutedContent: (routes) => context.openRoutedContent(routes),
          }),
      },
    };
  },
  dock: (context: BusinessRulesContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.KokuBusinessRuleDockContentDto);
    return {
      loadComponent: () =>
        import('./dock-container/business-rules-dock-container.component').then(
          (module) => module.BusinessRulesDockContainerComponent,
        ),
      inputs: computed(() => ({
        content: content().content,
        contentSetup: BUSINESS_RULES_CONTENT_SETUP,
        urlSegments: context.urlSegments(),
        buttonDockOutlet: context.buttonDockOutlet(),
        parentRoutePath: context.parentRoutePath(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
  header: (context: BusinessRulesContentRenderContext) => {
    const content = computed(() => context.content() as KokuDto.KokuBusinessRuleHeaderContentDto);
    const segmentMapping = computed(() => ({ ...(context.urlSegments() || {}) }));
    return {
      loadComponent: () =>
        import('./header-container/business-rules-header-container.component').then(
          (module) => module.BusinessRulesHeaderContainerComponent,
        ),
      inputs: computed(() => ({
        content: content(),
        sourceUrl: mapSourceUrl(content().sourceUrl, segmentMapping()),
        titlePath: content().titlePath,
        title: content().title,
        contentSetup: BUSINESS_RULES_CONTENT_SETUP,
        urlSegments: segmentMapping(),
        parentRoutePath: context.parentRoutePath(),
      })),
      outputs: {
        closeRequested: () => context.close(),
        openRoutedContentRequested: (routes: string[]) => context.openRoutedContent(routes),
      },
    };
  },
};
export const BUSINESS_RULES_CONTENT_SETUP: BusinessRulesContentRegistry = {
  modalContentRegistry: MODAL_CONTENT_REGISTRY,
  contentRegistry: BUSINESS_RULES_CONTENT_REGISTRY,
};
