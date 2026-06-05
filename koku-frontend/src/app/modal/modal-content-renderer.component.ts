import { Component, input } from '@angular/core';
import { KokuDynamicHostDirective } from '../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../dynamic-host/recipe.utils';
import type { ModalComponent } from './modal.component';
import { ModalDynamicContent, RenderedModalType } from './modal.type';

@Component({
  selector: 'modal-content-renderer',
  imports: [KokuDynamicHostDirective],
  templateUrl: './modal-content-renderer.component.html',
})
export class ModalContentRendererComponent {
  instance = input.required<ModalComponent>();
  modal = input.required<RenderedModalType>();
  content = input.required<ModalDynamicContent>();

  recipe = createStableRecipe({
    identity: () => {
      const modal = this.modal();
      const content = this.content();
      return {
        modal,
        type: content['@type'],
        factory: requireRecipeFactory(modal.dynamicContentSetup ?? {}, content['@type'], 'modal content'),
      };
    },
    equal: (previous, current) =>
      previous.modal === current.modal && previous.type === current.type && previous.factory === current.factory,
    create: ({ modal, factory }) =>
      factory({
        instance: this.instance(),
        modal,
        content: this.content,
      }),
  });
}
