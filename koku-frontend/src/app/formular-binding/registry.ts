import { ButtonEvent, FieldEvent, FormularContentSetup, FormularContentStates } from '../formular/formular.component';
import { ButtonRendererComponent } from '../formular/button-renderer/button-renderer.component';
import { FieldRendererComponent } from '../formular/field-renderer/field-renderer.component';
import { DividerComponent } from './layouts/divider/divider.component';
import { GridContainerComponent } from './containers/grid-container/grid-container.component';
import { LayoutRendererComponent } from '../formular/layout-renderer/layout-renderer.component';
import { PictureUploadComponent } from '../fields/picture-upload/picture-upload.component';
import { InputFieldComponent } from '../fields/input/input-field.component';
import { TextareaFieldComponent } from '../fields/textarea/textarea-field.component';
import { CheckboxFieldComponent } from '../fields/checkbox/checkbox-field.component';
import { FieldsetContainerComponent } from './containers/fieldset-container/fieldset-container.component';
import { ButtonComponent } from '../button/button.component';
import { SelectFieldComponent } from '../fields/select/select-field.component';
import { MultiSelectWithPricesFieldComponent } from '../fields/multi-select-with-prices/multi-select-with-prices-field.component';
import { StatFieldComponent } from '../fields/stat/stat-field.component';
import { Subject } from 'rxjs';
import { signal } from '@angular/core';
import { MultiSelectFieldComponent } from '../fields/multi-select/multi-select-field.component';
import { FieldSlotRendererComponent } from '../formular/field-renderer/field-slot-renderer/field-slot-renderer.component';
import { DocumentDesignerFieldComponent } from '../fields/document/document-designer/document-designer-field.component';

const FIELD_INITIALIZER = (formularContent: KokuDto.AbstractFormField<any>) => {
  const result: FormularContentStates = {
    fields: {},
    containers: {},
    buttons: {},
    layouts: {},
  };
  result.fields[formularContent.id as string] = {
    value: signal(formularContent.defaultValue),
    disabledCauses: signal(new Set<string>(formularContent.disabled ? ['default'] : [])),
    requiredCauses: signal(new Set<string>(formularContent.required ? ['default'] : [])),
    readonlyCauses: signal(new Set<string>(formularContent.readonly ? ['default'] : [])),
    loadingCauses: signal(new Set<string>([])),
    config: formularContent,
    fieldEventBus: new Subject<{
      eventName: FieldEvent;
      payload?: any;
    }>(),
  };
  return result;
};

const CONTAINER_REGISTRY: Partial<
  Record<
    KokuDto.AbstractFormContainer['@type'],
    {
      componentType: any;
      stateInitializer: (formularContent: KokuDto.AbstractFormContainer) => FormularContentStates;
      inputBindings?(instance: any, formularContent: KokuDto.AbstractFormContainer): Record<string, any>;
      outputBindings?(instance: any, formularContent: KokuDto.AbstractFormContainer): Record<string, any>;
    }
  >
> = {
  grid: {
    componentType: GridContainerComponent,
    stateInitializer: (formularContent: KokuDto.AbstractFormContainer) => {
      const castedFormularContent = formularContent as KokuDto.GridContainer;

      const result: FormularContentStates = {
        fields: {},
        containers: {},
        buttons: {},
        layouts: {},
      };
      result.containers[formularContent.id as string] = {
        config: formularContent,
      };
      for (const currentContent of castedFormularContent.content || []) {
        if (currentContent['@type']) {
          const mappedFieldConfig = FIELD_REGISTRY[currentContent['@type'] as KokuDto.AbstractFormField<any>['@type']];
          if (mappedFieldConfig) {
            const fieldContentStates = mappedFieldConfig.stateInitializer(
              currentContent as KokuDto.AbstractFormField<any>,
            );
            result.buttons = Object.assign(result.buttons, fieldContentStates.buttons);
            result.fields = Object.assign(result.fields, fieldContentStates.fields);
            result.containers = Object.assign(result.containers, fieldContentStates.containers);
          }
          const mappedContainerConfig =
            CONTAINER_REGISTRY[currentContent['@type'] as KokuDto.AbstractFormContainer['@type']];
          if (mappedContainerConfig) {
            const containerContentStates = mappedContainerConfig.stateInitializer(
              currentContent as KokuDto.AbstractFormContainer,
            );
            result.buttons = Object.assign(result.buttons, containerContentStates.buttons);
            result.fields = Object.assign(result.fields, containerContentStates.fields);
            result.containers = Object.assign(result.containers, containerContentStates.containers);
          }
          const mappedButtonConfig = BUTTON_REGISTRY[currentContent['@type'] as KokuDto.KokuFormButton['@type']];
          if (mappedButtonConfig) {
            const buttonContentStates = mappedButtonConfig.stateInitializer(currentContent as KokuDto.KokuFormButton);
            result.buttons = Object.assign(result.buttons, buttonContentStates.buttons);
            result.fields = Object.assign(result.fields, buttonContentStates.fields);
            result.containers = Object.assign(result.containers, buttonContentStates.containers);
          }
        }
      }
      return result;
    },
  },
  fieldset: {
    componentType: FieldsetContainerComponent,
    stateInitializer: (formularContent: KokuDto.AbstractFormContainer) => {
      const castedFormularContent = formularContent as KokuDto.FieldsetContainer;
      const result: FormularContentStates = {
        fields: {},
        containers: {},
        buttons: {},
        layouts: {},
      };
      result.containers[formularContent.id as string] = {
        config: formularContent,
      };
      for (const currentContent of castedFormularContent.content || []) {
        if (currentContent['@type']) {
          const mappedFieldConfig = FIELD_REGISTRY[currentContent['@type'] as KokuDto.AbstractFormField<any>['@type']];
          if (mappedFieldConfig) {
            const fieldContentStates = mappedFieldConfig.stateInitializer(
              currentContent as KokuDto.AbstractFormField<any>,
            );
            result.buttons = Object.assign(result.buttons, fieldContentStates.buttons);
            result.fields = Object.assign(result.fields, fieldContentStates.fields);
            result.containers = Object.assign(result.containers, fieldContentStates.containers);
          }
          const mappedContainerConfig =
            CONTAINER_REGISTRY[currentContent['@type'] as KokuDto.AbstractFormContainer['@type']];
          if (mappedContainerConfig) {
            const containerContentStates = mappedContainerConfig.stateInitializer(
              currentContent as KokuDto.AbstractFormContainer,
            );
            result.buttons = Object.assign(result.buttons, containerContentStates.buttons);
            result.fields = Object.assign(result.fields, containerContentStates.fields);
            result.containers = Object.assign(result.containers, containerContentStates.containers);
          }
          const mappedButtonConfig = BUTTON_REGISTRY[currentContent['@type'] as KokuDto.KokuFormButton['@type']];
          if (mappedButtonConfig) {
            const buttonContentStates = mappedButtonConfig.stateInitializer(currentContent as KokuDto.KokuFormButton);
            result.buttons = Object.assign(result.buttons, buttonContentStates.buttons);
            result.fields = Object.assign(result.fields, buttonContentStates.fields);
            result.containers = Object.assign(result.containers, buttonContentStates.containers);
          }
        }
      }
      return result;
    },
  },
};
const FIELD_REGISTRY: Partial<
  Record<
    KokuDto.AbstractFormField<any>['@type'],
    {
      componentType: any;
      stateInitializer: (formularContent: KokuDto.AbstractFormField<any>) => FormularContentStates;
      inputBindings?(
        instance: FieldRendererComponent,
        formularContent: KokuDto.AbstractFormField<any>,
      ): Record<string, any>;
      outputBindings?(
        instance: FieldRendererComponent,
        formularContent: KokuDto.AbstractFormField<any>,
      ): Record<string, any>;
    }
  >
> = {
  'picture-upload': {
    componentType: PictureUploadComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings(
      instance: FieldRendererComponent,
      formularContent: KokuDto.PictureUploadFormularField,
    ): Record<string, any> {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.defaultValue !== undefined && { defaultValue: formularContent.defaultValue }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
      };
    },
  },
  input: {
    componentType: InputFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.InputFormularField) => {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.type && { type: formularContent.type }),
        ...(formularContent.placeholder && { placeholder: formularContent.placeholder }),
        ...(formularContent.defaultValue !== undefined && { defaultValue: formularContent.defaultValue }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onInput: (data: any) => instance.emitToFieldEventBus('onInput', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  select: {
    componentType: SelectFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.SelectFormularField) => {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.placeholder && { placeholder: formularContent.placeholder }),
        ...(formularContent.defaultValue !== undefined && { defaultValue: formularContent.defaultValue }),
        ...(formularContent.possibleValues !== undefined && { possibleValues: formularContent.possibleValues }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  stat: {
    componentType: StatFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.StatFormularField) => {
      return {
        ...(formularContent.defaultValue !== undefined && { defaultValue: formularContent.defaultValue }),
        ...(formularContent.title !== undefined && { title: formularContent.title }),
        ...(formularContent.description !== undefined && { description: formularContent.description }),
        ...(formularContent.icon !== undefined && { icon: formularContent.icon }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  textarea: {
    componentType: TextareaFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.InputFormularField) => {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.placeholder && { placeholder: formularContent.placeholder }),
        ...(formularContent.defaultValue !== undefined && { defaultValue: formularContent.defaultValue }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onInput: (data: any) => instance.emitToFieldEventBus('onInput', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  checkbox: {
    componentType: CheckboxFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.InputFormularField) => {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.placeholder && { placeholder: formularContent.placeholder }),
        ...(formularContent.defaultValue && { defaultValue: formularContent.defaultValue }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onInput: (data: any) => instance.emitToFieldEventBus('onInput', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  'multi-select-with-pricing-adjustment': {
    componentType: MultiSelectWithPricesFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (
      instance: FieldRendererComponent,
      formularContent: KokuDto.MultiSelectWithPricingAdjustmentFormularField,
    ) => {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.placeholder && { placeholder: formularContent.placeholder }),
        ...(formularContent.defaultValue && { defaultValue: formularContent.defaultValue }),
        ...(formularContent.possibleValues !== undefined && { possibleValues: formularContent.possibleValues }),
        idPathMapping: formularContent.idPathMapping,
        pricePathMapping: formularContent.pricePathMapping,
        uniqueValues: formularContent.uniqueValues,
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  'multi-select': {
    componentType: MultiSelectFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.MultiSelectFormularField) => {
      return {
        ...(formularContent.label && { label: formularContent.label }),
        ...(formularContent.placeholder && { placeholder: formularContent.placeholder }),
        ...(formularContent.defaultValue && { defaultValue: formularContent.defaultValue }),
        ...(formularContent.possibleValues !== undefined && { possibleValues: formularContent.possibleValues }),
        idPathMapping: formularContent.idPathMapping,
        uniqueValues: formularContent.uniqueValues,
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
  'document-designer': {
    componentType: DocumentDesignerFieldComponent,
    stateInitializer: FIELD_INITIALIZER,
    inputBindings: (instance: FieldRendererComponent, formularContent: KokuDto.DocumentDesignerFormularField) => {
      return {
        ...(formularContent.defaultValue && { defaultValue: formularContent.defaultValue }),
      };
    },
    outputBindings: (instance: FieldRendererComponent) => {
      return {
        onChange: (data: any) => instance.emitToFieldEventBus('onChange', data),
        onBlur: (data: any) => instance.emitToFieldEventBus('onBlur', data),
        onFocus: (data: any) => instance.emitToFieldEventBus('onFocus', data),
      };
    },
  },
};
const BUTTON_INITIALIZER = (formularContent: KokuDto.KokuFormButton) => {
  const result: FormularContentStates = {
    fields: {},
    containers: {},
    buttons: {},
    layouts: {},
  };
  result.buttons[formularContent.id as string] = {
    config: formularContent,
    loadingCauses: new Set<string>(),
    disabledCauses: new Set<string>(),
    buttonEventBus: new Subject<{
      eventName: ButtonEvent;
      payload?: any;
    }>(),
  };
  return result;
};
const BUTTON_REGISTRY: Partial<
  Record<
    KokuDto.KokuFormButton['@type'],
    {
      componentType: any;
      stateInitializer: (formularContent: KokuDto.KokuFormButton) => FormularContentStates;
      inputBindings?(instance: ButtonRendererComponent, formularContent: KokuDto.KokuFormButton): Record<string, any>;
      outputBindings?(instance: ButtonRendererComponent, formularContent: KokuDto.KokuFormButton): Record<string, any>;
    }
  >
> = {
  button: {
    componentType: ButtonComponent,
    stateInitializer: BUTTON_INITIALIZER,
    inputBindings: (instance: ButtonRendererComponent, formularContent: KokuDto.KokuFormButton) => {
      let res = {
        ...(formularContent.href && { href: formularContent.href }),
        ...(formularContent.hrefTarget && { hrefTarget: formularContent.hrefTarget }),
        ...(formularContent.buttonType && { buttonType: formularContent.buttonType }),
        ...(formularContent.title && { title: formularContent.title }),
        ...(formularContent.text && { text: formularContent.text }),
        ...(formularContent.styles && { styles: formularContent.styles }),
        ...(formularContent.icon && { icon: formularContent.icon }),
      };

      if (instance.enableDockedOutput()) {
        res = {
          ...res,
          ...((formularContent.dockableSettings || {}).text && { text: (formularContent.dockableSettings || {}).text }),
          ...((formularContent.dockableSettings || {}).title && {
            title: (formularContent.dockableSettings || {}).title,
          }),
          ...((formularContent.dockableSettings || {}).styles && {
            styles: (formularContent.dockableSettings || {}).styles,
          }),
          ...((formularContent.dockableSettings || {}).icon && { icon: (formularContent.dockableSettings || {}).icon }),
        };
      }

      return res;
    },
    outputBindings: (instance: ButtonRendererComponent) => {
      return {
        onClick: (data: any) => instance.emitToButtonEventBus('onClick', data),
      };
    },
  },
};
const FIELD_SLOT_REGISTRY: Partial<
  Record<
    KokuDto.IFormFieldSlot['@type'],
    {
      componentType: any;
      inputBindings?(
        instance: FieldSlotRendererComponent,
        formularContent: KokuDto.IFormFieldSlot,
      ): Record<string, any>;
      outputBindings?(
        instance: FieldSlotRendererComponent,
        formularContent: KokuDto.IFormFieldSlot,
      ): Record<string, any>;
    }
  >
> = {
  button: {
    componentType: ButtonComponent,
    inputBindings: (instance: FieldSlotRendererComponent, formularContent: KokuDto.KokuFieldSlotButton) => {
      return {
        ...(formularContent.href && { href: formularContent.href }),
        ...(formularContent.hrefTarget && { hrefTarget: formularContent.hrefTarget }),
        ...(formularContent.buttonType && { buttonType: formularContent.buttonType }),
        ...(formularContent.text && { text: formularContent.text }),
        ...(formularContent.title && { title: formularContent.title }),
        ...(formularContent.styles && { styles: formularContent.styles }),
        ...(formularContent.icon && { icon: formularContent.icon }),
        join: true,
      };
    },
    outputBindings: (instance: FieldSlotRendererComponent) => {
      return {
        onClick: (data: any) => instance.emitToFieldEventBus(instance.clickEventName(), data),
        onFocus: (data: any) => instance.emitToFieldEventBus(instance.focusEventName(), data),
        onBlur: (data: any) => instance.emitToFieldEventBus(instance.blurEventName(), data),
      };
    },
  },
};

const LAYOUT_INITIALIZER = (formularContent: KokuDto.AbstractFormLayout) => {
  const result: FormularContentStates = {
    fields: {},
    containers: {},
    buttons: {},
    layouts: {},
  };
  result.layouts[formularContent.id as string] = {
    config: formularContent,
  };
  return result;
};
const LAYOUT_REGISTRY: Partial<
  Record<
    KokuDto.AbstractFormLayout['@type'],
    {
      componentType: any;
      stateInitializer: (formularContent: KokuDto.AbstractFormLayout) => FormularContentStates;
      inputBindings?(
        instance: LayoutRendererComponent,
        formularContent: KokuDto.AbstractFormLayout,
      ): Record<string, any>;
      outputBindings?(
        instance: LayoutRendererComponent,
        formularContent: KokuDto.AbstractFormLayout,
      ): Record<string, any>;
    }
  >
> = {
  divider: {
    componentType: DividerComponent,
    stateInitializer: LAYOUT_INITIALIZER,
    inputBindings: (instance: LayoutRendererComponent, formularContent: KokuDto.DividerLayout) => {
      return {
        ...(formularContent.text && { text: formularContent.text }),
      };
    },
  },
};

export const FORMULAR_CONTENT_SETUP: FormularContentSetup = {
  fieldRegistry: FIELD_REGISTRY,
  fieldSlotRegistry: FIELD_SLOT_REGISTRY,
  buttonRegistry: BUTTON_REGISTRY,
  layoutRegistry: LAYOUT_REGISTRY,
  containerRegistry: CONTAINER_REGISTRY,
};
