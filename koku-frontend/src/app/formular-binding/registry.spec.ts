import { computed, signal } from '@angular/core';
import { FORM_OUTLET } from '../formular/form-outlet';
import { FormularRuntime } from '../formular/formular.component';
import { FORMULAR_CONTENT_REGISTRY } from './registry';

describe('formular content recipes', () => {
  it('renders and binds every registered content recipe', async () => {
    for (const [type, factory] of Object.entries(FORMULAR_CONTENT_REGISTRY)) {
      expect(factory, `missing factory for ${type}`).toBeDefined();
      const runtime = new FormularRuntime(() => undefined);
      const defaultValue = type.includes('multi-select') ? [] : type === 'checkbox' ? false : 'initial';
      const content = signal({
        id: type,
        '@type': type,
        alias: type,
        valuePath: `values.${type}`,
        defaultValue,
        possibleValues: [{ id: 'one', name: 'One', price: 10 }],
        idPathMapping: 'id',
        pricePathMapping: 'price',
        uniqueValues: true,
        label: `Label ${type}`,
        placeholder: `Placeholder ${type}`,
        title: `Title ${type}`,
        text: `Text ${type}`,
        description: `Description ${type}`,
        icon: 'edit',
        buttonType: 'BUTTON',
        styles: ['PRIMARY'],
        dockableSettings: {
          title: `Dock ${type}`,
          text: `Dock text ${type}`,
          styles: ['SECONDARY'],
          icon: 'save',
        },
      } as any);
      const override = signal<any>(undefined);
      const recipe = factory!({
        id: type,
        content,
        runtime,
        override,
        loading: signal(false),
        submitting: signal(false),
        contentRegistry: signal(FORMULAR_CONTENT_REGISTRY),
        buttonDockOutlet: signal(undefined),
        enableDockedOutput: signal(false),
        placementOutlet: signal(FORM_OUTLET.CONTENT),
        context: signal({ source: 'registry-test' }),
      });
      const handle = runtime.resolveContent(type, recipe.control);
      runtime.initializeSource({}, []);
      const rendered = recipe.render(handle);

      expect(rendered.loadComponent).toBeDefined();
      expect(await rendered.loadComponent!()).toBeDefined();
      expect(rendered.inputs?.()).toBeTypeOf('object');
      for (const output of Object.values(rendered.outputs ?? {})) {
        output('changed-value');
      }
      override.set({ alias: type, value: defaultValue, disabled: true });
      handle.value?.();

      if (recipe.control?.writeSource) {
        expect(runtime.source()).toHaveProperty(`values.${type}`);
      }
    }
  }, 15_000);

  it('applies state causes and docked button settings', () => {
    const runtime = new FormularRuntime(() => undefined);
    const content = signal({
      id: 'button',
      '@type': 'button',
      title: 'Regular',
      text: 'Regular text',
      icon: 'edit',
      styles: ['PRIMARY'],
      buttonType: 'SUBMIT',
      loading: true,
      dockableSettings: {
        title: 'Docked',
        text: 'Docked text',
        icon: 'save',
        styles: ['SECONDARY'],
      },
    } as KokuDto.KokuFormButton);
    const recipe = FORMULAR_CONTENT_REGISTRY['button']!({
      id: 'button',
      content,
      runtime,
      override: signal({ alias: 'button', disabled: true }),
      loading: signal(true),
      submitting: signal(true),
      contentRegistry: signal(FORMULAR_CONTENT_REGISTRY),
      buttonDockOutlet: signal(undefined),
      enableDockedOutput: signal(true),
      placementOutlet: signal(FORM_OUTLET.APPEND_INNER),
      context: signal(undefined),
    });
    const inputs = recipe.render(runtime.resolveContent('button')).inputs!();

    expect(inputs).toEqual(
      expect.objectContaining({
        loading: true,
        disabled: true,
        title: 'Docked',
        text: 'Docked text',
        icon: 'save',
        styles: ['SECONDARY'],
        join: true,
      }),
    );
  });

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
