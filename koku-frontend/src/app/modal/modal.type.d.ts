import { ModalComponent } from './modal.component';

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

interface ModalDynamicContent {
  '@type': string;

  [key: string]: any;
}

type ModalContentSetup = Record<
  ModalDynamicContent['@type'],
  {
    componentType: any;
    inputBindings?(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: ModalDynamicContent,
    ): Record<string, any>;
    outputBindings?(
      instance: ModalComponent,
      modal: RenderedModalType,
      content: ModalDynamicContent,
    ): Record<string, any>;
  }
>;

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
