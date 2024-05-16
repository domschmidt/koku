import {ComponentType} from '@angular/cdk/overlay';

export type DocumentFieldMeta = { [key: string]: any };
export type DocumentFieldConfigurationTypes = {
  [key: string]: {
    component: ComponentType<any>;
    renderComponent: ComponentType<any>;
    meta: DocumentFieldMeta;
    name: string;
  }
};
export type DocumentRenderFieldTypes = {
  [key: string]: {
    component: ComponentType<any>;
    meta?: DocumentFieldMeta
  }
};
