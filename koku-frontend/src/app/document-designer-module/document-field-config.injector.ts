import {InjectionToken} from "@angular/core";
import {ComponentType} from "@angular/cdk/overlay";

export interface DocumentConfig {
  fields: {
    [key: string]: {
      configComponent: ComponentType<any>;
      renderComponent: ComponentType<any>;
      name: string;
    }
  },
  gridSize: number;
}

export const DOCUMENT_CONFIG = new InjectionToken<DocumentConfig>('DocumentConfig');
