import { Signal } from '@angular/core';
import { DynamicRenderRecipe } from '../dynamic-host/dynamic-host.directive';
import type { ModalComponent } from './modal.component';

export interface ModalButtonType {
  loading?: boolean;
  disabled?: boolean;
  href?: string;
  hrefTarget?: 'BLANK' | 'SELF';
  buttonType?: 'BUTTON' | 'SUBMIT';
  title?: string;
  icon?: string;
  text?: string;
  styles?: (
    | 'NEUTRAL'
    | 'PRIMARY'
    | 'SECONDARY'
    | 'ACCENT'
    | 'INFO'
    | 'SUCCESS'
    | 'WARNING'
    | 'ERROR'
    | 'OUTLINE'
    | 'DASH'
    | 'SOFT'
    | 'GHOST'
    | 'LINK'
    | 'ACTIVE'
    | 'DISABLED'
    | 'WIDE'
    | 'BLOCK'
    | 'SQUARE'
    | 'CIRCLE'
  )[];
  size?: 'XS' | 'SM' | 'MD' | 'LG' | 'XL';
  onClick: (event: Event, modal: RenderedModalType, button: ModalButtonType) => void;
}

export interface RenderedModalButtonType extends ModalButtonType {
  uid: number;
}

export interface ModalDynamicContent {
  '@type': string;

  [key: string]: any;
}

export interface ModalContentRenderContext<TContent extends ModalDynamicContent = ModalDynamicContent> {
  instance: ModalComponent;
  modal: RenderedModalType;
  content: Signal<TContent>;
}

export type ModalContentRecipeFactory<TContent extends ModalDynamicContent = any> = (
  context: ModalContentRenderContext<TContent>,
) => DynamicRenderRecipe;

export type ModalContentSetup = Record<ModalDynamicContent['@type'] | string, ModalContentRecipeFactory>;

export interface ModalType {
  headline?: string;
  content?: string;
  dynamicContent?: ModalDynamicContent;
  urlSegments?: Record<string, string>;
  onCloseRequested?: () => void;
  clickOutside?: ($event: Event) => void;
  buttons?: ModalButtonType[];
  fullscreen?: boolean;
  dynamicContentSetup?: ModalContentSetup | Partial<ModalContentSetup>;
  parentRoutePath?: string;
  maxWidthInPx?: number;
}

export interface RenderedModalType extends ModalType {
  uid: number;
  close: () => void;
  update: (modal: ModalType) => void;
}
