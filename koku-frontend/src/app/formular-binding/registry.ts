import { FormularContentRenderContext, FormularContentRegistry } from '../formular/formular.component';
import { linkedSignal, computed } from '@angular/core';
import { FORM_OUTLET } from '../formular/form-outlet';
export const FORMULAR_CONTENT_REGISTRY: FormularContentRegistry = {
  grid: (context: FormularContentRenderContext<KokuDto.GridContainer>) => {
    return {
      render: () => ({
        loadComponent: () =>
          import('./containers/grid-container/grid-container.component').then(
            (module) => module.GridContainerComponent,
          ),
        inputs: computed(() => ({
          runtime: context.runtime,
          loading: context.loading(),
          submitting: context.submitting(),
          content: context.content(),
          contentRegistry: context.contentRegistry(),
          buttonDockOutlet: context.buttonDockOutlet(),
          context: context.context(),
        })),
      }),
    };
  },
  fieldset: (context: FormularContentRenderContext<KokuDto.FieldsetContainer>) => {
    return {
      render: () => ({
        loadComponent: () =>
          import('./containers/fieldset-container/fieldset-container.component').then(
            (module) => module.FieldsetContainerComponent,
          ),
        inputs: computed(() => ({
          runtime: context.runtime,
          loading: context.loading(),
          submitting: context.submitting(),
          content: context.content(),
          contentRegistry: context.contentRegistry(),
          buttonDockOutlet: context.buttonDockOutlet(),
          context: context.context(),
        })),
      }),
    };
  },
  condition: (context: FormularContentRenderContext<KokuDto.ConditionalContainer>) => {
    return {
      render: () => ({
        loadComponent: () =>
          import('./containers/conditional-container/conditional-container.component').then(
            (module) => module.ConditionalContainerComponent,
          ),
        inputs: computed(() => ({
          runtime: context.runtime,
          loading: context.loading(),
          submitting: context.submitting(),
          content: context.content(),
          contentRegistry: context.contentRegistry(),
          buttonDockOutlet: context.buttonDockOutlet(),
          context: context.context(),
        })),
      }),
    };
  },
  'picture-upload': (context: FormularContentRenderContext<KokuDto.PictureUploadFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/picture-upload/picture-upload.component').then((module) => module.PictureUploadComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          config: context.content(),
          label: context.content().label,
          defaultValue: context.content().defaultValue ?? '',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
        },
      }),
    };
  },
  input: (context: FormularContentRenderContext<KokuDto.InputFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/input/input-field.component').then((module) => module.InputFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
          type: context.content().type || 'TEXT',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'date-input': (context: FormularContentRenderContext<KokuDto.DateInputFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/input/date-input-field.component').then((module) => module.DateInputFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'time-input': (context: FormularContentRenderContext<KokuDto.TimeInputFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/input/time-input-field.component').then((module) => module.TimeInputFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'month-input': (context: FormularContentRenderContext<KokuDto.MonthInputFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/input/month-input-field.component').then((module) => module.MonthInputFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'week-input': (context: FormularContentRenderContext<KokuDto.WeekInputFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/input/week-input-field.component').then((module) => module.WeekInputFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  select: (context: FormularContentRenderContext<KokuDto.SelectFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/select/select-field.component').then((module) => module.SelectFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
          possibleValues: context.content().possibleValues ?? [],
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  stat: (context: FormularContentRenderContext<KokuDto.StatFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () => import('../fields/stat/stat-field.component').then((module) => module.StatFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          defaultValue: context.content().defaultValue,
          title: context.content().title,
          description: context.content().description,
          icon: context.content().icon,
        })),
        outputs: {
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  textarea: (context: FormularContentRenderContext<KokuDto.TextareaFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/textarea/textarea-field.component').then((module) => module.TextareaFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? '',
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  checkbox: (context: FormularContentRenderContext<KokuDto.CheckboxFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/checkbox/checkbox-field.component').then((module) => module.CheckboxFieldComponent),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue,
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          typed: (data: any) => context.runtime.emit(context.id, 'INPUT', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'multi-select-with-pricing-adjustment': (
    context: FormularContentRenderContext<KokuDto.MultiSelectWithPricingAdjustmentFormularField>,
  ) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/multi-select-with-prices/multi-select-with-prices-field.component').then(
            (module) => module.MultiSelectWithPricesFieldComponent,
          ),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? [],
          possibleValues: context.content().possibleValues ?? [],
          idPathMapping: context.content().idPathMapping,
          pricePathMapping: context.content().pricePathMapping,
          uniqueValues: context.content().uniqueValues,
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'multi-select': (context: FormularContentRenderContext<KokuDto.MultiSelectFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/multi-select/multi-select-field.component').then(
            (module) => module.MultiSelectFieldComponent,
          ),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          label: context.content().label,
          placeholder: context.content().placeholder,
          defaultValue: context.content().defaultValue ?? [],
          possibleValues: context.content().possibleValues ?? [],
          idPathMapping: context.content().idPathMapping,
          uniqueValues: context.content().uniqueValues,
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  'document-designer': (context: FormularContentRenderContext<KokuDto.DocumentDesignerFormularField>) => {
    return {
      control: {
        createValue: () =>
          linkedSignal(() => {
            const content = context.content();
            const contentOverride = context.override();
            if (contentOverride?.value !== undefined) {
              return contentOverride.value;
            }
            return context.runtime.sourceValue(content.valuePath, content.defaultValue);
          }),
        writeSource: (source, value) => {
          const valuePath = context.content().valuePath;
          if (valuePath) {
            source.set(valuePath, value);
          }
        },
      },
      render: (handle) => ({
        loadComponent: () =>
          import('../fields/document/document-designer/document-designer-field.component').then(
            (module) => module.DocumentDesignerFieldComponent,
          ),
        inputs: computed(() => ({
          name: context.id,
          value: handle.value!(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          required: Boolean(context.content().required) || handle.requiredCauses().size > 0,
          readonly: Boolean(context.content().readonly) || handle.readonlyCauses().size > 0,
          loading: context.loading() || handle.loadingCauses().size > 0,
          defaultValue: context.content().defaultValue,
        })),
        outputs: {
          changed: (data: any) => {
            context.runtime.updateContentValue(context.id, data);
            context.runtime.emit(context.id, 'CHANGE', data);
          },
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  button: (context: FormularContentRenderContext<KokuDto.KokuFormButton>) => {
    return {
      render: (handle) => ({
        loadComponent: () => import('../button/button.component').then((module) => module.ButtonComponent),
        inputs: computed(() => ({
          loading:
            (context.submitting() && context.content().buttonType === 'SUBMIT') ||
            Boolean(context.content().loading) ||
            handle.loadingCauses().size > 0 ||
            context.loading(),
          disabled:
            context.submitting() ||
            Boolean(context.override()?.disabled ?? context.content().disabled) ||
            handle.disabledCauses().size > 0,
          href: context.content().href,
          hrefTarget: context.content().hrefTarget,
          buttonType: context.content().buttonType,
          title: context.enableDockedOutput()
            ? context.content().dockableSettings?.title || context.content().title
            : context.content().title,
          text: context.enableDockedOutput()
            ? context.content().dockableSettings?.text || context.content().text
            : context.content().text,
          styles: context.enableDockedOutput()
            ? context.content().dockableSettings?.styles || context.content().styles
            : context.content().styles,
          icon: context.enableDockedOutput()
            ? context.content().dockableSettings?.icon || context.content().icon
            : context.content().icon,
          join: context.placementOutlet() !== FORM_OUTLET.CONTENT,
        })),
        outputs: {
          clicked: (data: any) => context.runtime.emit(context.id, 'CLICK', data),
          blurred: (data: any) => context.runtime.emit(context.id, 'BLUR', data),
          focused: (data: any) => context.runtime.emit(context.id, 'FOCUS', data),
        },
      }),
    };
  },
  divider: (context: FormularContentRenderContext<KokuDto.DividerLayout>) => {
    return {
      render: () => ({
        loadComponent: () => import('./layouts/divider/divider.component').then((module) => module.DividerComponent),
        inputs: computed(() => ({
          loading: context.loading(),
          disabled: context.submitting(),
          text: context.content().text,
        })),
      }),
    };
  },
};
