import { Component, ComponentRef, computed, input, signal } from '@angular/core';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import type { ListContentSetup, ListItemSetup } from '../list.component';
import type { ListFieldEvent } from './list-item.component';
@Component({
  selector: 'list-field-renderer',
  imports: [KokuDynamicHostDirective],
  templateUrl: './list-field-renderer.component.html',
})
export class ListFieldRendererComponent {
  register = input.required<ListItemSetup>();
  contentSetup = input.required<ListContentSetup>();
  fieldId = input.required<string>();
  submitting = signal(false);
  private readonly identity = computed(
    () => {
      const id = this.fieldId();
      const fieldState = this.register().fields[id];
      const recipeFactory = fieldState ? this.contentSetup().fieldRegistry[fieldState.config['@type']] : undefined;
      if (!fieldState) {
        throw new Error(`List field state not found: ${id}`);
      }
      if (!recipeFactory) {
        throw new Error(`No list recipe registered for field type: ${fieldState.config['@type']}`);
      }
      return { id, fieldState, recipeFactory };
    },
    {
      equal: (previous, current) =>
        previous.id === current.id &&
        previous.fieldState === current.fieldState &&
        previous.recipeFactory === current.recipeFactory,
    },
  );
  private readonly config = computed(() => {
    const identity = this.identity();
    return this.register().fields[identity.id]?.config ?? identity.fieldState.config;
  });
  readonly recipe = computed(() => {
    const identity = this.identity();
    return identity.recipeFactory({
      id: identity.id,
      register: this.register,
      fieldState: identity.fieldState,
      config: this.config,
      submitting: this.submitting,
      emit: (eventName: ListFieldEvent, payload?: any) => this.emit(eventName, payload),
    });
  });
  captureInstance(instance: ComponentRef<unknown>) {
    this.emit('onInit', instance);
  }
  private emit(eventName: ListFieldEvent, payload?: any) {
    const fieldState = this.identity().fieldState;
    if (eventName === 'onInit') {
      fieldState.instance = payload;
    } else if (eventName === 'onChange') {
      fieldState.value.set(payload);
    }
  }
}
