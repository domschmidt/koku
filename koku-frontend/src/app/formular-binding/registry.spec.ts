import { signal } from '@angular/core';
import { FORM_OUTLET } from '../formular/form-outlet';
import { FormularRuntime } from '../formular/formular.component';
import { FORMULAR_CONTENT_REGISTRY } from './registry';

describe('formular content recipes', () => {
  it('matches shared aliases reactively inside each field recipe', () => {
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

    runtime.setContentOverrides([{ alias: 'customer', value: '41', disable: true }]);
    runtime.initializeSource({}, []);

    expect(first.handle.value?.()).toBe('41');
    expect(second.handle.value?.()).toBe('41');
    expect(first.inputs()['disabled']).toBeTrue();
    expect(second.inputs()['disabled']).toBeTrue();
    expect(runtime.source()).toEqual({ firstCustomerId: '41', secondCustomerId: '41' });

    runtime.setContentOverrides([{ alias: 'customer', value: '42', disable: false }]);
    runtime.syncContentValuesToSource();

    expect(first.handle.value?.()).toBe('42');
    expect(second.handle.value?.()).toBe('42');
    expect(first.inputs()['disabled']).toBeFalse();
    expect(second.inputs()['disabled']).toBeFalse();
    expect(runtime.source()).toEqual({ firstCustomerId: '42', secondCustomerId: '42' });
  });
});
