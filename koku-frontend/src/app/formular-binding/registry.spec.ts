import { computed, signal } from '@angular/core';
import { FORM_OUTLET } from '../formular/form-outlet';
import { FormularRuntime } from '../formular/formular.component';
import { FORMULAR_CONTENT_REGISTRY } from './registry';

describe('formular content recipes', () => {
  it('applies shared content overrides reactively to every matching recipe', () => {
    const runtime = new FormularRuntime(() => undefined);
    const createSelect = (id: string, valuePath: string) => {
      const content = signal({
        id,
        '@type': 'select',
        alias: 'customer',
        valuePath,
        defaultValue: '',
      } as KokuDto.SelectFormularField);
      const recipe = FORMULAR_CONTENT_REGISTRY['select']!({
        id,
        content,
        runtime,
        override: computed(() => {
          const alias = content().alias;
          return alias ? runtime.contentOverridesByAlias().get(alias) : undefined;
        }),
        loading: signal(false),
        submitting: signal(false),
        contentRegistry: signal(FORMULAR_CONTENT_REGISTRY),
        buttonDockOutlet: signal(undefined),
        enableDockedOutput: signal(false),
        placementOutlet: signal(FORM_OUTLET.CONTENT),
        context: signal(undefined),
      });
      const handle = runtime.resolveContent(id, recipe.control);
      return { handle, inputs: recipe.render(handle).inputs! };
    };
    const first = createSelect('first', 'firstCustomerId');
    const second = createSelect('second', 'secondCustomerId');
    const buttonContent = signal({
      id: 'button',
      '@type': 'button',
      alias: 'customer',
      buttonType: 'BUTTON',
      disabled: true,
    } as KokuDto.KokuFormButton);
    const buttonRecipe = FORMULAR_CONTENT_REGISTRY['button']!({
      id: 'button',
      content: buttonContent,
      runtime,
      override: computed(() => {
        const alias = buttonContent().alias;
        return alias ? runtime.contentOverridesByAlias().get(alias) : undefined;
      }),
      loading: signal(false),
      submitting: signal(false),
      contentRegistry: signal(FORMULAR_CONTENT_REGISTRY),
      buttonDockOutlet: signal(undefined),
      enableDockedOutput: signal(false),
      placementOutlet: signal(FORM_OUTLET.CONTENT),
      context: signal(undefined),
    });
    const buttonHandle = runtime.resolveContent('button', buttonRecipe.control);
    const buttonInputs = buttonRecipe.render(buttonHandle).inputs!;

    runtime.setContentOverrides([{ alias: 'customer', value: '41', disabled: true }]);
    runtime.initializeSource({}, []);

    expect(first.handle.value?.()).toBe('41');
    expect(second.handle.value?.()).toBe('41');
    expect(first.inputs()['disabled']).toBe(true);
    expect(second.inputs()['disabled']).toBe(true);
    expect(buttonInputs()['disabled']).toBe(true);
    expect(runtime.source()).toEqual({ firstCustomerId: '41', secondCustomerId: '41' });

    runtime.setContentOverrides([{ alias: 'customer', value: '42', disabled: false }]);
    runtime.syncContentValuesToSource();

    expect(first.handle.value?.()).toBe('42');
    expect(second.handle.value?.()).toBe('42');
    expect(first.inputs()['disabled']).toBe(false);
    expect(second.inputs()['disabled']).toBe(false);
    expect(buttonInputs()['disabled']).toBe(false);
    expect(runtime.source()).toEqual({ firstCustomerId: '42', secondCustomerId: '42' });
  });
});
